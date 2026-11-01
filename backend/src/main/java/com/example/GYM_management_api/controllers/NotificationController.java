package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.RecentContactLeadDto;
import com.example.GYM_management_api.dtos.UnreadCountResponseDto;
import com.example.GYM_management_api.services.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Thông báo (Notifications)")
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping("/unread-count")
    @Operation(summary = "Lấy số lượng liên hệ chưa đọc")
    public ResponseEntity<UnreadCountResponseDto> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @GetMapping("/recent")
    @Operation(summary = "Lấy danh sách liên hệ mới nhất")
    public ResponseEntity<List<RecentContactLeadDto>> getRecentContacts(
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(notificationService.getRecentContacts(limit));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Đánh dấu một liên hệ là đã đọc")
    public ResponseEntity<RecentContactLeadDto> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả liên hệ là đã đọc")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        int count = notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tất cả liên hệ là đã đọc",
                "updatedCount", count
        ));
    }
}
