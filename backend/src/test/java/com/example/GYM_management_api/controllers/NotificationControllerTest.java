package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.RecentContactLeadDto;
import com.example.GYM_management_api.dtos.UnreadCountResponseDto;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.INotificationService;
import com.example.GYM_management_api.utils.TestReportWatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: NotificationController (Thông báo liên hệ)")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private INotificationService notificationService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("TC-133: GET /api/notifications/unread-count trả về HTTP 200 và cấu trúc { unreadCount: N }")
    void getUnreadCount_Returns200() throws Exception {
        UnreadCountResponseDto res = UnreadCountResponseDto.builder()
                .unreadCount(3L)
                .build();

        when(notificationService.getUnreadCount()).thenReturn(res);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));

        verify(notificationService).getUnreadCount();
    }

    @Test
    @DisplayName("TC-134: GET /api/notifications/recent trả về HTTP 200 và danh sách liên hệ mới nhất")
    void getRecentContacts_Returns200() throws Exception {
        RecentContactLeadDto item1 = RecentContactLeadDto.builder()
                .id(1L)
                .name("Nguyen Van A")
                .phone("0901234567")
                .email("a@gmail.com")
                .message("Tu van goi tap 3 thang")
                .createdAt(LocalDateTime.of(2026, 10, 27, 9, 30))
                .isRead(false)
                .status("NEW")
                .build();

        when(notificationService.getRecentContacts(5)).thenReturn(List.of(item1));

        mockMvc.perform(get("/api/notifications/recent")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Nguyen Van A"))
                .andExpect(jsonPath("$[0].phone").value("0901234567"))
                .andExpect(jsonPath("$[0].message").value("Tu van goi tap 3 thang"))
                .andExpect(jsonPath("$[0].isRead").value(false))
                .andExpect(jsonPath("$[0].status").value("NEW"));

        verify(notificationService).getRecentContacts(5);
    }

    @Test
    @DisplayName("TC-138: PATCH /api/notifications/{id}/read trả về HTTP 200 và isRead = true")
    void markAsRead_Returns200() throws Exception {
        RecentContactLeadDto res = RecentContactLeadDto.builder()
                .id(1L)
                .name("Nguyen Van A")
                .phone("0901234567")
                .isRead(true)
                .status("NEW")
                .build();

        when(notificationService.markAsRead(1L)).thenReturn(res);

        mockMvc.perform(patch("/api/notifications/1/read")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isRead").value(true));

        verify(notificationService).markAsRead(1L);
    }

    @Test
    @DisplayName("TC-139: PATCH /api/notifications/read-all trả về HTTP 200 và updatedCount")
    void markAllAsRead_Returns200() throws Exception {
        when(notificationService.markAllAsRead()).thenReturn(3);

        mockMvc.perform(patch("/api/notifications/read-all")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(3))
                .andExpect(jsonPath("$.message").value("Đã đánh dấu tất cả liên hệ là đã đọc"));

        verify(notificationService).markAllAsRead();
    }
}
