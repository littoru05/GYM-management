package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.MembershipDto;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IMembershipService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicMembershipPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: PublicMembershipPlanController (Gói tập công khai)")
class PublicMembershipPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IMembershipService membershipService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("TC-PUBLIC-01: Gọi GET /api/public/membership-plans trả về danh sách gói tập đầy đủ thuộc tính")
    void getPublicMembershipPlans_Returns200WithFullFields() throws Exception {
        MembershipDto plan = MembershipDto.builder()
                .id("1")
                .code("MEMBERSHIP-6M")
                .name("Gói 6 tháng")
                .durationMonths(6)
                .price(BigDecimal.valueOf(1600000))
                .description("Gói tập 6 tháng được hội viên lựa chọn nhiều nhất")
                .imageUrl("https://images.unsplash.com/photo-1540497077202-7c8a3999166f")
                .benefits(List.of("Toàn bộ quyền lợi gói 3 tháng", "Tặng 3 buổi tập cùng HLV"))
                .popular(true)
                .tag("Bán chạy")
                .status("ACTIVE")
                .build();

        when(membershipService.getAllMemberships("ACTIVE")).thenReturn(List.of(plan));

        mockMvc.perform(get("/api/public/membership-plans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Gói 6 tháng"))
                .andExpect(jsonPath("$[0].durationMonths").value(6))
                .andExpect(jsonPath("$[0].price").value(1600000))
                .andExpect(jsonPath("$[0].imageUrl").value("https://images.unsplash.com/photo-1540497077202-7c8a3999166f"))
                .andExpect(jsonPath("$[0].tag").value("Bán chạy"))
                .andExpect(jsonPath("$[0].popular").value(true))
                .andExpect(jsonPath("$[0].benefits[0]").value("Toàn bộ quyền lợi gói 3 tháng"));

        verify(membershipService).getAllMemberships("ACTIVE");
    }

    @Test
    @DisplayName("TC-PUBLIC-02: Gọi GET /api/public/packages alias trả về HTTP 200")
    void getPublicPackagesAlias_Returns200() throws Exception {
        when(membershipService.getAllMemberships("ACTIVE")).thenReturn(List.of());

        mockMvc.perform(get("/api/public/packages")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(membershipService).getAllMemberships("ACTIVE");
    }
}

