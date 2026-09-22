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

        return MemberSubscriptionDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .membershipId(membershipId)
                .membershipName(membershipName)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .paidPrice(entity.getPaidPrice())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
