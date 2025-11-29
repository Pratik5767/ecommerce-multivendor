package com.project.service;

import com.project.request.SignupRequest;

public interface AuthService {

    String createUser(SignupRequest request);
}
