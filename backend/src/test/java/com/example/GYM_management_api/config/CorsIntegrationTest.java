package com.example.GYM_management_api.config;

import com.example.GYM_management_api.controllers.HealthController;
import com.example.GYM_management_api.security.AppUserDetailsService;
import com.example.GYM_management_api.security.JwtRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthController.class)
@Import({SecurityConfig.class, CorsConfig.class})
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppUserDetailsService userDetailsService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Pre-flight OPTIONS từ http://localhost:5173 - Thành công (200 OK) và trả về CORS headers")
    void shouldAllowPreflight_whenOriginIsLocalhost5173() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS"))
                .andExpect(header().string("Access-Control-Allow-Headers", "Authorization, Content-Type"))
                .andExpect(header().string("Access-Control-Expose-Headers", "Authorization, Content-Disposition"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    @Test
    @DisplayName("Pre-flight OPTIONS từ http://localhost:3000 - Thành công (200 OK)")
    void shouldAllowPreflight_whenOriginIsLocalhost3000() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Pre-flight OPTIONS từ Vercel preview domain (https://*.vercel.app) - Thành công (200 OK)")
    void shouldAllowPreflight_whenOriginIsVercelSubdomain() throws Exception {
        String vercelOrigin = "https://gym-frontend-preview-abc123xyz.vercel.app";
        mockMvc.perform(options("/api/health")
                        .header("Origin", vercelOrigin)
                        .header("Access-Control-Request-Method", "PUT")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", vercelOrigin))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Pre-flight OPTIONS từ domain không được phép - Bị chặn với 403 Forbidden")
    void shouldRejectPreflight_whenOriginIsNotAllowed() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "https://untrusted-website.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Pre-flight OPTIONS từ AWS ALB domain (https://*.elb.amazonaws.com) - Thành công (200 OK)")
    void shouldAllowPreflight_whenOriginIsAlbDomain() throws Exception {
        String albOrigin = "https://gym-staging-alb-123456789.ap-southeast-1.elb.amazonaws.com";
        mockMvc.perform(options("/api/health")
                        .header("Origin", albOrigin)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", albOrigin))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Headers", "Authorization, Content-Type"));
    }

    @Test
    @DisplayName("Pre-flight OPTIONS từ DuckDNS domain (https://*.duckdns.org) - Thành công (200 OK)")
    void shouldAllowPreflight_whenOriginIsDuckDnsDomain() throws Exception {
        String duckDnsOrigin = "https://gym-management-staging.duckdns.org";
        mockMvc.perform(options("/api/health")
                        .header("Origin", duckDnsOrigin)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", duckDnsOrigin))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Thực hiện GET request kèm Origin và Authorization - Thành công (200 OK) và kèm CORS Allow-Origin")
    void shouldAllowActualRequestWithOriginAndAuthorization() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/health")
                        .header("Origin", "http://localhost:5173")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Thực hiện GET request từ Vercel production kèm Authorization header - Thành công (200 OK)")
    void shouldAllowActualRequest_fromVercelWithAuthorizationHeader() throws Exception {
        String vercelOrigin = "https://gym-management.vercel.app";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/health")
                        .header("Origin", vercelOrigin)
                        .header("Authorization", "Bearer sample-jwt-token-from-external"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", vercelOrigin))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
