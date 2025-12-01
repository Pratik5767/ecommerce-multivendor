package com.project.service;

import com.project.domain.USER_ROLE;
import com.project.request.OtpRequest;
import com.project.request.SignupRequest;
import com.project.response.AuthResponse;

public interface AuthService {

    String createUser(SignupRequest request) throws Exception;

    void sentOtp(String email, USER_ROLE role) throws Exception;

    AuthResponse signingIn(OtpRequest request) throws Exception;
}
