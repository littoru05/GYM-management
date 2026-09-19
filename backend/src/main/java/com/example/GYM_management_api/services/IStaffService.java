package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.LoginDto;
import com.example.GYM_management_api.dtos.LoginResponseDto;

public interface IStaffService {
    LoginResponseDto login(LoginDto dto);
}
