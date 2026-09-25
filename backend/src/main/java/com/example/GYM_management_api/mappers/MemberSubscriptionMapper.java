package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.MemberSubscriptionDto;
import com.example.GYM_management_api.entities.MemberSubscription;
import org.springframework.stereotype.Component;

@Component
public class MemberSubscriptionMapper {

    public MemberSubscriptionDto toDto(MemberSubscription entity) {
        if (entity == null) {
            return null;
        }

        Long membershipId = null;
        String membershipName = null;
        if (entity.getMembership() != null) {
            membershipId = entity.getMembership().getId();
            membershipName = entity.getMembership().getName();
        }

        Long memberId = null;
        String memberCode = null;
        if (entity.getMember() != null) {
            memberId = entity.getMember().getId();
            memberCode = entity.getMember().getCode();
        }

        String paymentMethod = null;
        if (entity.getTransaction() != null && entity.getTransaction().getPaymentMethod() != null) {
            paymentMethod = entity.getTransaction().getPaymentMethod().name();
        }

        return MemberSubscriptionDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .memberId(memberId)
                .memberCode(memberCode)
                .membershipId(membershipId)
                .membershipName(membershipName)
                .paymentMethod(paymentMethod)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .paidPrice(entity.getPaidPrice())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
