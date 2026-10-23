package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.AuthResponseDto;
import com.example.GYM_management_api.dtos.LoginDto;
import com.example.GYM_management_api.dtos.RegisterDto;
import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("POST /api/auth/login - Đăng nhập thành công trả về token (200 OK)")
    void shouldReturn200AndToken_whenValidLoginCredentials() throws Exception {
        LoginDto req = LoginDto.builder()
                .email("admin@gym.local")
                .password("123456")
                .build();

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token("mock-jwt-token")
                .code("STF001")
                .name("Quản trị viên")
                .email("admin@gym.local")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();

        when(authService.login(any(LoginDto.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.email").value("admin@gym.local"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Sai thông tin đăng nhập trả về 401 Unauthorized theo ErrorResponseDto")
    void shouldReturn401_whenBadCredentials() throws Exception {
        LoginDto req = LoginDto.builder()
                .email("admin@gym.local")
                .password("wrongpassword")
                .build();

        when(authService.login(any(LoginDto.class)))
                .thenThrow(new BadCredentialsException("Email hoặc mật khẩu không chính xác"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Email hoặc mật khẩu không chính xác"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login - Validation DTO thất bại (để trống email, password) trả về 400 Bad Request")
    void shouldReturn400_whenLoginDtoInvalid() throws Exception {
        LoginDto invalidReq = LoginDto.builder()
                .email("")
                .password("")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại."))
                .andExpect(jsonPath("$.errors.email").value("Email không được để trống"))
                .andExpect(jsonPath("$.errors.password").value("Mật khẩu không được để trống"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Validation DTO thất bại (mật khẩu quá ngắn) trả về 400 Bad Request")
    void shouldReturn400_whenRegisterPasswordTooShort() throws Exception {
        RegisterDto req = RegisterDto.builder()
                .fullName("Nguyen Van A")
                .email("test@gym.com")
                .phone("0901234567")
                .password("123") // Dưới 6 ký tự
                .role(StaffRole.STAFF)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.password").value("Mật khẩu tối thiểu 6 ký tự"));
    }

    @Test
    @DisplayName("GET /api/auth/me - Đã xác thực thành công lấy thông tin tài khoản hiện tại")
    @WithMockUser(username = "admin@gym.local", roles = {"ADMIN"})
    void shouldReturn200_whenCurrentUserAuthenticated() throws Exception {
        AuthResponseDto currentUser = AuthResponseDto.builder()
                .code("STF001")
                .name("Quản trị viên")
                .email("admin@gym.local")
                .role(StaffRole.ADMIN)
                .build();

        when(authService.getCurrentUser("admin@gym.local")).thenReturn(currentUser);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Quản trị viên"))
                .andExpect(jsonPath("$.email").value("admin@gym.local"));
    }

    @Test
    @DisplayName("GET /api/auth/me - Người dùng không tồn tại trong DB trả về 404 Not Found theo ErrorResponseDto")
    @WithMockUser(username = "ghost@gym.local", roles = {"STAFF"})
    void shouldReturn404_whenUserNotFoundInDb() throws Exception {
        when(authService.getCurrentUser("ghost@gym.local"))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy thông tin tài khoản: ghost@gym.local"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy thông tin tài khoản: ghost@gym.local"))
                .andExpect(jsonPath("$.path").value("/api/auth/me"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Tài khoản bị khóa (LOCKED) trả về 403 Forbidden theo ErrorResponseDto")
    void shouldReturn403_whenAccountIsLocked() throws Exception {
        LoginDto req = LoginDto.builder()
                .email("lockedstaff@gym.local")
                .password("123456")
                .build();

        when(authService.login(any(LoginDto.class)))
                .thenThrow(new org.springframework.security.authentication.LockedException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
