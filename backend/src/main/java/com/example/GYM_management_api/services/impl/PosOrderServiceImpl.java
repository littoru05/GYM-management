package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.CheckoutRequestDTO;
import com.example.GYM_management_api.dtos.OrderItemRequestDTO;
import com.example.GYM_management_api.dtos.OrderItemResponseDTO;
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
import com.example.GYM_management_api.services.IPosOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PosOrderServiceImpl implements IPosOrderService {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final StaffRepository staffRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDTO createOrder(CheckoutRequestDTO request, Long currentUserId) {
        log.info("Bắt đầu xử lý POS Checkout với {} sản phẩm",
                request.getItems() != null ? request.getItems().size() : 0);

        // 1. Kiểm tra giỏ hàng
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Danh sách sản phẩm items không được để trống.");
        }

        // 2. Gom nhóm số lượng mua theo productId để xử lý trường hợp trùng sản phẩm trong 1 request
        Map<Long, Integer> productQuantityMap = new LinkedHashMap<>();
        for (OrderItemRequestDTO item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new BadRequestException("ID sản phẩm (productId) không được để trống.");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException("Số lượng mua của sản phẩm (ID: " + item.getProductId() + ") phải lớn hơn 0 (@Min(1)).");
            }
            productQuantityMap.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        // 3. Đối soát tồn kho và trạng thái tất cả sản phẩm TRƯỚC khi trừ kho
        Map<Long, Product> productMap = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : productQuantityMap.entrySet()) {
            Long productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

            if (product.getStatus() != CommonStatus.ACTIVE) {
                throw new BadRequestException("Sản phẩm '" + product.getName() + "' (Mã: " + product.getSku()
                        + ") hiện không hoạt động hoặc ngừng kinh doanh.");
            }

            int currentStock = product.getStock() != null ? product.getStock() : 0;
            if (currentStock < requestedQuantity) {
                log.warn("Sản phẩm ID={} '{}' không đủ tồn kho (Hiện có: {}, Yêu cầu: {})",
                        productId, product.getName(), currentStock, requestedQuantity);
                throw new InsufficientStockException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho (Tồn: "
                        + currentStock + ", Yêu cầu: " + requestedQuantity + ").");
            }

            productMap.put(productId, product);
        }

        // 4. Trừ tồn kho và lưu vào CSDL
        for (Map.Entry<Long, Integer> entry : productQuantityMap.entrySet()) {
            Product product = productMap.get(entry.getKey());
            int newStock = product.getStock() - entry.getValue();
            product.setStock(newStock);
            productRepository.save(product);

            // Bắn thông báo nếu sắp hết hàng
            if (product.getMinStock() != null && newStock <= product.getMinStock()) {
                Notification notif = Notification.builder()
                        .title("Cảnh báo sắp hết hàng: " + product.getName())
                        .message("Sản phẩm " + product.getName() + " (SKU: " + product.getSku()
                                + ") chỉ còn tồn " + newStock + " món (Định mức tối thiểu: " + product.getMinStock() + ").")
                        .type(NotificationType.INVENTORY)
                        .targetRole("ADMIN")
                        .actionUrl("/inventory/products")
                        .build();
                notificationRepository.save(notif);
            }
        }

        // 5. Xác định nhân viên thu ngân
        Staff staff = null;
        if (currentUserId != null) {
            staff = staffRepository.findById(currentUserId).orElse(null);
        }
        if (staff == null) {
            staff = getCurrentAuthenticatedStaff();
        }

        // 6. Xác định thông tin hội viên (nếu có)
        Member member = null;
        if (request.getMemberId() != null) {
            member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên với ID: " + request.getMemberId()));
        }

        // 7. Khởi tạo đơn hàng Transaction
        LocalDateTime now = LocalDateTime.now();
        String orderCode = generateOrderCode();

        Transaction order = Transaction.builder()
                .code(orderCode)
                .transactionTime(now)
                .type(TransactionType.RETAIL)
                .member(member)
                .memberName(member != null ? member.getName() : "Khách lẻ")
                .staff(staff)
                .staffName(staff != null ? staff.getName() : "Thu ngân")
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH)
                .description(request.getNotes() != null && !request.getNotes().isBlank() ? request.getNotes() : "Bán lẻ tại quầy POS")
                .items(new ArrayList<>())
                .build();

        // 8. Tính toán tiền tệ bằng BigDecimal cho từng chi tiết món hàng
        BigDecimal subtotal = BigDecimal.ZERO;
        List<TransactionDetail> details = new ArrayList<>();

        for (OrderItemRequestDTO itemReq : request.getItems()) {
            Product product = productMap.get(itemReq.getProductId());
            BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            BigDecimal unitCost = product.getCost() != null ? product.getCost() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            subtotal = subtotal.add(lineTotal);

            TransactionDetail detail = TransactionDetail.builder()
                    .transaction(order)
                    .product(product)
                    .itemType("PRODUCT")
                    .itemName(product.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .unitCost(unitCost)
                    .totalPrice(lineTotal)
                    .build();

            details.add(detail);
        }

        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setAmount(finalAmount);
        order.getItems().addAll(details);

        // 9. Lưu đơn hàng Transaction kèm TransactionDetail (CascadeType.ALL)
        Transaction savedOrder = transactionRepository.save(order);
        log.info("Tạo đơn hàng POS thành công! Mã đơn: {}, Tổng tiền: {}", savedOrder.getCode(), savedOrder.getAmount());

        return mapToOrderResponseDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Transaction order = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
        return mapToOrderResponseDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getTodayOrders() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Transaction> orders = transactionRepository
                .findByTypeAndTransactionTimeBetweenOrderByTransactionTimeDesc(
                        TransactionType.RETAIL, startOfDay, endOfDay);

        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToOrderResponseDTO(Transaction order) {
        List<OrderItemResponseDTO> itemResponses = new ArrayList<>();
        if (order.getItems() != null) {
            for (TransactionDetail detail : order.getItems()) {
                Product p = detail.getProduct();
                itemResponses.add(OrderItemResponseDTO.builder()
                        .id(detail.getId())
                        .productId(p != null ? p.getId() : null)
                        .productSku(p != null ? p.getSku() : null)
                        .productName(detail.getItemName())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .unitCost(detail.getUnitCost())
                        .totalPrice(detail.getTotalPrice())
                        .build());
            }
        }

        return OrderResponseDTO.builder()
                .id(order.getId())
                .code(order.getCode())
                .orderDate(order.getTransactionTime())
                .transactionTime(order.getTransactionTime())
                .staffId(order.getStaff() != null ? order.getStaff().getId() : null)
                .staffName(order.getStaffName())
                .memberId(order.getMember() != null ? order.getMember().getId() : null)
                .memberName(order.getMemberName())
                .paymentMethod(order.getPaymentMethod())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getAmount())
                .notes(order.getDescription())
                .status("COMPLETED")
                .items(itemResponses)
                .build();
    }

    private synchronized String generateOrderCode() {
        String datePrefix = "POS" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long randomSuffix = (long) (Math.random() * 9000L) + 1000L;
        String code = datePrefix + randomSuffix;
        while (transactionRepository.existsByCode(code)) {
            randomSuffix = (long) (Math.random() * 9000L) + 1000L;
            code = datePrefix + randomSuffix;
        }
        return code;
    }

    private Staff getCurrentAuthenticatedStaff() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                return staffRepository.findByEmailIgnoreCase(username)
                        .or(() -> staffRepository.findByCode(username))
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Không thể lấy thông tin thu ngân từ SecurityContext: {}", e.getMessage());
        }
        return null;
    }
}
