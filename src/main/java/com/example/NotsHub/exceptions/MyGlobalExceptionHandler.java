package com.example.NotsHub.exceptions;

import com.example.NotsHub.payload.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyGlobalExceptionHandler.class);

    // ── Validation errors (@Valid) ───────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        Map<String, String> response = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            response.put(fieldName, message);
        });
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ── ResourceNotFoundException ────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<?>> myResourceNotFoundException(
            ResourceNotFoundException e) {
        APIResponse<?> apiResponse = new APIResponse<>(
                e.getMessage(), false, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    // ── APIException ─────────────────────────────────────────
    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse<?>> myAPIException(
            APIException e) {
        APIResponse<?> apiResponse = new APIResponse<>(
                e.getMessage(), false, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<APIResponse<?>> handleAccessDenied(Exception e) {
        APIResponse<?> apiResponse = new APIResponse<>(
                "Access denied", false, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String details = e.getMostSpecificCause() != null
                ? e.getMostSpecificCause().getMessage()
                : e.getMessage();
        String normalized = details == null ? "" : details.toLowerCase();

        String message = "Invalid data. Please check your input and try again.";
        if (normalized.contains("pending_user_registration.uk_pending_user_registration_username")
                || normalized.contains("duplicate entry") && normalized.contains("username")) {
            message = "A pending registration already exists for this username. Please verify OTP or wait for expiry, then try again.";
        } else if (normalized.contains("pending_user_registration.uk_pending_user_registration_email")
                || normalized.contains("duplicate entry") && normalized.contains("email")) {
            message = "A pending registration already exists for this email. Please verify OTP or wait for expiry, then try again.";
        } else if (normalized.contains("users") && normalized.contains("username")) {
            message = "Username is already taken.";
        } else if (normalized.contains("users") && normalized.contains("email")) {
            message = "Email is already in use.";
        }

        APIResponse<?> apiResponse = new APIResponse<>(message, false, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse<?>> handleBadCredentialsException(BadCredentialsException e) {
        APIResponse<?> apiResponse = new APIResponse<>(
                "Invalid username or password.", false, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public ResponseEntity<APIResponse<?>> handleMultipartSizeException(Exception e) {
        String message = "File is too large. Maximum allowed upload size is 10 MB.";
        APIResponse<?> apiResponse = new APIResponse<>(message, false, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<?>> handleUnexpectedException(Exception e) {
        logger.error("Unhandled exception", e);
        APIResponse<?> apiResponse = new APIResponse<>(
                "Internal server error", false, null);
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
