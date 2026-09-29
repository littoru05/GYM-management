package com.example.GYM_management_api.controllers;

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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.GYM_management_api.dtos.MemberDto;
import com.example.GYM_management_api.dtos.SubscriptionRequestDto;
import com.example.GYM_management_api.services.IMemberService;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử tầng Controller: MemberController (Tầng Controller - Quản lý Hội viên)")
class MemberControllerTest {

    @Mock
    private IMemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private MemberDto sampleMemberDto;

    @BeforeEach
    void setUp() {
        sampleMemberDto = MemberDto.builder()
                .id("1")
                .code("M0001")
                .name("Nguyen Van A")
                .phone("0900000001")
                .email("vana@gym.local")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-107: Gọi API GET /api/members trả về HTTP 200 OK với danh sách phân trang")
    void getMembers_Returns200() {
        Page<MemberDto> page = new PageImpl<>(List.of(sampleMemberDto));
        when(memberService.getMembers(0, 10, null, null)).thenReturn(page);

        ResponseEntity<Page<MemberDto>> response = memberController.getMembers(0, 10, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(memberService, times(1)).getMembers(0, 10, null, null);
    }

    @Test
    @DisplayName("TC-108: Gọi API GET /api/members/{id} trả về HTTP 200 OK khi tìm thấy hội viên")
    void getMemberById_Returns200() {
        when(memberService.getMemberByIdOrCode("1")).thenReturn(sampleMemberDto);

        ResponseEntity<MemberDto> response = memberController.getMemberById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("M0001", response.getBody().getCode());
        verify(memberService, times(1)).getMemberByIdOrCode("1");
    }

    @Test
    @DisplayName("TC-109: Gọi API POST /api/members trả về HTTP 201 CREATED khi tạo mới hội viên thành công")
    void createMember_Returns201() {
        MemberDto request = MemberDto.builder().name("Tran Thi B").phone("0912345678").build();
        when(memberService.createMember(request)).thenReturn(sampleMemberDto);

        ResponseEntity<MemberDto> response = memberController.createMember(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().getId());
        verify(memberService, times(1)).createMember(request);
    }

    @Test
    @DisplayName("TC-110: Gọi API PUT /api/members/{id} trả về HTTP 200 OK khi cập nhật hội viên thành công")
    void updateMember_Returns200() {
        MemberDto request = MemberDto.builder().name("Nguyen Van A Updated").build();
        when(memberService.updateMember(1L, request)).thenReturn(sampleMemberDto);

        ResponseEntity<MemberDto> response = memberController.updateMember(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(memberService, times(1)).updateMember(1L, request);
    }

    @Test
    @DisplayName("TC-111: Gọi API DELETE /api/members/{id} trả về HTTP 200 OK khi xóa/khóa hội viên thành công")
    void deleteMember_Returns200() {
        doNothing().when(memberService).deleteMember(1L);

        ResponseEntity<?> response = memberController.deleteMember(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(memberService, times(1)).deleteMember(1L);
    }

    @Test
    @DisplayName("TC-112: Gọi API POST /api/members/{id}/subscriptions trả về HTTP 200 OK khi kích hoạt gói tập")
    void registerSubscription_Returns200() {
        SubscriptionRequestDto request = new SubscriptionRequestDto(10L, LocalDate.now(), "Dang ky");
        when(memberService.registerSubscription(1L, request)).thenReturn(sampleMemberDto);

        ResponseEntity<MemberDto> response = memberController.registerSubscription(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(memberService, times(1)).registerSubscription(1L, request);
    }
}
