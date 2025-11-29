package com.project.service;

import com.project.request.LoginRequest;
import com.project.request.SignupRequest;
import com.project.response.AuthResponse;

public interface AuthService {

    String createUser(SignupRequest request) throws Exception;

    void sentOtp(String email) throws Exception;

    AuthResponse signingIn(LoginRequest request);
}
