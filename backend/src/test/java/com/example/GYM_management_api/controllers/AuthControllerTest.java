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
import com.example.GYM_management_api.utils.TestReportWatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: AuthController (Tầng Controller - Xác thực & Phân quyền)")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("TC-30: Gọi API POST /api/auth/login thành công trả về HTTP 200 OK")
    void login_Success_Returns200() throws Exception {
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
    @DisplayName("TC-31: Gọi API POST /api/auth/login sai thông tin trả về HTTP 401 UNAUTHORIZED")
    void login_BadCredentials_Returns401() throws Exception {
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
    @DisplayName("TC-32: Gọi API POST /api/auth/login tài khoản bị khóa trả về HTTP 403 FORBIDDEN")
    void login_Locked_Returns403() throws Exception {
        LoginDto req = LoginDto.builder()
                .email("lockedstaff@gym.local")
                .password("123456")
                .build();

        when(authService.login(any(LoginDto.class)))
                .thenThrow(new LockedException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."));

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

    @Test
    @DisplayName("TC-33: Gọi API POST /api/auth/login tài khoản chưa kích hoạt trả về HTTP 403 FORBIDDEN")
    void login_Disabled_Returns403() throws Exception {
        LoginDto req = LoginDto.builder()
                .email("disabled@gym.local")
                .password("123456")
                .build();

        when(authService.login(any(LoginDto.class)))
                .thenThrow(new DisabledException("Tài khoản chưa được kích hoạt."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Tài khoản chưa được kích hoạt."))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("TC-34: Gọi API POST /api/auth/logout thành công trả về HTTP 200 OK")
    void logout_Returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công"));

        verify(authService, times(1)).logout();
    }

    @Test
    @DisplayName("TC-35: Gọi API POST /api/auth/register thành công trả về HTTP 201 CREATED")
    void register_Success_Returns201() throws Exception {
        RegisterDto req = RegisterDto.builder()
                .fullName("Nguyen Van A")
                .email("new@gym.local")
                .phone("0901234567")
                .password("123456")
                .role(StaffRole.STAFF)
                .build();

        Map<String, Object> mockResult = Map.of("message", "Đăng ký thành công");
        when(authService.register(any(RegisterDto.class))).thenReturn(mockResult);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Đăng ký thành công"));
    }

    @Test
    @DisplayName("TC-36: Gọi API POST /api/auth/register trùng lặp thông tin trả về HTTP 400 BAD REQUEST")
    void register_IllegalArgument_Returns400() throws Exception {
        RegisterDto req = RegisterDto.builder()
                .fullName("Nguyen Van A")
                .email("exist@gym.local")
                .phone("0901234567")
                .password("123456")
                .role(StaffRole.STAFF)
                .build();

        when(authService.register(any(RegisterDto.class)))
                .thenThrow(new IllegalArgumentException("Email đã tồn tại"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Email đã tồn tại"));
    }

    @Test
    @DisplayName("TC-37: Gọi API GET /api/auth/me khi đã đăng nhập trả về HTTP 200 OK")
    @WithMockUser(username = "admin@gym.local", roles = {"ADMIN"})
    void getCurrentUser_Authenticated_Returns200() throws Exception {
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
    @DisplayName("TC-38: Gọi API GET /api/auth/me khi chưa đăng nhập trả về HTTP 401 UNAUTHORIZED")
    void getCurrentUser_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("TC-39: Gọi API GET /api/admin/test kiểm tra quyền ADMIN trả về HTTP 200 OK")
    void testAdminRole_Returns200() throws Exception {
        mockMvc.perform(get("/api/admin/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Bạn là ADMIN - Đã truy cập tài nguyên quản trị thành công!"));
    }

    @Test
    @DisplayName("Validation: POST /api/auth/login - Để trống email hoặc password trả về 400 Bad Request")
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
    @DisplayName("Validation: POST /api/auth/register - Mật khẩu dưới 6 ký tự trả về 400 Bad Request")
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
    @DisplayName("Error Handling: GET /api/auth/me - Tài khoản không tồn tại trong DB trả về 404 Not Found")
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
}
