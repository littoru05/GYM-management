package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.MembershipDto;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IMembershipService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MembershipController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: MembershipController (Tầng Controller - Quản lý Gói tập)")
class MembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IMembershipService membershipService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

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
    void getMemberships_Returns200() throws Exception {
        when(membershipService.getAllMemberships(null)).thenReturn(List.of(sampleMembershipDto));

        mockMvc.perform(get("/api/memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("PKG-GOLD"))
                .andExpect(jsonPath("$[0].name").value("Gói Gold 12 Tháng"));

        verify(membershipService).getAllMemberships(null);
    }

    @Test
    @DisplayName("TC-114: Gọi API GET /api/memberships/{id} trả về HTTP 200 OK khi tìm thấy gói tập")
    void getMembershipById_Returns200() throws Exception {
        when(membershipService.getMembershipById(1L)).thenReturn(sampleMembershipDto);

        mockMvc.perform(get("/api/memberships/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PKG-GOLD"))
                .andExpect(jsonPath("$.price").value(5000000));

        verify(membershipService).getMembershipById(1L);
    }

    @Test
    @DisplayName("TC-115: Gọi API POST /api/memberships trả về HTTP 201 CREATED khi tạo mới gói tập thành công")
    void createMembership_Returns201() throws Exception {
        MembershipDto req = MembershipDto.builder().name("Gói Mới").durationMonths(3).price(BigDecimal.valueOf(1000000)).build();
        when(membershipService.createMembership(any(MembershipDto.class))).thenReturn(sampleMembershipDto);

        mockMvc.perform(post("/api/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PKG-GOLD"));

        verify(membershipService).createMembership(any(MembershipDto.class));
    }

    @Test
    @DisplayName("TC-116: Gọi API PUT /api/memberships/{id} trả về HTTP 200 OK khi cập nhật gói tập thành công")
    void updateMembership_Returns200() throws Exception {
        MembershipDto req = MembershipDto.builder().name("Gói Đổi Tên").build();
        when(membershipService.updateMembership(eq(1L), any(MembershipDto.class))).thenReturn(sampleMembershipDto);

        mockMvc.perform(put("/api/memberships/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PKG-GOLD"));

        verify(membershipService).updateMembership(eq(1L), any(MembershipDto.class));
    }

    @Test
    @DisplayName("TC-117: Gọi API DELETE /api/memberships/{id} trả về HTTP 200 OK khi xóa gói tập thành công")
    void deleteMembership_Returns200() throws Exception {
        doNothing().when(membershipService).deleteMembership(1L);

        mockMvc.perform(delete("/api/memberships/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(membershipService).deleteMembership(1L);
    }

    @Test
    @DisplayName("TC-118: Gọi API GET /api/memberships/{id} không tìm thấy trả về HTTP 404 NOT FOUND")
    void getMembershipById_NotFound_Returns404() throws Exception {
        when(membershipService.getMembershipById(999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy gói tập với ID: 999"));

        mockMvc.perform(get("/api/memberships/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy gói tập với ID: 999"));
    }
}
