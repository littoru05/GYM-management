package com.example.GYM_management_api.controllers;

import java.time.LocalDateTime;
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

import com.example.GYM_management_api.dtos.CheckInDto;
import com.example.GYM_management_api.services.ICheckInService;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử tầng Controller: CheckInController (Tầng Controller - Điểm danh / Check-in)")
class CheckInControllerTest {

    @Mock
    private ICheckInService checkInService;

    @InjectMocks
    private CheckInController checkInController;

    private CheckInDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = CheckInDto.builder()
                .id("1")
                .code("CI-0001")
                .memberId("1")
                .memberName("Nguyen Van A")
                .membershipName("Gói 1 Tháng")
                .time(LocalDateTime.now())
                .status("SUCCESS")
                .build();
    }

    @Test
    @DisplayName("TC-122: Gọi API POST /api/check-ins trả về HTTP 200 OK khi điểm danh thành công")
    void checkIn_Returns200() {
        CheckInDto req = CheckInDto.builder().phone("0900000001").build();
        when(checkInService.checkIn(req)).thenReturn(sampleDto);

        ResponseEntity<CheckInDto> response = checkInController.checkIn(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CI-0001", response.getBody().getCode());
        verify(checkInService, times(1)).checkIn(req);
    }

    @Test
    @DisplayName("TC-123: Gọi API GET /api/check-ins/today trả về HTTP 200 OK với danh sách check-in hôm nay")
    void getTodayCheckIns_Returns200() {
        when(checkInService.getTodayCheckIns()).thenReturn(List.of(sampleDto));

        ResponseEntity<List<CheckInDto>> response = checkInController.getTodayCheckIns();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(checkInService, times(1)).getTodayCheckIns();
    }

    @Test
    @DisplayName("TC-124: Gọi API GET /api/check-ins trả về HTTP 200 OK với toàn bộ lịch sử điểm danh")
    void getAllCheckIns_Returns200() {
        when(checkInService.getAllCheckIns()).thenReturn(List.of(sampleDto));

        ResponseEntity<List<CheckInDto>> response = checkInController.getAllCheckIns();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(checkInService, times(1)).getAllCheckIns();
    }
}
