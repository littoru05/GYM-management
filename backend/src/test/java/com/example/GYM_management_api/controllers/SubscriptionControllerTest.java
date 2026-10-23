package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.MemberSubscriptionDto;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IMemberSubscriptionService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: SubscriptionController (Tầng Controller - Đăng ký Gói tập)")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IMemberSubscriptionService subscriptionService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

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
    void createSubscription_Returns201() throws Exception {
        MemberSubscriptionDto req = MemberSubscriptionDto.builder().memberId(1L).membershipId(10L).build();
        when(subscriptionService.createSubscription(any(MemberSubscriptionDto.class))).thenReturn(sampleDto);

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUB-0001"));

        verify(subscriptionService).createSubscription(any(MemberSubscriptionDto.class));
    }

    @Test
    @DisplayName("TC-119: Gọi API GET /api/subscriptions trả về HTTP 200 OK với danh sách tất cả hợp đồng")
    void getAllSubscriptions_Returns200() throws Exception {
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SUB-0001"));

        verify(subscriptionService).getAllSubscriptions();
    }

    @Test
    @DisplayName("TC-120: Gọi API GET /api/subscriptions/member/{memberIdOrCode} trả về HTTP 200 OK với lịch sử gói tập của hội viên")
    void getSubscriptionsByMember_Returns200() throws Exception {
        when(subscriptionService.getSubscriptionsByMember("M0001")).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/subscriptions/member/M0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SUB-0001"));

        verify(subscriptionService).getSubscriptionsByMember("M0001");
    }

    @Test
    @DisplayName("TC-121: Gọi API GET /api/subscriptions/{id} trả về HTTP 200 OK khi lấy chi tiết hợp đồng gói tập")
    void getSubscriptionById_Returns200() throws Exception {
        when(subscriptionService.getSubscriptionById(1L)).thenReturn(sampleDto);

        mockMvc.perform(get("/api/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUB-0001"));

        verify(subscriptionService).getSubscriptionById(1L);
    }

    @Test
    @DisplayName("TC-122: Gọi API GET /api/subscriptions/{id} không tìm thấy trả về HTTP 404 NOT FOUND")
    void getSubscriptionById_NotFound_Returns404() throws Exception {
        when(subscriptionService.getSubscriptionById(999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy hợp đồng với ID: 999"));

        mockMvc.perform(get("/api/subscriptions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
