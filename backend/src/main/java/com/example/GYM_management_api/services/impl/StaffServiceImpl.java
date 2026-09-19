package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.LoginDto;
import com.example.GYM_management_api.dtos.LoginResponseDto;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import com.example.GYM_management_api.exceptions.AccountNotActiveException;
import com.example.GYM_management_api.exceptions.InvalidCredentialsException;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.security.JwtService;
import com.example.GYM_management_api.services.IStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements IStaffService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Email hoặc mật khẩu không đúng";

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponseDto login(LoginDto dto) {
        Staff staff = staffRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(dto.getPassword(), staff.getPassword())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        if (staff.getStatus() != StaffStatus.ACTIVE) {
            throw new AccountNotActiveException("Tài khoản đã bị khóa hoặc không hoạt động");
        }

        staff.setLastLogin(LocalDateTime.now());
        staffRepository.save(staff);

        String role = staff.getRole().name();
        String accessToken = jwtService.generateToken(staff.getId(), staff.getEmail(), role);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .user(LoginResponseDto.UserInfoDto.builder()
                        .id(staff.getId())
                        .code(staff.getCode())
                        .name(staff.getName())
                        .email(staff.getEmail())
                        .role(role)
                        .build())
                .build();
    }
}
