package com.example.GYM_management_api.controllers;

import java.math.BigDecimal;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.GYM_management_api.dtos.MembershipDto;
import com.example.GYM_management_api.services.IMembershipService;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử tầng Controller: MembershipController (Tầng Controller - Quản lý Gói tập)")
class MembershipControllerTest {

    @Mock
    private IMembershipService membershipService;

    @InjectMocks
    private MembershipController membershipController;

    private MembershipDto sampleMembershipDto;

    @BeforeEach
    void setUp() {
        sampleMembershipDto = MembershipDto.builder()
                .id("1")
                .code("PKG-GOLD")
                .name("Gói Gold 12 Tháng")
                .durationMonths(12)
                .price(BigDecimal.valueOf(5000000))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-113: Gọi API GET /api/memberships trả về HTTP 200 OK với danh sách gói tập")
    void getMemberships_Returns200() {
        when(membershipService.getAllMemberships(null)).thenReturn(List.of(sampleMembershipDto));

        ResponseEntity<List<MembershipDto>> response = membershipController.getMemberships(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(membershipService, times(1)).getAllMemberships(null);
    }

    @Test
    @DisplayName("TC-114: Gọi API GET /api/memberships/{id} trả về HTTP 200 OK khi tìm thấy gói tập")
    void getMembershipById_Returns200() {
        when(membershipService.getMembershipById(1L)).thenReturn(sampleMembershipDto);

        ResponseEntity<MembershipDto> response = membershipController.getMembershipById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PKG-GOLD", response.getBody().getCode());
        verify(membershipService, times(1)).getMembershipById(1L);
    }

    @Test
    @DisplayName("TC-115: Gọi API POST /api/memberships trả về HTTP 201 CREATED khi tạo mới gói tập thành công")
    void createMembership_Returns201() {
        MembershipDto req = MembershipDto.builder().name("Gói Mới").durationMonths(3).price(BigDecimal.valueOf(1000000)).build();
        when(membershipService.createMembership(req)).thenReturn(sampleMembershipDto);

        ResponseEntity<MembershipDto> response = membershipController.createMembership(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(membershipService, times(1)).createMembership(req);
    }

    @Test
    @DisplayName("TC-116: Gọi API PUT /api/memberships/{id} trả về HTTP 200 OK khi cập nhật gói tập thành công")
    void updateMembership_Returns200() {
        MembershipDto req = MembershipDto.builder().name("Gói Đổi Tên").build();
        when(membershipService.updateMembership(1L, req)).thenReturn(sampleMembershipDto);

        ResponseEntity<MembershipDto> response = membershipController.updateMembership(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(membershipService, times(1)).updateMembership(1L, req);
    }

    @Test
    @DisplayName("TC-117: Gọi API DELETE /api/memberships/{id} trả về HTTP 200 OK khi xóa gói tập thành công")
    void deleteMembership_Returns200() {
        doNothing().when(membershipService).deleteMembership(1L);

        ResponseEntity<?> response = membershipController.deleteMembership(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(membershipService, times(1)).deleteMembership(1L);
    }
}
