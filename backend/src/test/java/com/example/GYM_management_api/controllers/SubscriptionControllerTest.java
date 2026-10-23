package com.example.GYM_management_api.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.GYM_management_api.dtos.MemberSubscriptionDto;
import com.example.GYM_management_api.services.IMemberSubscriptionService;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử tầng Controller: SubscriptionController (Tầng Controller - Đăng ký Gói tập)")
class SubscriptionControllerTest {

    @Mock
    private IMemberSubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private MemberSubscriptionDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = MemberSubscriptionDto.builder()
                .id(1L)
                .code("SUB-0001")
                .memberId(1L)
                .memberCode("M0001")
                .membershipId(10L)
                .membershipName("Gói 1 Tháng")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .paidPrice(BigDecimal.valueOf(500000))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-118: Gọi API POST /api/subscriptions trả về HTTP 201 CREATED khi kích hoạt gói tập thành công")
    void createSubscription_Returns201() {
        MemberSubscriptionDto req = MemberSubscriptionDto.builder().memberId(1L).membershipId(10L).build();
        when(subscriptionService.createSubscription(req)).thenReturn(sampleDto);

        ResponseEntity<MemberSubscriptionDto> response = subscriptionController.createSubscription(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUB-0001", response.getBody().getCode());
        verify(subscriptionService, times(1)).createSubscription(req);
    }

    @Test
    @DisplayName("TC-119: Gọi API GET /api/subscriptions trả về HTTP 200 OK với danh sách tất cả hợp đồng")
    void getAllSubscriptions_Returns200() {
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(sampleDto));

        ResponseEntity<List<MemberSubscriptionDto>> response = subscriptionController.getAllSubscriptions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(subscriptionService, times(1)).getAllSubscriptions();
    }

    @Test
    @DisplayName("TC-120: Gọi API GET /api/subscriptions/member/{memberIdOrCode} trả về HTTP 200 OK với lịch sử gói tập của hội viên")
    void getSubscriptionsByMember_Returns200() {
        when(subscriptionService.getSubscriptionsByMember("M0001")).thenReturn(List.of(sampleDto));

        ResponseEntity<List<MemberSubscriptionDto>> response = subscriptionController.getSubscriptionsByMember("M0001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(subscriptionService, times(1)).getSubscriptionsByMember("M0001");
    }

    @Test
    @DisplayName("TC-121: Gọi API GET /api/subscriptions/{id} trả về HTTP 200 OK khi lấy chi tiết hợp đồng gói tập")
    void getSubscriptionById_Returns200() {
        when(subscriptionService.getSubscriptionById(1L)).thenReturn(sampleDto);

        ResponseEntity<MemberSubscriptionDto> response = subscriptionController.getSubscriptionById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUB-0001", response.getBody().getCode());
        verify(subscriptionService, times(1)).getSubscriptionById(1L);
    }
}
