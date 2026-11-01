package com.example.GYM_management_api.exceptions;

import com.example.GYM_management_api.security.JwtRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestValidationDto {
        @NotBlank(message = "Tên không được để trống")
        private String name;
    }

    @RestController
    @RequestMapping("/test-api")
    static class TestController {

        @PostMapping("/validation")
        public String testValidation(@Valid @RequestBody TestValidationDto dto) {
            return "OK";
        }

        @GetMapping("/not-found")
        public String testNotFound() {
            throw new ResourceNotFoundException("Không tìm thấy hội viên với mã HV001");
        }

        @GetMapping("/bad-request")
        public String testBadRequest() {
            throw new BadRequestException("Voucher đã hết hạn sử dụng");
        }

        @GetMapping("/insufficient-stock")
        public String testInsufficientStock() {
            throw new InsufficientStockException("Sản phẩm không đủ số lượng tồn kho");
        }

        @GetMapping("/illegal-argument")
        public String testIllegalArgument() {
            throw new IllegalArgumentException("Tham số đầu vào không hợp lệ");
        }

        @GetMapping("/duplicate")
        public String testDuplicate() {
            throw new DuplicateResourceException("Số điện thoại đã tồn tại trong hệ thống");
        }

        @GetMapping("/data-integrity")
        public String testDataIntegrity() {
            throw new DataIntegrityViolationException("phone unique constraint violation");
        }

        @GetMapping("/access-denied")
        public String testAccessDenied() {
            throw new AccessDeniedException("Forbidden action");
        }

        @GetMapping("/locked")
        public String testLocked() {
            throw new LockedException("Tài khoản đã bị tạm khóa");
        }

        @GetMapping("/bad-credentials")
        public String testBadCredentials() {
            throw new BadCredentialsException("Sai thông tin đăng nhập");
        }

        @GetMapping("/param")
        public String testParam(@RequestParam String requiredParam) {
            return "Param: " + requiredParam;
        }

        @GetMapping("/type-mismatch")
        public String testTypeMismatch(@RequestParam Integer number) {
            return "Number: " + number;
        }

        @GetMapping("/server-error")
        public String testServerError() {
            throw new RuntimeException("Lỗi kết nối cơ sở dữ liệu nội bộ nghiêm trọng!");
        }
    }

    @Test
    @DisplayName("400 Bad Request - Validation DTO thất bại trả về errors map chuẩn")
    void shouldReturn400WithErrorsMap_whenValidationFails() throws Exception {
        TestValidationDto invalidDto = new TestValidationDto("");

        mockMvc.perform(post("/test-api/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại."))
                .andExpect(jsonPath("$.errors.name").value("Tên không được để trống"))
                .andExpect(jsonPath("$.path").value("/test-api/validation"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("404 Not Found - ResourceNotFoundException trả về đúng thông điệp")
    void shouldReturn404_whenResourceNotFound() throws Exception {
        mockMvc.perform(get("/test-api/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy hội viên với mã HV001"))
                .andExpect(jsonPath("$.path").value("/test-api/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("400 Bad Request - BadRequestException (vi phạm logic/hết hạn voucher)")
    void shouldReturn400_whenBadRequestException() throws Exception {
        mockMvc.perform(get("/test-api/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Voucher đã hết hạn sử dụng"))
                .andExpect(jsonPath("$.path").value("/test-api/bad-request"));
    }

    @Test
    @DisplayName("400 Bad Request - InsufficientStockException (hết hàng/không đủ tồn kho)")
    void shouldReturn400_whenInsufficientStockException() throws Exception {
        mockMvc.perform(get("/test-api/insufficient-stock"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Sản phẩm không đủ số lượng tồn kho"));
    }

    @Test
    @DisplayName("400 Bad Request - IllegalArgumentException")
    void shouldReturn400_whenIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test-api/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Tham số đầu vào không hợp lệ"));
    }

    @Test
    @DisplayName("409 Conflict - DuplicateResourceException")
    void shouldReturn409_whenDuplicateResourceException() throws Exception {
        mockMvc.perform(get("/test-api/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Số điện thoại đã tồn tại trong hệ thống"));
    }

    @Test
    @DisplayName("409 Conflict - DataIntegrityViolationException")
    void shouldReturn409_whenDataIntegrityViolationException() throws Exception {
        mockMvc.perform(get("/test-api/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Số điện thoại này đã tồn tại trong hệ thống."));
    }

    @Test
    @DisplayName("403 Forbidden - AccessDeniedException")
    void shouldReturn403_whenAccessDeniedException() throws Exception {
        mockMvc.perform(get("/test-api/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Bạn không có quyền thực hiện thao tác này. Quyền yêu cầu bị từ chối."));
    }

    @Test
    @DisplayName("403 Forbidden - LockedException (tài khoản bị khóa)")
    void shouldReturn403_whenLockedException() throws Exception {
        mockMvc.perform(get("/test-api/locked"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Tài khoản đã bị tạm khóa"));
    }

    @Test
    @DisplayName("401 Unauthorized - BadCredentialsException")
    void shouldReturn401_whenBadCredentialsException() throws Exception {
        mockMvc.perform(get("/test-api/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Sai thông tin đăng nhập"));
    }

    @Test
    @DisplayName("400 Bad Request - MissingServletRequestParameterException (thiếu query param)")
    void shouldReturn400_whenMissingServletRequestParameter() throws Exception {
        mockMvc.perform(get("/test-api/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("requiredParam")));
    }

    @Test
    @DisplayName("400 Bad Request - MethodArgumentTypeMismatchException (sai kiểu dữ liệu param)")
    void shouldReturn400_whenTypeMismatch() throws Exception {
        mockMvc.perform(get("/test-api/type-mismatch").param("number", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("number")));
    }

    @Test
    @DisplayName("500 Internal Server Error - Ẩn 100% stack trace, chỉ trả thông điệp an toàn chuẩn hóa")
    void shouldReturn500WithoutStackTrace_whenUnhandledException() throws Exception {
        mockMvc.perform(get("/test-api/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Đã xảy ra lỗi hệ thống. Vui lòng liên hệ quản trị viên."))
                .andExpect(jsonPath("$.path").value("/test-api/server-error"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.message", not(containsString("nghiêm trọng"))));
    }
}
