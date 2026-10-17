package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.CheckoutRequestDTO;
import com.example.GYM_management_api.dtos.OrderItemRequestDTO;
import com.example.GYM_management_api.dtos.OrderItemResponseDTO;
import com.example.GYM_management_api.dtos.OrderResponseDTO;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.PaymentMethod;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.services.IPosOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PosOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PosOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IPosOrderService posOrderService;

    @MockitoBean
    private StaffRepository staffRepository;

    @MockitoBean
    private com.example.GYM_management_api.security.JwtRequestFilter jwtRequestFilter;

    // =========================================================================
    // 1. BEAN VALIDATION TEST CASES (@Valid ON CheckoutRequestDTO)
    // =========================================================================

    @Test
    @DisplayName("Validation: Gửi giỏ hàng rỗng (items: []) -> 400 Bad Request")
    void checkout_EmptyItems_Returns400BadRequest() throws Exception {
        String requestJson = """
                {
                    "items": [],
                    "paymentMethod": "CASH"
                }
                """;

        mockMvc.perform(post("/api/pos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại."))
                .andExpect(jsonPath("$.errors.items").value("Danh sách sản phẩm items không được để trống"));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("Validation: Sản phẩm có quantity <= 0 -> 400 Bad Request")
    void checkout_NonPositiveQuantity_Returns400BadRequest() throws Exception {
        String requestJson = """
                {
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 0
                        }
                    ],
                    "paymentMethod": "CASH"
                }
                """;

        mockMvc.perform(post("/api/pos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại."))
                .andExpect(jsonPath("$.errors['items[0].quantity']").value("Số lượng mua phải là số nguyên dương lớn hơn 0 (@Min(1))"));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("Validation: Thiếu paymentMethod (null) -> 400 Bad Request")
    void checkout_NullPaymentMethod_Returns400BadRequest() throws Exception {
        String requestJson = """
                {
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 2
                        }
                    ],
                    "paymentMethod": null
                }
                """;

        mockMvc.perform(post("/api/pos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại."))
                .andExpect(jsonPath("$.errors.paymentMethod").value("Phương thức thanh toán (paymentMethod) không được để trống"));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("Validation: Thiếu productId trong items -> 400 Bad Request")
    void checkout_NullProductId_Returns400BadRequest() throws Exception {
        String requestJson = """
                {
                    "items": [
                        {
                            "quantity": 2
                        }
                    ],
                    "paymentMethod": "CASH"
                }
                """;

        mockMvc.perform(post("/api/pos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại."))
                .andExpect(jsonPath("$.errors['items[0].productId']").value("ID sản phẩm (productId) không được để trống"));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("Validation: discountAmount < 0 (ví dụ: -50000) -> 400 Bad Request")
    void checkout_NegativeDiscountAmount_Returns400BadRequest() throws Exception {
        String requestJson = """
                {
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 2
                        }
                    ],
                    "paymentMethod": "CASH",
                    "discountAmount": -50000
                }
                """;

        mockMvc.perform(post("/api/pos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại."))
                .andExpect(jsonPath("$.errors.discountAmount").value("Số tiền giảm giá (discountAmount) không được là số âm"));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.never()).createOrder(any(), any());
    }

    // =========================================================================
    // 2. SUCCESSFUL CHECKOUT (HTTP 201 CREATED)
    // =========================================================================

    @Test
    @DisplayName("Luồng thành công: Gửi request hợp lệ -> 201 Created kèm JSON chuẩn")
    @WithMockUser(username = "thungan@gym.com", roles = "STAFF")
    void checkout_ValidRequest_Returns201Created() throws Exception {
        Staff staff = Staff.builder()
                .code("STF001")
                .name("Nguyễn Thu Ngân")
                .email("thungan@gym.com")
                .build();
        staff.setId(10L);

        when(staffRepository.findByEmailIgnoreCase("thungan@gym.com")).thenReturn(Optional.of(staff));

        OrderItemResponseDTO itemResponse = OrderItemResponseDTO.builder()
                .id(1L)
                .productId(101L)
                .productSku("DRK-WAT-001")
                .productName("Nước khoáng Lavie")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(10000))
                .totalPrice(BigDecimal.valueOf(20000))
                .build();

        OrderResponseDTO orderResponse = OrderResponseDTO.builder()
                .id(1L)
                .code("POS202610101234")
                .orderDate(LocalDateTime.now())
                .transactionTime(LocalDateTime.now())
                .staffId(10L)
                .staffName("Nguyễn Thu Ngân")
                .paymentMethod(PaymentMethod.CASH)
                .subtotal(BigDecimal.valueOf(20000))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(20000))
                .status("COMPLETED")
                .items(List.of(itemResponse))
                .build();

        when(posOrderService.createOrder(any(CheckoutRequestDTO.class), any())).thenReturn(orderResponse);

        CheckoutRequestDTO validRequest = CheckoutRequestDTO.builder()
                .items(List.of(OrderItemRequestDTO.builder().productId(101L).quantity(2).build()))
                .paymentMethod(PaymentMethod.CASH)
                .notes("Bán lẻ tại quầy")
                .build();

        mockMvc.perform(post("/api/pos/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("POS202610101234"))
                .andExpect(jsonPath("$.staffId").value(10))
                .andExpect(jsonPath("$.staffName").value("Nguyễn Thu Ngân"))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.totalAmount").value(20000))
                .andExpect(jsonPath("$.items[0].productName").value("Nước khoáng Lavie"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.times(1))
                .createOrder(any(CheckoutRequestDTO.class), org.mockito.ArgumentMatchers.isNull());
    }

    // =========================================================================
    // 3. API CHI TIẾT & HÓA ĐƠN (GET /orders/{id} & GET /orders/today)
    // =========================================================================

    @Test
    @DisplayName("GET /orders/{id}: Trả về 200 OK khi tồn tại đơn hàng")
    void getOrderById_Found_Returns200Ok() throws Exception {
        OrderResponseDTO orderResponse = OrderResponseDTO.builder()
                .id(1L)
                .code("POS202610101234")
                .totalAmount(BigDecimal.valueOf(50000))
                .paymentMethod(PaymentMethod.CASH)
                .status("COMPLETED")
                .build();

        when(posOrderService.getOrderById(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/pos/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("POS202610101234"))
                .andExpect(jsonPath("$.totalAmount").value(50000));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.times(1)).getOrderById(1L);
    }

    @Test
    @DisplayName("GET /orders/{id}: Trả về 404 Not Found khi không tìm thấy đơn")
    void getOrderById_NotFound_Returns404NotFound() throws Exception {
        when(posOrderService.getOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: 999"));

        mockMvc.perform(get("/api/pos/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy đơn hàng với ID: 999"));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.times(1)).getOrderById(999L);
    }

    @Test
    @DisplayName("GET /orders/today: Trả về 200 OK kèm danh sách đơn trong ngày")
    void getTodayOrders_Returns200OkWithList() throws Exception {
        OrderResponseDTO orderResponse = OrderResponseDTO.builder()
                .id(1L)
                .code("POS202610101234")
                .totalAmount(BigDecimal.valueOf(100000))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status("COMPLETED")
                .build();

        when(posOrderService.getTodayOrders()).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/pos/orders/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("POS202610101234"))
                .andExpect(jsonPath("$[0].paymentMethod").value("BANK_TRANSFER"));

        org.mockito.Mockito.verify(posOrderService, org.mockito.Mockito.times(1)).getTodayOrders();
    }
}
