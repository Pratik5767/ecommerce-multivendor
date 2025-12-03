package com.project.request;

import com.project.domain.USER_ROLE;
import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String otp;
    private USER_ROLE role;
}
