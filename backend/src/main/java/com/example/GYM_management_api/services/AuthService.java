package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.AuthResponseDto;
import com.example.GYM_management_api.dtos.LoginDto;
import com.example.GYM_management_api.dtos.RegisterDto;

import java.util.Map;

public interface AuthService {

    AuthResponseDto login(LoginDto request);

    Map<String, Object> register(RegisterDto request);

    AuthResponseDto getCurrentUser(String email);

    void logout();
}
