package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.AuthRequest;
import com.example.GYM_management_api.dtos.AuthResponse;
import com.example.GYM_management_api.dtos.RegisterRequest;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.security.JwtUtil;
import com.example.GYM_management_api.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        String emailOrCode = request.getEmail().trim();
        String password = request.getPassword().trim();

        Staff staff = staffRepository.findByEmailIgnoreCase(emailOrCode)
                .or(() -> staffRepository.findByCode(emailOrCode))
                .orElseThrow(() -> new BadCredentialsException("Email hoặc mật khẩu không chính xác."));

        if (staff.getStatus() == StaffStatus.LOCKED) {
            throw new LockedException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        if (staff.getStatus() != StaffStatus.ACTIVE) {
            throw new DisabledException("Tài khoản chưa được kích hoạt hoặc đã ngưng hoạt động.");
        }

        if (!passwordEncoder.matches(password, staff.getPassword())) {
            throw new BadCredentialsException("Email hoặc mật khẩu không chính xác.");
        }

        // Cập nhật thời điểm đăng nhập gần nhất
        staff.setLastLogin(LocalDateTime.now());
        staffRepository.save(staff);

        // Tạo JWT Token với payload: userId, email, role, exp
        String token = jwtUtil.generateToken(staff.getId(), staff.getEmail(), staff.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(staff.getId())
                .code(staff.getCode())
                .name(staff.getName())
                .email(staff.getEmail())
                .phone(staff.getPhone())
                .role(staff.getRole())
                .status(staff.getStatus())
                .build();
    }

    @Override
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (staffRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email này đã được sử dụng.");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (staffRepository.existsByPhone(request.getPhone().trim())) {
                throw new IllegalArgumentException("Số điện thoại này đã được sử dụng.");
            }
        }

        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = "STF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } else if (staffRepository.existsByCode(code.trim())) {
            throw new IllegalArgumentException("Mã nhân viên đã tồn tại.");
        }

        StaffRole role = request.getRole() != null ? request.getRole() : StaffRole.STAFF;

        Staff newStaff = Staff.builder()
                .code(code.trim())
                .name(request.getName().trim())
                .email(email)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .password(passwordEncoder.encode(request.getPassword().trim()))
                .role(role)
                .status(StaffStatus.ACTIVE)
                .build();

        staffRepository.save(newStaff);

        return Map.of(
                "message", "Đăng ký tài khoản thành công!",
                "code", newStaff.getCode(),
                "email", newStaff.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String email) {
        Staff staff = staffRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản: " + email));

        return AuthResponse.builder()
                .id(staff.getId())
                .code(staff.getCode())
                .name(staff.getName())
                .email(staff.getEmail())
                .phone(staff.getPhone())
                .role(staff.getRole())
                .status(staff.getStatus())
                .build();
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
