package com.example.GYM_management_api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tiện ích JWT JwtUtil (Tầng Security)")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long testExpiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", testExpiration);
    }

    @Test
    @DisplayName("TC-20: Tạo JWT Token và trích xuất đúng thông tin Claims")
    void generateToken_And_ExtractClaims() {
        String token = jwtUtil.generateToken(10L, "admin@gym.local", "ADMIN");

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals("admin@gym.local", jwtUtil.getUsernameFromJWT(token));
        assertEquals("admin@gym.local", jwtUtil.extractEmail(token));
        assertEquals(10L, jwtUtil.extractUserId(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
        assertNotNull(jwtUtil.extractExpiration(token));
    }

    @Test
    @DisplayName("TC-21: Xác thực Token hợp lệ trả về true")
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken(1L, "user@gym.local", "STAFF");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("TC-22: Từ chối Token không hợp lệ, sai chữ ký, rỗng hoặc null trả về false")
    void validateToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "header.payload.invalidsignature";
        assertFalse(jwtUtil.validateToken(invalidToken));

        assertFalse(jwtUtil.validateToken(null));
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("TC-23: Từ chối Token đã quá hạn sử dụng trả về false")
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Cấu hình expiration âm để tạo token đã hết hạn
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L);
        String expiredToken = jwtUtil.generateToken(1L, "expired@gym.local", "STAFF");

        assertFalse(jwtUtil.validateToken(expiredToken));
    }
}

