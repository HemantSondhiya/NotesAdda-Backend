package com.example.NotsHub.controller;


import com.example.NotsHub.payload.AuthenticationResult;
import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.security.response.UserInfoResponse;
import com.example.NotsHub.security.request.LoginRequest;
import com.example.NotsHub.security.request.ResendEmailOtpRequest;
import com.example.NotsHub.security.request.SignupRequest;
import com.example.NotsHub.security.request.VerifyEmailOtpRequest;
import com.example.NotsHub.security.response.MessageResponse;
import com.example.NotsHub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        AuthenticationResult result = authService.login(loginRequest);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                result.getJwtCookie().toString())
                .body(result.getResponse());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return authService.register(signUpRequest);
    }

    @PostMapping("/verify-email-otp")
    public ResponseEntity<?> verifyEmailOtp(@Valid @RequestBody VerifyEmailOtpRequest request) {
        return authService.verifyEmailOtp(request);
    }

    @PostMapping("/resend-email-otp")
    public ResponseEntity<?> resendEmailOtp(@Valid @RequestBody ResendEmailOtpRequest request) {
        return authService.resendEmailOtp(request);
    }

    @GetMapping("/username")
    @PreAuthorize("isAuthenticated()")
    public String currentUserName(Authentication authentication){
        if (authentication != null)
            return authentication.getName();
        else
            return "user not authenticated";
    }


    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserDetails(Authentication authentication){
        return ResponseEntity.ok().body(authService.getCurrentUserDetails(authentication));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie cookie = authService.logoutUser();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                        cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size){
        Page<UserInfoResponse> users = authService.getAllUsers(page, size);
        return ResponseEntity.ok(new APIResponse<>("Users retrieved successfully", true, PagedResponse.from(users)));
    }



}
