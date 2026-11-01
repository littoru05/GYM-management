package com.example.GYM_management_api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    @DisplayName("CORS Source: Hỗ trợ danh sách nhiều origin từ chuỗi comma-separated")
    void testCorsConfigurationSource_MultiOrigins() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "frontendOrigin", "http://localhost:5173, http://localhost:3000, http://localhost:80");

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/reports/revenue");
        request.addHeader("Origin", "http://localhost:3000");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOriginPatterns()).contains(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:80"
        );
        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    @DisplayName("CORS Source: Fallback danh sách origin mặc định khi chuỗi rỗng")
    void testCorsConfigurationSource_DefaultFallback() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "frontendOrigin", "");

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        request.addHeader("Origin", "http://localhost:5173");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOriginPatterns()).contains(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:80",
                "https://*.vercel.app",
                "https://*.elb.amazonaws.com",
                "https://*.duckdns.org"
        );
        assertThat(config.getAllowCredentials()).isTrue();
    }
}
