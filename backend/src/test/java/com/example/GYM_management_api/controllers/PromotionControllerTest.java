package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.PromotionDto;
import com.example.GYM_management_api.dtos.VoucherApplyRequestDto;
import com.example.GYM_management_api.dtos.VoucherApplyResponseDto;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IPromotionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import com.example.GYM_management_api.utils.TestReportWatcher;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IPromotionService promotionService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    // =========================================================================
    // 1. OPERATION TESTS: /api/promotions/apply & /api/promotions/active
    // =========================================================================

    @Test
    @DisplayName("POST /api/promotions/apply: Thành công khi mã và đơn hàng hợp lệ")
    void applyPromotion_success() throws Exception {
        VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                .code("SUMMER20")
                .orderAmount(BigDecimal.valueOf(500000))
                .build();

        VoucherApplyResponseDto response = VoucherApplyResponseDto.builder()
                .valid(true)
                .code("SUMMER20")
                .name("Khuyến mãi chào hè")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(20))
                .orderAmount(BigDecimal.valueOf(500000))
                .discountAmount(BigDecimal.valueOf(100000))
                .finalAmount(BigDecimal.valueOf(400000))
                .message("Áp dụng mã giảm giá thành công.")
                .build();

        when(promotionService.applyVoucher(any(VoucherApplyRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/promotions/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.code").value("SUMMER20"))
                .andExpect(jsonPath("$.discountAmount").value(100000))
                .andExpect(jsonPath("$.finalAmount").value(400000));
    }

    @Test
    @DisplayName("POST /api/promotions/apply: Trả về 404 khi mã không tồn tại")
    void applyPromotion_notFound() throws Exception {
        VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                .code("INVALID")
                .orderAmount(BigDecimal.valueOf(500000))
                .build();

        when(promotionService.applyVoucher(any(VoucherApplyRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Mã giảm giá 'INVALID' không tồn tại trong hệ thống."));

        mockMvc.perform(post("/api/promotions/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Mã giảm giá 'INVALID' không tồn tại trong hệ thống."));
    }

    @Test
    @DisplayName("POST /api/promotions/apply: Trả về 400 khi mã hết hạn hoặc quá số lượt")
    void applyPromotion_expiredOrOverLimit() throws Exception {
        VoucherApplyRequestDto request = VoucherApplyRequestDto.builder()
                .code("EXPIRED")
                .orderAmount(BigDecimal.valueOf(500000))
                .build();

        when(promotionService.applyVoucher(any(VoucherApplyRequestDto.class)))
                .thenThrow(new BadRequestException("Mã giảm giá 'EXPIRED' đã hết hạn sử dụng."));

        mockMvc.perform(post("/api/promotions/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mã giảm giá 'EXPIRED' đã hết hạn sử dụng."));
    }

    @Test
    @DisplayName("POST /api/promotions/apply: Trả về 400 khi thiếu thông tin bắt buộc (Bean validation)")
    void applyPromotion_beanValidationFailed() throws Exception {
        VoucherApplyRequestDto invalidRequest = VoucherApplyRequestDto.builder()
                .code("") // Blank code
                .orderAmount(null)
                .build();

        mockMvc.perform(post("/api/promotions/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").exists())
                .andExpect(jsonPath("$.errors.orderAmount").exists());
    }

    @Test
    @DisplayName("GET /api/promotions/active: Lấy danh sách voucher khả dụng")
    void getActivePromotions_success() throws Exception {
        when(promotionService.getAllActivePromotions()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // =========================================================================
    // 2. ADMIN CRUD TESTS: /api/admin/promotions
    // =========================================================================

    @Test
    @DisplayName("GET /api/admin/promotions: Phân trang danh sách voucher")
    void getPromotions_success() throws Exception {
        PromotionDto dto = PromotionDto.builder()
                .id("1")
                .code("SUMMER20")
                .name("Ưu đãi hè")
                .build();
        Page<PromotionDto> page = new PageImpl<>(Collections.singletonList(dto));

        when(promotionService.getPromotions(0, 10, null, null)).thenReturn(page);

        mockMvc.perform(get("/api/admin/promotions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("SUMMER20"));
    }

    @Test
    @DisplayName("GET /api/admin/promotions/{id}: Lấy chi tiết voucher theo ID")
    void getPromotionById_success() throws Exception {
        PromotionDto dto = PromotionDto.builder()
                .id("1")
                .code("SUMMER20")
                .name("Ưu đãi hè")
                .build();

        when(promotionService.getPromotionById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.code").value("SUMMER20"));
    }

    @Test
    @DisplayName("POST /api/admin/promotions: Tạo mới voucher thành công")
    void createPromotion_success() throws Exception {
        PromotionDto request = PromotionDto.builder()
                .code("SUMMER20")
                .name("Ưu đãi hè")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(20))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .usageLimit(100)
                .build();

        PromotionDto created = PromotionDto.builder()
                .id("1")
                .code("SUMMER20")
                .name("Ưu đãi hè")
                .build();

        when(promotionService.createPromotion(any(PromotionDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.code").value("SUMMER20"));
    }

    @Test
    @DisplayName("POST /api/admin/promotions: Trả về 409 khi mã voucher đã tồn tại")
    void createPromotion_duplicateCode() throws Exception {
        PromotionDto request = PromotionDto.builder()
                .code("DUPLICATE")
                .name("Mã trùng")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(20))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .usageLimit(100)
                .build();

        when(promotionService.createPromotion(any(PromotionDto.class)))
                .thenThrow(new DuplicateResourceException("Mã khuyến mãi 'DUPLICATE' đã tồn tại trong hệ thống."));

        mockMvc.perform(post("/api/admin/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Mã khuyến mãi 'DUPLICATE' đã tồn tại trong hệ thống."));
    }

    @Test
    @DisplayName("PUT /api/admin/promotions/{id}: Cập nhật voucher thành công")
    void updatePromotion_success() throws Exception {
        PromotionDto request = PromotionDto.builder()
                .code("SUMMER20")
                .name("Ưu đãi hè 2026")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(25))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .usageLimit(150)
                .build();

        PromotionDto updated = PromotionDto.builder()
                .id("1")
                .code("SUMMER20")
                .name("Ưu đãi hè 2026")
                .build();

        when(promotionService.updatePromotion(eq(1L), any(PromotionDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/admin/promotions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ưu đãi hè 2026"));
    }

    @Test
    @DisplayName("DELETE /api/admin/promotions/{id}: Xóa voucher thành công trả về 204")
    void deletePromotion_success() throws Exception {
        doNothing().when(promotionService).deletePromotion(1L);

        mockMvc.perform(delete("/api/admin/promotions/1"))
                .andExpect(status().isNoContent());
    }
}
