package com.example.NotsHub.service;


import com.example.NotsHub.payload.AuthenticationResult;
import com.example.NotsHub.payload.UserResponse;
import com.example.NotsHub.security.request.LoginRequest;
import com.example.NotsHub.security.response.MessageResponse;
import com.example.NotsHub.security.response.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AuthenticationResult login(LoginRequest loginRequest);

    ResponseEntity<MessageResponse> register(com.example.NotsHub.security.request.@Valid SignupRequest signUpRequest);

    UserInfoResponse getCurrentUserDetails(Authentication authentication);

    ResponseCookie logoutUser();

}
