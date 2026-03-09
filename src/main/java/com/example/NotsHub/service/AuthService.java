package com.example.NotsHub.service;


import com.example.NotsHub.payload.AuthenticationResult;
import com.example.NotsHub.security.request.*;
import com.example.NotsHub.security.response.MessageResponse;
import com.example.NotsHub.security.response.SignupStatusResponse;
import com.example.NotsHub.security.response.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AuthenticationResult login(LoginRequest loginRequest);

    ResponseEntity<MessageResponse> register(com.example.NotsHub.security.request.@Valid SignupRequest signUpRequest);

    ResponseEntity<SignupStatusResponse> getSignupStatus(String email);

    ResponseEntity<MessageResponse> verifyEmailOtp(VerifyEmailOtpRequest request);

    ResponseEntity<MessageResponse> resendEmailOtp(ResendEmailOtpRequest request);

    UserInfoResponse getCurrentUserDetails(Authentication authentication);

    ResponseCookie logoutUser();

    Page<UserInfoResponse> getAllUsers(int page, int size);

    ResponseEntity<MessageResponse> forgotPassword(ForgotPasswordRequest request);

    ResponseEntity<MessageResponse> resetPassword(ResetPasswordRequest request);
}
