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
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping({"/api/auth/login", "/api/login"})
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto req) {
        try {
            AuthResponseDto response = authService.login(req);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", HttpStatus.UNAUTHORIZED.value(),
                            "error", "Unauthorized",
                            "message", e.getMessage()
                    ));
        } catch (LockedException | DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", HttpStatus.FORBIDDEN.value(),
                            "error", "Forbidden",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "error", "Bad Request",
                            "message", "Đăng nhập thất bại: " + e.getMessage()
                    ));
        }
    }

    @PostMapping({"/api/auth/logout", "/api/logout"})
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseEntity.ok(Map.of(
                "status", HttpStatus.OK.value(),
                "message", "Đăng xuất thành công"
        ));
    }

    @PostMapping({"/api/auth/register", "/api/register"})
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto req) {
        try {
            Map<String, Object> response = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "error", "Bad Request",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Register error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "error", "Bad Request",
                    "message", "Đăng ký thất bại: " + e.getMessage()
            ));
        }
    }

    @GetMapping({"/api/auth/me", "/api/me"})
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", HttpStatus.UNAUTHORIZED.value(),
                            "error", "Unauthorized",
                            "message", "Chưa xác thực"
                    ));
        }

        try {
            AuthResponseDto response = authService.getCurrentUser(authentication.getName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "status", HttpStatus.NOT_FOUND.value(),
                            "error", "Not Found",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Get current user error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "error", "Internal Server Error",
                            "message", "Lỗi lấy thông tin người dùng: " + e.getMessage()
                    ));
        }
    }
}
