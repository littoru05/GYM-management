package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.CheckoutRequestDTO;
import com.example.GYM_management_api.dtos.OrderItemRequestDTO;
import com.example.GYM_management_api.dtos.OrderResponseDTO;
import com.example.GYM_management_api.entities.*;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.entities.enums.NotificationType;
import com.example.GYM_management_api.entities.enums.PaymentMethod;
import com.example.GYM_management_api.entities.enums.TransactionType;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.InsufficientStockException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.repositories.*;
import com.example.GYM_management_api.services.impl.PosOrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import com.example.GYM_management_api.utils.TestReportWatcher;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
class PosOrderServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private PosOrderServiceImpl posOrderService;

    private Product productWater;
    private Product productWhey;
    private Staff mockStaff;
    private Member mockMember;

    @BeforeEach
    void setUp() {
        productWater = Product.builder()
                .sku("DRK-WAT-001")
                .name("Nước khoáng Lavie")
                .price(BigDecimal.valueOf(10000))
                .cost(BigDecimal.valueOf(5000))
                .stock(50)
                .minStock(10)
                .status(CommonStatus.ACTIVE)
                .build();
        productWater.setId(1L);

        productWhey = Product.builder()
                .sku("SUP-WHE-001")
                .name("Whey Gold Standard")
                .price(BigDecimal.valueOf(500000))
                .cost(BigDecimal.valueOf(400000))
                .stock(5)
                .minStock(2)
                .status(CommonStatus.ACTIVE)
                .build();
        productWhey.setId(2L);

        mockStaff = Staff.builder()
                .code("STF001")
                .name("Nguyễn Thu Ngân")
                .email("thungan@gym.com")
                .build();
        mockStaff.setId(10L);

        mockMember = Member.builder()
                .code("MEM001")
                .name("Trần Khách Hàng")
                .phone("0987654321")
                .build();
        mockMember.setId(20L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // 1. HAPPY PATH TEST CASES
    // =========================================================================

    @Test
    @DisplayName("Happy Path: Thanh toán giỏ hàng 2 sản phẩm hợp lệ, đủ tồn kho, tính đúng tiền và trừ kho")
    void createOrder_HappyPath_Success() {
        // Given
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(2).build(), // 2 * 10,000 = 20,000
                OrderItemRequestDTO.builder().productId(2L).quantity(1).build()  // 1 * 500,000 = 500,000
        );

        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .memberId(20L)
                .discountAmount(BigDecimal.valueOf(20000)) // 520,000 - 20,000 = 500,000
                .notes("Bán lẻ tại quầy")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(productRepository.findById(2L)).thenReturn(Optional.of(productWhey));
        when(staffRepository.findById(10L)).thenReturn(Optional.of(mockStaff));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(mockMember));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        // When
        OrderResponseDTO response = posOrderService.createOrder(request, 10L);

        // Then - 1. Kiểm tra DTO trả về
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStaffId()).isEqualTo(10L);
        assertThat(response.getStaffName()).isEqualTo("Nguyễn Thu Ngân");
        assertThat(response.getMemberId()).isEqualTo(20L);
        assertThat(response.getMemberName()).isEqualTo("Trần Khách Hàng");
        assertThat(response.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(520000));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(500000));
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(response.getItems()).hasSize(2);

        // Then - 2. Kiểm tra trừ tồn kho
        assertThat(productWater.getStock()).isEqualTo(48); // 50 - 2
        assertThat(productWhey.getStock()).isEqualTo(4);   // 5 - 1
        verify(productRepository, times(1)).save(productWater);
        verify(productRepository, times(1)).save(productWhey);

        // Then - 3. Kiểm tra lưu transaction
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        Transaction captured = txCaptor.getValue();
        assertThat(captured.getType()).isEqualTo(TransactionType.RETAIL);
        assertThat(captured.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500000));
        assertThat(captured.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Happy Path: Khách vãng lai (không có memberId), staff trích xuất từ Security Context")
    void createOrder_AnonymousMemberAndSecurityContextStaff_Success() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("thungan@gym.com");
        when(auth.getName()).thenReturn("thungan@gym.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(staffRepository.findByEmailIgnoreCase("thungan@gym.com")).thenReturn(Optional.of(mockStaff));

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });

        // When (currentUserId = null để kích hoạt trích xuất từ Security Context)
        OrderResponseDTO response = posOrderService.createOrder(request, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMemberName()).isEqualTo("Khách lẻ");
        assertThat(response.getMemberId()).isNull();
        assertThat(response.getStaffId()).isEqualTo(10L);
        assertThat(response.getStaffName()).isEqualTo("Nguyễn Thu Ngân");
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        verify(memberRepository, never()).findById(any());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Happy Path: Gom nhóm khi cùng 1 sản phẩm xuất hiện nhiều lần trong giỏ hàng")
    void createOrder_MergeDuplicateProductItems_Success() {
        // Given: Sản phẩm 1 xuất hiện 2 lần trong items: quantity 2 + quantity 3 = 5
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(2).build(),
                OrderItemRequestDTO.builder().productId(1L).quantity(3).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponseDTO response = posOrderService.createOrder(request, 10L);

        // Then: Kho giảm 5, còn 45
        assertThat(productWater.getStock()).isEqualTo(45);
        verify(productRepository, times(1)).save(productWater);
        assertThat(response.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(50000)); // 5 * 10,000
    }

    // =========================================================================
    // 2. ROLLBACK TRIGGER (INSUFFICIENT STOCK) TEST CASES
    // =========================================================================

    @Test
    @DisplayName("Rollback: Thiếu hàng tồn kho (Tồn = 2, Yêu cầu = 3) -> ném InsufficientStockException & KHÔNG lưu Transaction")
    void createOrder_InsufficientStock_ThrowsExceptionAndNeverSavesOrder() {
        // Given
        productWater.setStock(2);
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(3).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));

        // When & Then
        assertThatThrownBy(() -> posOrderService.createOrder(request, 10L))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("không đủ số lượng tồn kho");

        // CRITICAL: Đảm bảo tuyệt đối transactionRepository.save KHÔNG bao giờ được gọi
        verify(transactionRepository, never()).save(any());
        // Đảm bảo kho không bị trừ dở dang
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rollback đa sản phẩm: Giỏ hàng 2 sản phẩm (SP 1 đủ hàng, SP 2 thiếu hàng) -> ném InsufficientStockException & KHÔNG trừ kho SP 1, KHÔNG lưu đơn")
    void createOrder_MultiItems_OneExceedsStock_RollbacksAllProductsAndNeverSavesOrder() {
        // Given: SP1 (nước) tồn 50 mua 2 (hợp lệ); SP2 (whey) tồn 2 mua 5 (vượt tồn kho)
        productWhey.setStock(2);
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(2).build(),
                OrderItemRequestDTO.builder().productId(2L).quantity(5).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(productRepository.findById(2L)).thenReturn(Optional.of(productWhey));

        // When & Then
        assertThatThrownBy(() -> posOrderService.createOrder(request, 10L))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("không đủ số lượng tồn kho")
                .hasMessageContaining("Whey Gold Standard");

        // CRITICAL: Tuyệt đối không trừ kho sản phẩm nào (ngay cả SP1 đủ hàng cũng không bị trừ dở dang)
        verify(productRepository, never()).save(any(Product.class));
        assertThat(productWater.getStock()).isEqualTo(50); // Nguyên vẹn 50
        assertThat(productWhey.getStock()).isEqualTo(2);   // Nguyên vẹn 2

        // Tuyệt đối không lưu giao dịch
        verify(transactionRepository, never()).save(any(Transaction.class));
    }


    // =========================================================================
    // 3. INVALID PRODUCT & VALIDATION TEST CASES
    // =========================================================================

    @Test
    @DisplayName("Bắt lỗi: Giỏ hàng rỗng (items = null hoặc empty) -> ném BadRequestException")
    void createOrder_EmptyItems_ThrowsBadRequestException() {
        CheckoutRequestDTO requestWithNull = CheckoutRequestDTO.builder().items(null).build();
        assertThatThrownBy(() -> posOrderService.createOrder(requestWithNull, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Danh sách sản phẩm items không được để trống");

        CheckoutRequestDTO requestWithEmpty = CheckoutRequestDTO.builder().items(Collections.emptyList()).build();
        assertThatThrownBy(() -> posOrderService.createOrder(requestWithEmpty, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Danh sách sản phẩm items không được để trống");

        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Bắt lỗi: Sản phẩm trong giỏ có productId = null hoặc quantity <= 0 -> ném BadRequestException")
    void createOrder_InvalidItemAttributes_ThrowsBadRequestException() {
        CheckoutRequestDTO reqNullId = CheckoutRequestDTO.builder()
                .items(List.of(OrderItemRequestDTO.builder().productId(null).quantity(2).build()))
                .build();
        assertThatThrownBy(() -> posOrderService.createOrder(reqNullId, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ID sản phẩm (productId) không được để trống");

        CheckoutRequestDTO reqZeroQty = CheckoutRequestDTO.builder()
                .items(List.of(OrderItemRequestDTO.builder().productId(1L).quantity(0).build()))
                .build();
        assertThatThrownBy(() -> posOrderService.createOrder(reqZeroQty, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số lượng mua của sản phẩm");

        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Bắt lỗi: Sản phẩm không tồn tại trong hệ thống -> ném ResourceNotFoundException")
    void createOrder_ProductNotFound_ThrowsResourceNotFoundException() {
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(999L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder().items(items).build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> posOrderService.createOrder(request, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy sản phẩm với ID: 999");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bắt lỗi: Sản phẩm đang ở trạng thái ngừng kinh doanh (INACTIVE) -> ném BadRequestException")
    void createOrder_InactiveProduct_ThrowsBadRequestException() {
        productWater.setStatus(CommonStatus.INACTIVE);
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder().items(items).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));

        assertThatThrownBy(() -> posOrderService.createOrder(request, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("hiện không hoạt động hoặc ngừng kinh doanh");

        verify(transactionRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bắt lỗi: discountAmount < 0 (số tiền giảm giá âm) -> ném BadRequestException")
    void createOrder_NegativeDiscountAmount_ThrowsBadRequestException() {
        // Given
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(2).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .discountAmount(BigDecimal.valueOf(-50000))
                .build();

        // When & Then
        assertThatThrownBy(() -> posOrderService.createOrder(request, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số tiền giảm giá không được là số âm");

        verify(transactionRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bắt lỗi: discountAmount > subtotal (giảm giá vượt quá tổng tiền đơn hàng) -> ném BadRequestException")
    void createOrder_DiscountAmountExceedsSubtotal_ThrowsBadRequestException() {
        // Given: subtotal = 1 * 10,000 = 10,000; discount = 50,000 > subtotal
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .discountAmount(BigDecimal.valueOf(50000))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));

        // When & Then
        assertThatThrownBy(() -> posOrderService.createOrder(request, 10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số tiền giảm giá không được vượt quá tổng giá trị đơn hàng");

        verify(transactionRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bắt lỗi: Hội viên truyền vào không tồn tại -> ném ResourceNotFoundException")
    void createOrder_MemberNotFound_ThrowsResourceNotFoundException() {
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .memberId(999L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> posOrderService.createOrder(request, 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy hội viên với ID: 999");

        verify(transactionRepository, never()).save(any());
    }

    // =========================================================================
    // 4. LOW STOCK INVENTORY NOTIFICATION
    // =========================================================================

    @Test
    @DisplayName("Cảnh báo kho: Khi số lượng sau khi trừ <= minStock -> bắn thông báo tồn kho")
    void createOrder_LowStock_TriggersNotification() {
        // Given: Whey có stock = 3, minStock = 2. Mua 2 -> còn 1 (<= minStock 2)
        productWhey.setStock(3);
        productWhey.setMinStock(2);

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(2L).quantity(2).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(productWhey));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        posOrderService.createOrder(request, 10L);

        // Then
        ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notifCaptor.capture());
        Notification notification = notifCaptor.getValue();
        assertThat(notification.getType()).isEqualTo(NotificationType.INVENTORY);
        assertThat(notification.getTargetRole()).isEqualTo("ADMIN");
        assertThat(notification.getMessage()).contains("chỉ còn tồn 1 món");
    }

    // =========================================================================
    // 5. QUERY ORDERS: getOrderById & getTodayOrders
    // =========================================================================

    @Test
    @DisplayName("getOrderById: Thành công khi ID tồn tại")
    void getOrderById_Found_ReturnsDTO() {
        Transaction tx = Transaction.builder()
                .code("POS20261010001")
                .transactionTime(LocalDateTime.now())
                .subtotal(BigDecimal.valueOf(10000))
                .amount(BigDecimal.valueOf(10000))
                .paymentMethod(PaymentMethod.CASH)
                .items(new ArrayList<>())
                .build();
        tx.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        OrderResponseDTO response = posOrderService.getOrderById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("POS20261010001");
    }

    @Test
    @DisplayName("getOrderById: Ném ResourceNotFoundException khi ID không tồn tại")
    void getOrderById_NotFound_ThrowsException() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> posOrderService.getOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy đơn hàng với ID: 999");
    }

    @Test
    @DisplayName("getTodayOrders: Trả về danh sách đơn bán lẻ trong ngày")
    void getTodayOrders_ReturnsList() {
        Transaction tx = Transaction.builder()
                .code("POS20261010002")
                .transactionTime(LocalDateTime.now())
                .subtotal(BigDecimal.valueOf(20000))
                .amount(BigDecimal.valueOf(20000))
                .paymentMethod(PaymentMethod.CARD)
                .items(new ArrayList<>())
                .build();
        tx.setId(2L);

        when(transactionRepository.findByTypeAndTransactionTimeBetweenOrderByTransactionTimeDesc(
                eq(TransactionType.RETAIL), any(), any()))
                .thenReturn(List.of(tx));

        List<OrderResponseDTO> result = posOrderService.getTodayOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("POS20261010002");
        verify(transactionRepository, times(1))
                .findByTypeAndTransactionTimeBetweenOrderByTransactionTimeDesc(eq(TransactionType.RETAIL), any(), any());
    }

    // =========================================================================
    // 6. BRANCH COVERAGE: DISCOUNT, NULL VALUES, CODE RETRY & STAFF LOOKUP
    // =========================================================================

    @Test
    @DisplayName("Branch Coverage: Giảm giá bằng 100% tổng tiền (Discount == Subtotal) -> finalAmount = 0")
    void createOrder_DiscountEqualsSubtotal_SetsAmountToZero() {
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build() // 1 * 10,000 = 10,000
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .discountAmount(BigDecimal.valueOf(10000)) // 10,000 - 10,000 = 0
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(staffRepository.findById(10L)).thenReturn(Optional.of(mockStaff));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, 10L);

        assertThat(response.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Branch Coverage: Sản phẩm có price và cost là null -> tính toán mặc định là ZERO")
    void createOrder_ProductPriceOrCostNull_DefaultsToZero() {
        productWater.setPrice(null);
        productWater.setCost(null);

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(null) // test paymentMethod fallback
                .notes("")           // test notes fallback
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(staffRepository.findById(10L)).thenReturn(Optional.of(mockStaff));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, 10L);

        assertThat(response.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(response.getNotes()).isEqualTo("Bán lẻ tại quầy POS");
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Branch Coverage: Trùng mã đơn hàng ở lần đầu -> vòng lặp while tạo mã mới thành công")
    void createOrder_OrderCodeCollision_RetriesCodeGeneration() {
        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(staffRepository.findById(10L)).thenReturn(Optional.of(mockStaff));
        // Lần đầu trả về true (trùng mã), lần hai trả về false (hợp lệ)
        when(transactionRepository.existsByCode(anyString())).thenReturn(true).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, 10L);

        assertThat(response).isNotNull();
        verify(transactionRepository, times(2)).existsByCode(anyString());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Branch Coverage: Tra cứu thu ngân fallback qua findByCode khi findByEmailIgnoreCase không thấy")
    void createOrder_StaffLookupFallbackToFindByCode_WhenEmailNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("STF001");
        when(auth.getName()).thenReturn("STF001");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(staffRepository.findByEmailIgnoreCase("STF001")).thenReturn(Optional.empty());
        when(staffRepository.findByCode("STF001")).thenReturn(Optional.of(mockStaff));

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, null);

        assertThat(response.getStaffId()).isEqualTo(10L);
        assertThat(response.getStaffName()).isEqualTo("Nguyễn Thu Ngân");
        verify(staffRepository, times(1)).findByEmailIgnoreCase("STF001");
        verify(staffRepository, times(1)).findByCode("STF001");
    }

    @Test
    @DisplayName("Branch Coverage: currentUserId truyền vào không tìm thấy staff -> fallback SecurityContext")
    void createOrder_StaffIdNotFound_FallsBackToSecurityContext() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("thungan@gym.com");
        when(auth.getName()).thenReturn("thungan@gym.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(staffRepository.findById(999L)).thenReturn(Optional.empty());
        when(staffRepository.findByEmailIgnoreCase("thungan@gym.com")).thenReturn(Optional.of(mockStaff));

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, 999L);

        assertThat(response.getStaffId()).isEqualTo(10L);
        verify(staffRepository, times(1)).findById(999L);
        verify(staffRepository, times(1)).findByEmailIgnoreCase("thungan@gym.com");
    }

    @Test
    @DisplayName("Branch Coverage: Sản phẩm có minStock = null -> không bắn thông báo tồn kho")
    void createOrder_ProductMinStockNull_DoesNotTriggerNotification() {
        productWater.setMinStock(null);

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(staffRepository.findById(10L)).thenReturn(Optional.of(mockStaff));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        posOrderService.createOrder(request, 10L);

        verify(notificationRepository, never()).save(any());
    }

    // =========================================================================
    // 7. COMPREHENSIVE COVERAGE FOR getCurrentAuthenticatedStaff() (100% BRANCHES)
    // =========================================================================

    @Test
    @DisplayName("Branch getCurrentAuthenticatedStaff: SecurityContext Authentication là null -> staff là null, tên 'Thu ngân'")
    void createOrder_SecurityContextAuthNull_DefaultsStaffToNull() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, null);

        assertThat(response.getStaffId()).isNull();
        assertThat(response.getStaffName()).isEqualTo("Thu ngân");
        verify(staffRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Branch getCurrentAuthenticatedStaff: auth.isAuthenticated() là false -> staff là null")
    void createOrder_SecurityContextUnauthenticated_DefaultsStaffToNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, null);

        assertThat(response.getStaffId()).isNull();
        assertThat(response.getStaffName()).isEqualTo("Thu ngân");
        verify(staffRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Branch getCurrentAuthenticatedStaff: auth.getPrincipal() là 'anonymousUser' -> staff là null")
    void createOrder_SecurityContextAnonymousUser_DefaultsStaffToNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("anonymousUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, null);

        assertThat(response.getStaffId()).isNull();
        assertThat(response.getStaffName()).isEqualTo("Thu ngân");
        verify(staffRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Branch getCurrentAuthenticatedStaff: Cả findByEmail và findByCode đều không thấy -> trả về null (.orElse(null))")
    void createOrder_StaffNotFoundInDatabaseByEmailOrCode_DefaultsStaffToNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("unknown@gym.com");
        when(auth.getName()).thenReturn("unknown@gym.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(staffRepository.findByEmailIgnoreCase("unknown@gym.com")).thenReturn(Optional.empty());
        when(staffRepository.findByCode("unknown@gym.com")).thenReturn(Optional.empty());

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, null);

        assertThat(response.getStaffId()).isNull();
        assertThat(response.getStaffName()).isEqualTo("Thu ngân");
        verify(staffRepository, times(1)).findByEmailIgnoreCase("unknown@gym.com");
        verify(staffRepository, times(1)).findByCode("unknown@gym.com");
    }

    @Test
    @DisplayName("Branch getCurrentAuthenticatedStaff: Lỗi ngoại lệ trong SecurityContext -> khối catch xử lý an toàn và trả về null")
    void createOrder_SecurityContextThrowsException_CatchesAndDefaultsStaffToNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("admin@gym.com");
        when(auth.getName()).thenThrow(new RuntimeException("Simulated security context error"));

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        List<OrderItemRequestDTO> items = List.of(
                OrderItemRequestDTO.builder().productId(1L).quantity(1).build()
        );
        CheckoutRequestDTO request = CheckoutRequestDTO.builder()
                .items(items)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(productWater));
        when(transactionRepository.existsByCode(anyString())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = posOrderService.createOrder(request, null);

        assertThat(response.getStaffId()).isNull();
        assertThat(response.getStaffName()).isEqualTo("Thu ngân");
    }
}
