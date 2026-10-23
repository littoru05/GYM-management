package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.VoucherValidateRequestDto;
import com.example.GYM_management_api.dtos.VoucherValidateResponseDto;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IVoucherService;
import com.example.GYM_management_api.utils.TestReportWatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VoucherController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: VoucherController (API Kiểm tra & Áp dụng Voucher)")
class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IVoucherService voucherService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @DisplayName("POST /api/vouchers/validate: Kiểm tra voucher thành công trả về 200 OK")
    void validateVoucher_Success_Returns200() throws Exception {
        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("WELCOME10")
                .orderAmount(new BigDecimal("500000.00"))
                .membershipId(1L)
                .build();

        VoucherValidateResponseDto response = VoucherValidateResponseDto.builder()
                .valid(true)
                .code("WELCOME10")
                .name("Ưu đãi hội viên mới")
                .discountType("PERCENTAGE")
                .discountValue(new BigDecimal("10.00"))
                .discountAmount(new BigDecimal("50000.00"))
                .finalPrice(new BigDecimal("450000.00"))
                .amountAfterDiscount(new BigDecimal("450000.00"))
                .build();

        when(voucherService.validateVoucher(any(VoucherValidateRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.code").value("WELCOME10"))
                .andExpect(jsonPath("$.discountAmount").value(50000.00))
                .andExpect(jsonPath("$.finalPrice").value(450000.00));
    }

    @Test
    @DisplayName("POST /api/vouchers/validate: Mã voucher không tồn tại trả về 404 NOT FOUND")
    void validateVoucher_NotFound_Returns404() throws Exception {
        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("INVALID_CODE")
                .orderAmount(new BigDecimal("500000.00"))
                .build();

        when(voucherService.validateVoucher(any(VoucherValidateRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Mã voucher không tồn tại: INVALID_CODE"));

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Mã voucher không tồn tại: INVALID_CODE"));
    }

    @Test
    @DisplayName("POST /api/vouchers/validate: Voucher hết hạn hoặc chưa đạt giá trị tối thiểu trả về 400 BAD REQUEST")
    void validateVoucher_BadRequest_Returns400() throws Exception {
        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("EXPIRED")
                .orderAmount(new BigDecimal("100000.00"))
                .build();

        when(voucherService.validateVoucher(any(VoucherValidateRequestDto.class)))
                .thenThrow(new BadRequestException("Mã voucher đã hết hạn"));

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Mã voucher đã hết hạn"));
    }

    @Test
    @DisplayName("POST /api/vouchers/validate: Thiếu mã voucher (code rỗng) trả về 400 Validation Error")
    void validateVoucher_EmptyCode_Returns400ValidationError() throws Exception {
        VoucherValidateRequestDto request = VoucherValidateRequestDto.builder()
                .code("")
                .orderAmount(new BigDecimal("500000.00"))
                .build();

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors.code").value("Mã voucher không được để trống"));
    }
}
