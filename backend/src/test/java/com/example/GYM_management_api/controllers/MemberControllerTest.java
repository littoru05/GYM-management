package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.MemberDto;
import com.example.GYM_management_api.dtos.SubscriptionRequestDto;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IMemberService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(controllers = MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: MemberController (Tầng Controller - Quản lý Hội viên)")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IMemberService memberService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

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
    void getMembers_Returns200() throws Exception {
        Page<MemberDto> page = new PageImpl<>(List.of(sampleMemberDto));
        when(memberService.getMembers(0, 10, null, null)).thenReturn(page);

        mockMvc.perform(get("/api/members")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("M0001"))
                .andExpect(jsonPath("$.content[0].name").value("Nguyen Van A"));

        verify(memberService).getMembers(0, 10, null, null);
    }

    @Test
    @DisplayName("TC-108: Gọi API GET /api/members/{id} trả về HTTP 200 OK khi tìm thấy hội viên")
    void getMemberById_Returns200() throws Exception {
        when(memberService.getMemberByIdOrCode("1")).thenReturn(sampleMemberDto);

        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("M0001"))
                .andExpect(jsonPath("$.phone").value("0900000001"));

        verify(memberService).getMemberByIdOrCode("1");
    }

    @Test
    @DisplayName("TC-109: Gọi API POST /api/members trả về HTTP 201 CREATED khi tạo mới hội viên thành công")
    void createMember_Returns201() throws Exception {
        MemberDto request = MemberDto.builder().name("Tran Thi B").phone("0912345678").build();
        when(memberService.createMember(any(MemberDto.class))).thenReturn(sampleMemberDto);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.code").value("M0001"));

        verify(memberService).createMember(any(MemberDto.class));
    }

    @Test
    @DisplayName("TC-110: Gọi API PUT /api/members/{id} trả về HTTP 200 OK khi cập nhật hội viên thành công")
    void updateMember_Returns200() throws Exception {
        MemberDto request = MemberDto.builder().name("Nguyen Van A Updated").build();
        when(memberService.updateMember(eq(1L), any(MemberDto.class))).thenReturn(sampleMemberDto);

        mockMvc.perform(put("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("M0001"));

        verify(memberService).updateMember(eq(1L), any(MemberDto.class));
    }

    @Test
    @DisplayName("TC-111: Gọi API DELETE /api/members/{id} trả về HTTP 200 OK khi xóa/khóa hội viên thành công")
    void deleteMember_Returns200() throws Exception {
        doNothing().when(memberService).deleteMember(1L);

        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Xử lý xóa hội viên thành công."));

        verify(memberService).deleteMember(1L);
    }

    @Test
    @DisplayName("TC-112: Gọi API POST /api/members/{id}/subscriptions trả về HTTP 200 OK khi kích hoạt gói tập")
    void registerSubscription_Returns200() throws Exception {
        SubscriptionRequestDto request = new SubscriptionRequestDto(10L, LocalDate.now(), "Dang ky");
        when(memberService.registerSubscription(eq(1L), any(SubscriptionRequestDto.class))).thenReturn(sampleMemberDto);

        mockMvc.perform(post("/api/members/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("M0001"));

        verify(memberService).registerSubscription(eq(1L), any(SubscriptionRequestDto.class));
    }

    @Test
    @DisplayName("TC-113: Gọi API GET /api/members/{id} không tìm thấy hội viên trả về HTTP 404 NOT FOUND")
    void getMemberById_NotFound_Returns404() throws Exception {
        when(memberService.getMemberByIdOrCode("999"))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy hội viên với mã hoặc ID: 999"));

        mockMvc.perform(get("/api/members/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy hội viên với mã hoặc ID: 999"));
    }
}
