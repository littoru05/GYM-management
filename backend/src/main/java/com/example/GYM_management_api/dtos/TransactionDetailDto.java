package com.example.GYM_management_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailDto {
    private String id;
    private String productId;
    private String membershipId;
    private String trainerId;
    private String itemType;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal unitCost;
    private BigDecimal totalPrice;
}
