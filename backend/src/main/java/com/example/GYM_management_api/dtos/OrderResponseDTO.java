package com.example.GYM_management_api.dtos;

import com.example.GYM_management_api.entities.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết đơn hàng hóa đơn bán lẻ Mini POS (UC09)")
public class OrderResponseDTO {

    @Schema(description = "ID đơn hàng", example = "1")
    private Long id;

    @Schema(description = "Mã đơn hàng", example = "POS20261006001")
    private String code;

    @Schema(description = "Thời gian tạo đơn hàng", example = "2026-10-06T15:30:00")
    private LocalDateTime orderDate;

    @Schema(description = "Thời gian thanh toán giao dịch", example = "2026-10-06T15:30:00")
    private LocalDateTime transactionTime;

    @Schema(description = "ID nhân viên thu ngân", example = "2")
    private Long staffId;

    @Schema(description = "Tên nhân viên thu ngân", example = "Nguyễn Văn Thu Ngân")
    private String staffName;

    @Schema(description = "ID hội viên (nếu có)", example = "1")
    private Long memberId;

    @Schema(description = "Tên khách hàng / hội viên", example = "Trần Khách Lẻ")
    private String memberName;

    @Schema(description = "Phương thức thanh toán", example = "CASH")
    private PaymentMethod paymentMethod;

    @Schema(description = "Tổng tiền trước giảm giá (VND)", example = "150000.00")
    private BigDecimal subtotal;

    @Schema(description = "Số tiền giảm giá (VND)", example = "10000.00")
    private BigDecimal discountAmount;

    @Schema(description = "Tổng tiền thanh toán cuối cùng (finalAmount)", example = "140000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Alias tổng tiền thanh toán (amount)", example = "140000.00")
    private BigDecimal amount;

    @Schema(description = "Ghi chú đơn hàng", example = "Khách thanh toán tại quầy POS")
    private String notes;

    @Schema(description = "Trạng thái đơn hàng", example = "COMPLETED")
    @Builder.Default
    private String status = "COMPLETED";

    @Schema(description = "Danh sách chi tiết các món hàng")
    @Builder.Default
    private List<OrderItemResponseDTO> items = new ArrayList<>();

    public BigDecimal getTotalAmount() {
        if (this.totalAmount != null) return this.totalAmount;
        return this.amount;
    }

    public BigDecimal getAmount() {
        if (this.amount != null) return this.amount;
        return this.totalAmount;
    }

    public String getDescription() {
        return this.notes;
    }

    public void setDescription(String description) {
        this.notes = description;
    }
}
