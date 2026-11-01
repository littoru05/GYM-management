package com.example.GYM_management_api.config;

import com.example.GYM_management_api.controllers.AuthController;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.security.AppUserDetailsService;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, CorsConfig.class, GlobalExceptionHandler.class})
class SecurityErrorResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppUserDetailsService userDetailsService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        // Cho phép request đi qua mock JwtRequestFilter mà không can thiệp SecurityContext
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("401 Unauthorized - Truy cập endpoint bảo vệ khi chưa đăng nhập trả về ErrorResponseDto chuẩn")
    void shouldReturn401Json_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Phiên đăng nhập không hợp lệ hoặc đã hết hạn."))
                .andExpect(jsonPath("$.path").value("/api/auth/me"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("403 Forbidden - Quyền STAFF truy cập endpoint ADMIN (/api/admin/test) trả về ErrorResponseDto chuẩn")
    @WithMockUser(username = "staff@gym.local", roles = {"STAFF"})
    void shouldReturn403Json_whenForbiddenRole() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Bạn không có quyền truy cập tài nguyên này."))
                .andExpect(jsonPath("$.path").value("/api/admin/test"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
