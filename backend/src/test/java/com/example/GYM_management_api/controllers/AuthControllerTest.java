package com.example.GYM_management_api.controllers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;

import com.example.GYM_management_api.dtos.AuthResponseDto;
import com.example.GYM_management_api.dtos.LoginDto;
import com.example.GYM_management_api.dtos.RegisterDto;
import com.example.GYM_management_api.services.AuthService;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@DisplayName("Kiểm thử tầng Controller: AuthController (Tầng Controller)")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("TC-30: Gọi API POST /api/auth/login thành công trả về HTTP 200 OK")
    void login_Success_Returns200() {
        LoginDto request = new LoginDto("admin@gym.local", "123456");
        AuthResponseDto mockResponse = AuthResponseDto.builder().token("test.jwt.token").build();

        when(authService.login(request)).thenReturn(mockResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    @DisplayName("TC-31: Gọi API POST /api/auth/login sai thông tin trả về HTTP 401 UNAUTHORIZED")
    void login_BadCredentials_Returns401() {
        LoginDto request = new LoginDto("admin@gym.local", "wrongpass");

        when(authService.login(request)).thenThrow(new BadCredentialsException("Sai mật khẩu"));

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("TC-32: Gọi API POST /api/auth/login tài khoản bị khóa trả về HTTP 403 FORBIDDEN")
    void login_Locked_Returns403() {
        LoginDto request = new LoginDto("locked@gym.local", "123456");
        when(authService.login(request)).thenThrow(new LockedException("Tài khoản bị khóa"));

        ResponseEntity<?> response = authController.login(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("TC-33: Gọi API POST /api/auth/login tài khoản chưa kích hoạt trả về HTTP 403 FORBIDDEN")
    void login_Disabled_Returns403() {
        LoginDto request = new LoginDto("disabled@gym.local", "123456");
        when(authService.login(request)).thenThrow(new DisabledException("Tài khoản chưa kích hoạt"));

        ResponseEntity<?> response = authController.login(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("TC-34: Gọi API POST /api/auth/logout thành công trả về HTTP 200 OK")
    void logout_Returns200() {
        ResponseEntity<?> response = authController.logout();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService, times(1)).logout();
    }

    @Test
    @DisplayName("TC-35: Gọi API POST /api/auth/register thành công trả về HTTP 201 CREATED")
    void register_Success_Returns201() {
        RegisterDto request = RegisterDto.builder().email("new@gym.local").fullName("New User").build();
        Map<String, Object> mockResult = Map.of("message", "Đăng ký thành công");

        when(authService.register(request)).thenReturn(mockResult);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResult, response.getBody());
    }

    @Test
    @DisplayName("TC-36: Gọi API POST /api/auth/register trùng lặp thông tin trả về HTTP 400 BAD REQUEST")
    void register_IllegalArgument_Returns400() {
        RegisterDto request = RegisterDto.builder().email("exist@gym.local").build();

        when(authService.register(request)).thenThrow(new IllegalArgumentException("Email đã tồn tại"));

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("TC-37: Gọi API GET /api/auth/me khi đã đăng nhập trả về HTTP 200 OK")
    void getCurrentUser_Authenticated_Returns200() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("admin@gym.local");

        AuthResponseDto mockDto = AuthResponseDto.builder().email("admin@gym.local").build();
        when(authService.getCurrentUser("admin@gym.local")).thenReturn(mockDto);

        ResponseEntity<?> response = authController.getCurrentUser(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockDto, response.getBody());
    }

    @Test
    @DisplayName("TC-38: Gọi API GET /api/auth/me khi chưa đăng nhập trả về HTTP 401 UNAUTHORIZED")
    void getCurrentUser_Unauthenticated_Returns401() {
        ResponseEntity<?> responseNull = authController.getCurrentUser(null);
        assertEquals(HttpStatus.UNAUTHORIZED, responseNull.getStatusCode());

        Authentication unauth = mock(Authentication.class);
        when(unauth.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> responseFalse = authController.getCurrentUser(unauth);
        assertEquals(HttpStatus.UNAUTHORIZED, responseFalse.getStatusCode());
    }

    @Test
    @DisplayName("TC-39: Gọi API GET /api/admin/test kiểm tra quyền ADMIN trả về HTTP 200 OK")
    void testAdminRole_Returns200() {
        ResponseEntity<?> response = authController.testAdminRole();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
