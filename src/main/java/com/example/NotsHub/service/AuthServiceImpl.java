package com.example.NotsHub.service;

import com.example.NotsHub.Repository.EmailVerificationOtpRepository;
import com.example.NotsHub.Repository.PendingUserRegistrationRepository;
import com.example.NotsHub.Repository.RoleRepository;
import com.example.NotsHub.Repository.UserRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.AppRole;
import com.example.NotsHub.model.EmailVerificationOtp;
import com.example.NotsHub.model.PendingUserRegistration;
import com.example.NotsHub.model.Role;
import com.example.NotsHub.model.User;
import com.example.NotsHub.payload.AuthenticationResult;
import com.example.NotsHub.security.jwt.JwtUtils;
import com.example.NotsHub.security.request.LoginRequest;
import com.example.NotsHub.security.request.ResendEmailOtpRequest;
import com.example.NotsHub.security.request.SignupRequest;
import com.example.NotsHub.security.request.VerifyEmailOtpRequest;
import com.example.NotsHub.security.response.MessageResponse;
import com.example.NotsHub.security.response.UserInfoResponse;
import com.example.NotsHub.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;
    @Autowired
    EmailVerificationOtpRepository emailVerificationOtpRepository;
    @Autowired
    PendingUserRegistrationRepository pendingUserRegistrationRepository;
    @Autowired
    EmailOtpNotificationService emailOtpNotificationService;

    @org.springframework.beans.factory.annotation.Value("${app.email.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;
    @org.springframework.beans.factory.annotation.Value("${app.email.otp.max-attempts:5}")
    private int otpMaxAttempts;
    @org.springframework.beans.factory.annotation.Value("${app.email.otp.resend-cooldown-seconds:60}")
    private int otpResendCooldownSeconds;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public AuthenticationResult login(LoginRequest loginRequest) {
        User user = userRepository.findByUserName(loginRequest.getUsername())
                .orElseThrow(() -> new APIException("Invalid username or password."));

        if (!user.isEmailVerified()) {
            // Verify password before sending OTP to prevent OTP spam for wrong credentials
            if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new APIException("Invalid username or password.");
            }
            generateAndSendOtp(user, true);
            throw new APIException("Email OTP sent to your registered email. Verify OTP to complete registration.");
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(),
                userDetails.getUsername(), roles, userDetails.getEmail(), jwtCookie.toString());

        return new AuthenticationResult(response, jwtCookie);
    }

    @Override
    public ResponseEntity<MessageResponse> register(@Valid SignupRequest signUpRequest) {
        String normalizedUsername = signUpRequest.getUsername().trim();
        String normalizedEmail = signUpRequest.getEmail().trim().toLowerCase();

        if (userRepository.existsByUserName(normalizedUsername)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        if (pendingUserRegistrationRepository.findByUsernameIgnoreCase(normalizedUsername).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already pending verification. Please verify OTP or wait for expiry."));
        }

        if (pendingUserRegistrationRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already pending verification. Please verify OTP or wait for expiry."));
        }

        PendingUserRegistration pendingRegistration = new PendingUserRegistration();
        pendingRegistration.setUsername(normalizedUsername);
        pendingRegistration.setEmail(normalizedEmail);
        pendingRegistration.setPasswordHash(encoder.encode(signUpRequest.getPassword()));

        generateAndSendOtp(pendingRegistration, false);
        return ResponseEntity.ok(new MessageResponse("Registration started. OTP sent to your email. Verify OTP to complete registration."));
    }

    @Override
    public ResponseEntity<MessageResponse> verifyEmailOtp(VerifyEmailOtpRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        PendingUserRegistration pendingRegistration = pendingUserRegistrationRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new APIException("No pending registration found for this email. Please register first."));

        if (pendingRegistration.getExpiresAt().isBefore(LocalDateTime.now())) {
            pendingUserRegistrationRepository.delete(pendingRegistration);
            throw new APIException("OTP has expired. Pending registration removed. Please register again.");
        }

        if (pendingRegistration.getAttemptCount() >= otpMaxAttempts) {
            pendingUserRegistrationRepository.delete(pendingRegistration);
            throw new APIException("Maximum OTP attempts reached. Pending registration removed. Please register again.");
        }

        if (!encoder.matches(request.getOtp(), pendingRegistration.getOtpHash())) {
            int updatedAttempts = pendingRegistration.getAttemptCount() + 1;
            if (updatedAttempts >= otpMaxAttempts) {
                pendingUserRegistrationRepository.delete(pendingRegistration);
                throw new APIException("Maximum OTP attempts reached. Pending registration removed. Please register again.");
            }
            pendingRegistration.setAttemptCount(updatedAttempts);
            pendingUserRegistrationRepository.save(pendingRegistration);
            throw new APIException("Invalid OTP.");
        }

        if (userRepository.existsByUserName(pendingRegistration.getUsername())
                || userRepository.existsByEmail(pendingRegistration.getEmail())) {
            pendingUserRegistrationRepository.delete(pendingRegistration);
            throw new APIException("Account already exists. Please sign in.");
        }

        Role studentRole = roleRepository.findByRoleName(AppRole.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user = new User(pendingRegistration.getUsername(),
                pendingRegistration.getEmail(),
                pendingRegistration.getPasswordHash());
        user.setEmailVerified(true);
        user.getRoles().add(studentRole);
        userRepository.save(user);

        pendingUserRegistrationRepository.delete(pendingRegistration);

        return ResponseEntity.ok(new MessageResponse("Email verified successfully. Registration completed. You can sign in now."));
    }

    @Override
    public ResponseEntity<MessageResponse> resendEmailOtp(ResendEmailOtpRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        PendingUserRegistration pendingRegistration = pendingUserRegistrationRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new APIException("No pending registration found for this email. Please register first."));

        if (pendingRegistration.getExpiresAt().isBefore(LocalDateTime.now())) {
            pendingUserRegistrationRepository.delete(pendingRegistration);
            throw new APIException("Pending registration expired and removed. Please register again.");
        }

        generateAndSendOtp(pendingRegistration, true);
        return ResponseEntity.ok(new MessageResponse("A new OTP has been sent to your email."));
    }

    @Override
    public UserInfoResponse getCurrentUserDetails(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            return new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);
        }
        if (principal instanceof UserDetails userDetails) {
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            return new UserInfoResponse(null, userDetails.getUsername(), roles);
        }
        return new UserInfoResponse(null, authentication.getName(), List.of());
    }

    @Override
    public ResponseCookie logoutUser() {
        return jwtUtils.getCleanJwtCookie();
    }

    @Override
    public Page<UserInfoResponse> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(user -> new UserInfoResponse(
                        user.getUserId(),
                        user.getUserName(),
                        user.getRoles().stream()
                                .map(role -> role.getRoleName().name())
                                .collect(Collectors.toList()),
                        user.getEmail(),
                        null
                ));
    }

    private void generateAndSendOtp(User user, boolean enforceCooldown) {
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationOtp otpRecord = emailVerificationOtpRepository.findByUser(user)
                .orElseGet(() -> {
                    EmailVerificationOtp created = new EmailVerificationOtp();
                    created.setUser(user);
                    return created;
                });

        if (enforceCooldown && otpRecord.getLastSentAt() != null) {
            LocalDateTime allowedAt = otpRecord.getLastSentAt().plusSeconds(otpResendCooldownSeconds);
            if (allowedAt.isAfter(now)) {
                long remainingSeconds = java.time.Duration.between(now, allowedAt).getSeconds();
                throw new APIException("Please wait " + remainingSeconds + " seconds before requesting another OTP.");
            }
        }

        String otp = generateNumericOtp();
        otpRecord.setOtpHash(encoder.encode(otp));
        otpRecord.setExpiresAt(now.plusMinutes(otpExpiryMinutes));
        otpRecord.setAttemptCount(0);
        otpRecord.setLastSentAt(now);
        otpRecord.setVerifiedAt(null);

        emailVerificationOtpRepository.save(otpRecord);
        emailOtpNotificationService.sendVerificationOtp(user.getEmail(), otp, otpExpiryMinutes);
    }

    private void generateAndSendOtp(PendingUserRegistration pendingRegistration, boolean enforceCooldown) {
        LocalDateTime now = LocalDateTime.now();

        if (enforceCooldown && pendingRegistration.getLastSentAt() != null) {
            LocalDateTime allowedAt = pendingRegistration.getLastSentAt().plusSeconds(otpResendCooldownSeconds);
            if (allowedAt.isAfter(now)) {
                long remainingSeconds = java.time.Duration.between(now, allowedAt).getSeconds();
                throw new APIException("Please wait " + remainingSeconds + " seconds before requesting another OTP.");
            }
        }

        String otp = generateNumericOtp();
        pendingRegistration.setOtpHash(encoder.encode(otp));
        pendingRegistration.setExpiresAt(now.plusMinutes(otpExpiryMinutes));
        pendingRegistration.setAttemptCount(0);
        pendingRegistration.setLastSentAt(now);

        pendingUserRegistrationRepository.save(pendingRegistration);
        emailOtpNotificationService.sendVerificationOtp(pendingRegistration.getEmail(), otp, otpExpiryMinutes);
    }

    private String generateNumericOtp() {
        int otpValue = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otpValue);
    }
}
