package com.example.GYM_management_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Endpoint kiểm tra trạng thái hoạt động của hệ thống")
public class HealthController {

    @GetMapping
    @Operation(summary = "Kiểm tra tình trạng service", description = "Trả về trạng thái hoạt động của ứng dụng cho Load Balancer / Monitoring")
    public ResponseEntity<Map<String, String>> checkHealth() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
