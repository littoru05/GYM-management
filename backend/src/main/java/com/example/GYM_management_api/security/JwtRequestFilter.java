package com.example.GYM_management_api.security;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(jwt)) {
                    username = jwtUtil.getUsernameFromJWT(jwt);
                }
            } catch (Exception ex) {
                log.warn("Cannot extract username from JWT: {}", ex.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Kiểm tra tài khoản có bị khóa không (UC10)
                if (!userDetails.isAccountNonLocked()) {
                    log.warn("Tài khoản {} đã bị khóa (LOCKED). Từ chối thực hiện nghiệp vụ.", username);
                    SecurityContextHolder.clearContext();
                    sendErrorResponse(response, request.getRequestURI(), HttpStatus.UNAUTHORIZED,
                            "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
                    return;
                }

                // Kiểm tra tài khoản có bị ngưng hoạt động / chưa kích hoạt không
                if (!userDetails.isEnabled()) {
                    log.warn("Tài khoản {} chưa được kích hoạt hoặc đã ngưng hoạt động. Từ chối yêu cầu.", username);
                    SecurityContextHolder.clearContext();
                    sendErrorResponse(response, request.getRequestURI(), HttpStatus.UNAUTHORIZED,
                            "Tài khoản chưa được kích hoạt hoặc đã ngưng hoạt động.");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ex) {
                log.warn("Failed to set user authentication: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String uri, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(uri)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorDto));
        response.getWriter().flush();
    }
}
