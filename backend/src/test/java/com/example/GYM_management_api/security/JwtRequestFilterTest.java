package com.example.GYM_management_api.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRequestFilterTest {

    @Mock
    private AppUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Tài khoản hợp lệ và hoạt động bình thường - Thiết lập Authentication và cho qua Filter")
    void shouldAuthenticate_whenValidTokenAndActiveAccount() throws Exception {
        String token = "valid-token";
        String email = "staff@gym.local";

        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/api/pos/checkout");

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromJWT(token)).thenReturn(email);

        UserDetails userDetails = new User(
                email, "password", true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"))
        );
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        jwtRequestFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Tài khoản bị khóa (accountNonLocked = false) - Chặn ngay lập tức trả về 401 Unauthorized")
    void shouldBlockAndReturn401_whenAccountIsLocked() throws Exception {
        String token = "valid-token-locked-user";
        String email = "lockedstaff@gym.local";

        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/api/pos/checkout");

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromJWT(token)).thenReturn(email);

        // accountNonLocked = false
        UserDetails lockedUser = new User(
                email, "password", true, true, true, false,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"))
        );
        when(userDetailsService.loadUserByUsername(email)).thenReturn(lockedUser);

        jwtRequestFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Tài khoản của bạn đã bị khóa");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Tài khoản chưa kích hoạt / ngưng hoạt động (enabled = false) - Chặn ngay lập tức trả về 401 Unauthorized")
    void shouldBlockAndReturn401_whenAccountIsDisabled() throws Exception {
        String token = "valid-token-disabled-user";
        String email = "inactive@gym.local";

        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/api/subscriptions");

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromJWT(token)).thenReturn(email);

        // enabled = false
        UserDetails disabledUser = new User(
                email, "password", false, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"))
        );
        when(userDetailsService.loadUserByUsername(email)).thenReturn(disabledUser);

        jwtRequestFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Tài khoản chưa được kích hoạt hoặc đã ngưng hoạt động");
        verify(filterChain, never()).doFilter(request, response);
    }
}
