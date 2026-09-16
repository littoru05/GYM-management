package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.AuthRequest;
import com.example.GYM_management_api.dtos.AuthResponse;
import com.example.GYM_management_api.dtos.RegisterRequest;

import java.util.Map;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    Map<String, Object> register(RegisterRequest request);

    AuthResponse getCurrentUser(String email);

    void logout();
}
