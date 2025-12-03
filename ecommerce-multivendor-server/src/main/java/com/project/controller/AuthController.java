package com.project.controller;

import com.project.domain.USER_ROLE;
import com.project.request.LoginRequest;
import com.project.request.SignupRequest;
import com.project.response.ApiResponse;
import com.project.response.AuthResponse;
import com.project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody SignupRequest request) throws Exception {
        String jwt = authService.createUser(request);

        AuthResponse response = new AuthResponse();
        response.setJwt(jwt);
        response.setMessage("Register successfully");
        response.setRole(USER_ROLE.ROLE_CUSTOMER);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sent/otp")
    public ResponseEntity<ApiResponse> sendOtpHandler(@RequestBody LoginRequest request) throws Exception {
        authService.sentOtp(request.getEmail(), request.getRole());
        ApiResponse response = new ApiResponse();
        response.setMessage("OTP sent successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> loginHandler(@RequestBody LoginRequest request) throws Exception {
        String email = request.getEmail();
        USER_ROLE role = request.getRole();

        if (role.equals(USER_ROLE.ROLE_SELLER)) {
            request.setEmail("seller_"+email);
            AuthResponse response = authService.signingIn(request);
            return ResponseEntity.ok(response);
        } else {
            AuthResponse response = authService.signingIn(request);
            return ResponseEntity.ok(response);
        }
    }
}
