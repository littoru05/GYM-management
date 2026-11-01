package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.AuthResponseDto;
import com.example.GYM_management_api.dtos.LoginDto;
import com.example.GYM_management_api.dtos.RegisterDto;
import com.example.GYM_management_api.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Xác thực (Authentication)")
public class AuthController {

    private final AuthService authService;

    @PostMapping({ "/api/auth/login", "/api/login" })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto req) {
        AuthResponseDto response = authService.login(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping({ "/api/auth/logout", "/api/logout" })
    public ResponseEntity<Map<String, Object>> logout() {
        authService.logout();
        return ResponseEntity.ok(Map.of(
                "status", HttpStatus.OK.value(),
                "message", "Đăng xuất thành công"));
    }

    @PostMapping({ "/api/auth/register", "/api/register" })
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterDto req) {
        Map<String, Object> response = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({ "/api/auth/me", "/api/me" })
    public ResponseEntity<AuthResponseDto> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        }
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadCredentialsException("Chưa xác thực danh tính.");
        }
        AuthResponseDto response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/admin/test")
    public ResponseEntity<?> testAdminRole() {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Bạn là ADMIN - Đã truy cập tài nguyên quản trị thành công!"));
    }

}
