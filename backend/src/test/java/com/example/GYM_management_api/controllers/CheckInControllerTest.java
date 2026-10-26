package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.CheckInDto;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.ICheckInService;
import com.example.GYM_management_api.utils.TestReportWatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CheckInController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: CheckInController (Tầng Controller - Điểm danh / Check-in)")
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ICheckInService checkInService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

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
    void checkIn_Returns200() throws Exception {
        CheckInDto req = CheckInDto.builder().phone("0900000001").build();
        when(checkInService.checkIn(any(CheckInDto.class))).thenReturn(sampleDto);

        mockMvc.perform(post("/api/check-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CI-0001"))
                .andExpect(jsonPath("$.memberName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(checkInService).checkIn(any(CheckInDto.class));
    }

    @Test
    @DisplayName("TC-123: Gọi API GET /api/check-ins/today trả về HTTP 200 OK với danh sách check-in hôm nay")
    void getTodayCheckIns_Returns200() throws Exception {
        when(checkInService.getTodayCheckIns()).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/check-ins/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CI-0001"))
                .andExpect(jsonPath("$[0].memberName").value("Nguyen Van A"));

        verify(checkInService).getTodayCheckIns();
    }

    @Test
    @DisplayName("TC-124: Gọi API GET /api/check-ins trả về HTTP 200 OK với toàn bộ lịch sử điểm danh")
    void getAllCheckIns_Returns200() throws Exception {
        when(checkInService.getAllCheckIns()).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/check-ins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CI-0001"));

        verify(checkInService).getAllCheckIns();
    }

    @Test
    @DisplayName("TC-125: Gọi API POST /api/check-ins khi gói tập hết hạn trả về HTTP 400 BAD REQUEST")
    void checkIn_ExpiredMembership_Returns400() throws Exception {
        CheckInDto req = CheckInDto.builder().phone("0900000001").build();
        when(checkInService.checkIn(any(CheckInDto.class)))
                .thenThrow(new BadRequestException("Gói tập của hội viên đã hết hạn."));

        mockMvc.perform(post("/api/check-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Gói tập của hội viên đã hết hạn."));
    }
}
