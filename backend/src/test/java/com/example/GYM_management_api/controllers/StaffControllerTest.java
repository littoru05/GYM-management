package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.StaffDto;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IStaffService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StaffController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class StaffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IStaffService staffService;

    @MockitoBean
    private StaffRepository staffRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("GET /api/admin/staffs - Lấy danh sách nhân viên 200 OK")
    void getStaffs_Success() throws Exception {
        StaffDto dto = StaffDto.builder()
                .id("1")
                .code("STF-001")
                .name("Nguyễn Văn A")
                .email("nva@gym.com")
                .role("STAFF")
                .status("ACTIVE")
                .build();

        when(staffService.getStaffs(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(dto)));

        mockMvc.perform(get("/api/admin/staffs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.content[0].email").value("nva@gym.com"));
    }

    @Test
    @DisplayName("GET /api/admin/staffs/{id} - Lấy chi tiết nhân viên thành công")
    void getStaffById_Success() throws Exception {
        StaffDto dto = StaffDto.builder()
                .id("1")
                .code("STF-001")
                .name("Nguyễn Văn A")
                .email("nva@gym.com")
                .build();

        when(staffService.getStaffById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/staffs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Nguyễn Văn A"));
    }

    @Test
    @DisplayName("GET /api/admin/staffs/{id} - Báo 404 khi không tìm thấy nhân viên")
    void getStaffById_NotFound() throws Exception {
        when(staffService.getStaffById(99L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy nhân viên với ID: 99"));

        mockMvc.perform(get("/api/admin/staffs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Không tìm thấy nhân viên với ID: 99"));
    }

    @Test
    @DisplayName("POST /api/admin/staffs - Tạo mới nhân viên 201 Created")
    void createStaff_Success() throws Exception {
        StaffDto request = StaffDto.builder()
                .name("Nguyễn Văn B")
                .email("nvb@gym.com")
                .phone("0987654321")
                .password("secret123")
                .role("STAFF")
                .build();

        StaffDto created = StaffDto.builder()
                .id("2")
                .code("STF-002")
                .name("Nguyễn Văn B")
                .email("nvb@gym.com")
                .phone("0987654321")
                .role("STAFF")
                .status("ACTIVE")
                .build();

        when(staffService.createStaff(any(StaffDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/admin/staffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("2"))
                .andExpect(jsonPath("$.code").value("STF-002"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/admin/staffs - Validation thất bại khi để trống tên hoặc email không hợp lệ")
    void createStaff_ValidationError() throws Exception {
        StaffDto invalidRequest = StaffDto.builder()
                .name("")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/admin/staffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("PUT /api/admin/staffs/{id} - Cập nhật nhân viên thành công 200 OK")
    void updateStaff_Success() throws Exception {
        StaffDto updateRequest = StaffDto.builder()
                .name("Nguyễn Văn B Updated")
                .email("nvb@gym.com")
                .phone("0987654321")
                .role("ADMIN")
                .build();

        when(staffService.updateStaff(eq(2L), any(StaffDto.class), any())).thenReturn(updateRequest);

        mockMvc.perform(put("/api/admin/staffs/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nguyễn Văn B Updated"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin@gym.com", roles = {"ADMIN"})
    @DisplayName("PUT /api/admin/staffs/{id} - Báo lỗi 400 khi tự khóa Admin qua form cập nhật")
    void updateStaff_SelfLock_ReturnsBadRequest() throws Exception {
        StaffDto updateRequest = StaffDto.builder()
                .name("Admin Updated")
                .email("admin@gym.com")
                .phone("0987654321")
                .role("ADMIN")
                .status("LOCKED")
                .build();

        when(staffService.updateStaff(eq(1L), any(StaffDto.class), eq("admin@gym.com")))
                .thenThrow(new BadRequestException("Bạn không thể tự khóa hoặc vô hiệu hóa tài khoản quản trị viên đang đăng nhập."));

        mockMvc.perform(put("/api/admin/staffs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bạn không thể tự khóa hoặc vô hiệu hóa tài khoản quản trị viên đang đăng nhập."));
    }

    @Test
    @WithMockUser(username = "admin@gym.com", roles = {"ADMIN"})
    @DisplayName("PATCH /api/admin/staffs/{id}/toggle-status - Khóa/Mở khóa thành công 200 OK")
    void toggleStaffStatus_Success() throws Exception {
        StaffDto toggled = StaffDto.builder()
                .id("5")
                .code("STF-005")
                .name("Nhân Viên 5")
                .email("staff5@gym.com")
                .status("LOCKED")
                .build();

        when(staffService.toggleStaffStatus(eq(5L), eq("admin@gym.com"))).thenReturn(toggled);

        mockMvc.perform(patch("/api/admin/staffs/5/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"));
    }

    @Test
    @WithMockUser(username = "admin@gym.com", roles = {"ADMIN"})
    @DisplayName("PATCH /api/admin/staffs/{id}/toggle-status - Báo lỗi 400 khi tự khóa Admin đang đăng nhập")
    void toggleStaffStatus_SelfLock_ReturnsBadRequest() throws Exception {
        when(staffService.toggleStaffStatus(eq(1L), eq("admin@gym.com")))
                .thenThrow(new BadRequestException("Bạn không thể tự khóa tài khoản quản trị viên đang đăng nhập."));

        mockMvc.perform(patch("/api/admin/staffs/1/toggle-status"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bạn không thể tự khóa tài khoản quản trị viên đang đăng nhập."));
    }

    @Test
    @WithMockUser(username = "admin@gym.com", roles = {"ADMIN"})
    @DisplayName("DELETE /api/admin/staffs/{id} - Vô hiệu hóa nhân viên thành công")
    void deleteStaff_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/staffs/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Vô hiệu hóa tài khoản nhân viên thành công."));
    }
}
