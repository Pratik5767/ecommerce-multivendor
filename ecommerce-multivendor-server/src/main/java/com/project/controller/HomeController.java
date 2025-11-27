package com.project.controller;

import com.project.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping
    public ApiResponse HomeControllerHandler() {
        ApiResponse response = new ApiResponse();
        response.setMessage("Welcome to multivendor Ecommerce System");
        return response;
    }
}