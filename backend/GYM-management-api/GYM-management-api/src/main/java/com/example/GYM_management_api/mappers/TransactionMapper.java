package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.TransactionDetailDto;
import com.example.GYM_management_api.dtos.TransactionDto;
import com.example.GYM_management_api.entities.*;
import com.example.GYM_management_api.entities.enums.PaymentMethod;
import com.example.GYM_management_api.entities.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionDto dto, Member member, Staff staff, Promotion promotion) {
        if (dto == null) {
            return null;
        }

        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "TX" + (System.currentTimeMillis() % 100000);
        }

        LocalDateTime txTime = dto.getTransactionTime();
        if (txTime == null) {
            txTime = LocalDateTime.now();
        }

        TransactionType type = TransactionType.RETAIL;
        if (dto.getType() != null) {
            try {
                type = TransactionType.valueOf(dto.getType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        PaymentMethod paymentMethod = PaymentMethod.CASH;
        if (dto.getPaymentMethod() != null) {
            try {
                paymentMethod = PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String memberName = dto.getMemberName();
        if (memberName == null && member != null) {
            memberName = member.getName();
        }
        if (memberName == null) {
            memberName = "Khách lẻ";
        }

        String staffName = dto.getStaffName();
        if (staffName == null && staff != null) {
            staffName = staff.getName();
        }

        Transaction tx = Transaction.builder()
                .code(code)
                .transactionTime(txTime)
                .type(type)
                .member(member)
                .memberName(memberName)
                .staff(staff)
                .staffName(staffName)
                .promotion(promotion)
                .subtotal(dto.getSubtotal() != null ? dto.getSubtotal() : dto.getAmount())
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                .amount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO)
                .paymentMethod(paymentMethod)
                .description(dto.getDescription())
                .items(new ArrayList<>())
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                tx.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return tx;
    }

    public TransactionDto toDto(Transaction entity) {
        if (entity == null) {
            return null;
        }

        List<TransactionDetailDto> itemDtos = entity.getItems() != null
                ? entity.getItems().stream().map(this::toDetailDto).collect(Collectors.toList())
                : new ArrayList<>();

        String memberId = entity.getMember() != null ? String.valueOf(entity.getMember().getId()) : null;
        String staffId = entity.getStaff() != null ? String.valueOf(entity.getStaff().getId()) : null;
        String promoCode = entity.getPromotion() != null ? entity.getPromotion().getCode() : null;

        String membershipId = null;
        String membershipName = null;
        String trainerName = null;

        if (entity.getItems() != null) {
            for (TransactionDetail d : entity.getItems()) {
                if (d.getMembership() != null) {
                    membershipId = String.valueOf(d.getMembership().getId());
                    membershipName = d.getMembership().getName();
                } else if (d.getItemName() != null && d.getItemName().toLowerCase().contains("gói")) {
                    membershipName = d.getItemName();
                }
                if (d.getTrainer() != null) {
                    trainerName = d.getTrainer().getName();
                } else if (d.getItemName() != null && d.getItemName().toLowerCase().contains("hlv")) {
                    trainerName = d.getItemName();
                }
            }
        }

        if (membershipName == null && entity.getMember() != null && entity.getMember().getCurrentMembership() != null) {
            membershipId = String.valueOf(entity.getMember().getCurrentMembership().getId());
            membershipName = entity.getMember().getCurrentMembership().getName();
        }

        if (membershipName == null && entity.getDescription() != null && entity.getType() == TransactionType.MEMBERSHIP) {
            membershipName = entity.getDescription();
        }

        String memberName = entity.getMemberName();
        if ((memberName == null || memberName.trim().isEmpty() || "null".equalsIgnoreCase(memberName)) && entity.getMember() != null) {
            memberName = entity.getMember().getName();
        }

        String desc = entity.getDescription();
        if ((desc == null || desc.trim().isEmpty() || "null".equalsIgnoreCase(desc)) && entity.getType() == TransactionType.MEMBERSHIP) {
            if (membershipName != null) {
                desc = "Gói " + membershipName + (trainerName != null ? " + HLV " + trainerName : "");
            }
        }

        return TransactionDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .code(entity.getCode())
                .date(entity.getTransactionTime() != null ? entity.getTransactionTime().toLocalDate() : null)
                .transactionTime(entity.getTransactionTime())
                .type(entity.getType() != null ? entity.getType().name() : "RETAIL")
                .memberId(memberId)
                .memberName(memberName)
                .membershipId(membershipId)
                .membershipName(membershipName)
                .trainerName(trainerName)
                .staffId(staffId)
                .staffName(entity.getStaffName())
                .promotionCode(promoCode)
                .subtotal(entity.getSubtotal())
                .discountAmount(entity.getDiscountAmount())
                .amount(entity.getAmount())
                .paymentMethod(entity.getPaymentMethod() != null ? entity.getPaymentMethod().name() : "CASH")
                .description(desc)
                .items(itemDtos)
                .build();
    }

    public TransactionDetailDto toDetailDto(TransactionDetail detail) {
        if (detail == null) return null;

        return TransactionDetailDto.builder()
                .id(detail.getId() != null ? String.valueOf(detail.getId()) : null)
                .productId(detail.getProduct() != null ? String.valueOf(detail.getProduct().getId()) : null)
                .membershipId(detail.getMembership() != null ? String.valueOf(detail.getMembership().getId()) : null)
                .trainerId(detail.getTrainer() != null ? String.valueOf(detail.getTrainer().getId()) : null)
                .itemType(detail.getItemType())
                .itemName(detail.getItemName())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .unitCost(detail.getUnitCost())
                .totalPrice(detail.getTotalPrice())
                .build();
    }
}
