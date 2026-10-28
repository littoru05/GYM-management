package com.example.GYM_management_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.example.GYM_management_api.dtos.RecentContactLeadDto;
import com.example.GYM_management_api.dtos.UnreadCountResponseDto;
import com.example.GYM_management_api.entities.ContactLead;
import com.example.GYM_management_api.entities.enums.LeadStatus;
import com.example.GYM_management_api.repositories.ContactLeadRepository;
import com.example.GYM_management_api.repositories.NotificationRepository;
import com.example.GYM_management_api.services.impl.NotificationServiceImpl;
import com.example.GYM_management_api.utils.TestReportWatcher;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ NotificationServiceImpl (Thông báo liên hệ)")
class NotificationServiceImplTest {

    @Mock
    private ContactLeadRepository contactLeadRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("TC-131: Đếm số lượng liên hệ chưa đọc (unreadCount) thành công")
    void getUnreadCount_Success() {
        when(contactLeadRepository.countByIsReadFalse()).thenReturn(3L);

        UnreadCountResponseDto result = notificationService.getUnreadCount();

        assertNotNull(result);
        assertEquals(3L, result.getUnreadCount());
        verify(contactLeadRepository, times(1)).countByIsReadFalse();
    }

    @Test
    @DisplayName("TC-132: Lấy danh sách liên hệ mới nhất thành công")
    void getRecentContacts_Success() {
        ContactLead lead1 = ContactLead.builder()
                .name("Nguyen Van A")
                .phone("0901234567")
                .email("a@gmail.com")
                .message("Tu van goi tap 3 thang")
                .status(LeadStatus.NEW)
                .isRead(false)
                .build();
        lead1.setId(101L);

        ContactLead lead2 = ContactLead.builder()
                .name("Tran Thi B")
                .phone("0912345678")
                .email("b@gmail.com")
                .message("Hoi ve huan luyen vien")
                .status(LeadStatus.NEW)
                .isRead(true)
                .build();
        lead2.setId(102L);

        when(contactLeadRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5)))
                .thenReturn(List.of(lead1, lead2));

        List<RecentContactLeadDto> results = notificationService.getRecentContacts(5);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(101L, results.get(0).getId());
        assertEquals("Nguyen Van A", results.get(0).getName());
        assertEquals("0901234567", results.get(0).getPhone());
        assertFalse(results.get(0).getIsRead());
        assertEquals("NEW", results.get(0).getStatus());

        assertEquals(102L, results.get(1).getId());
        assertEquals("Tran Thi B", results.get(1).getName());
        verify(contactLeadRepository, times(1)).findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("TC-135: Đánh dấu một liên hệ là đã đọc (markAsRead) thành công")
    void markAsRead_Success() {
        ContactLead lead = ContactLead.builder()
                .name("Nguyen Van A")
                .phone("0901234567")
                .email("a@gmail.com")
                .message("Tu van goi tap")
                .status(LeadStatus.NEW)
                .isRead(false)
                .build();
        lead.setId(101L);

        when(contactLeadRepository.findById(101L)).thenReturn(java.util.Optional.of(lead));
        when(contactLeadRepository.save(lead)).thenReturn(lead);

        RecentContactLeadDto result = notificationService.markAsRead(101L);

        assertNotNull(result);
        assertEquals(101L, result.getId());
        org.junit.jupiter.api.Assertions.assertTrue(result.getIsRead());
        verify(contactLeadRepository, times(1)).findById(101L);
        verify(contactLeadRepository, times(1)).save(lead);
    }

    @Test
    @DisplayName("TC-136: Đánh dấu đã đọc thất bại khi không tìm thấy id")
    void markAsRead_NotFound_ThrowsException() {
        when(contactLeadRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.example.GYM_management_api.exceptions.ResourceNotFoundException.class,
                () -> notificationService.markAsRead(999L)
        );

        verify(contactLeadRepository, times(1)).findById(999L);
        verify(contactLeadRepository, times(0)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("TC-137: Đánh dấu tất cả liên hệ là đã đọc (markAllAsRead) thành công")
    void markAllAsRead_Success() {
        when(contactLeadRepository.markAllAsRead()).thenReturn(4);

        int updatedCount = notificationService.markAllAsRead();

        assertEquals(4, updatedCount);
        verify(contactLeadRepository, times(1)).markAllAsRead();
    }
}
