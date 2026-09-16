package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.MemberDto;
import com.example.GYM_management_api.entities.Member;
import com.example.GYM_management_api.entities.Membership;
import com.example.GYM_management_api.entities.enums.MemberStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MemberMapper {

    public Member toEntity(MemberDto dto, Membership membership) {
        if (dto == null) {
            return null;
        }

        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "M" + String.format("%04d", (System.currentTimeMillis() % 10000));
        }

        MemberStatus status = calculateMemberStatus(dto.getExpiryDate());

        Member member = Member.builder()
                .code(code.trim().toUpperCase())
                .name(dto.getName())
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .email(dto.getEmail())
                .dob(dto.getDob())
                .joinDate(dto.getJoinDate() != null ? dto.getJoinDate() : LocalDate.now())
                .status(status)
                .avatar(dto.getAvatar() != null ? dto.getAvatar() : "https://i.pravatar.cc/150?u=" + code)
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                member.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return member;
    }

    public void updateEntityFromDto(MemberDto dto, Member entity, Membership membership) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : entity.getPhone());
        entity.setEmail(dto.getEmail());
        if (dto.getDob() != null) {
            entity.setDob(dto.getDob());
        }
        if (dto.getJoinDate() != null) {
            entity.setJoinDate(dto.getJoinDate());
        }
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                entity.setStatus(MemberStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (dto.getAvatar() != null) {
            entity.setAvatar(dto.getAvatar());
        }
        if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
            entity.setCode(dto.getCode().trim().toUpperCase());
        }
    }

    public MemberDto toDto(Member entity) {
        if (entity == null) {
            return null;
        }

        Membership ms = entity.getCurrentMembership();
        String msId = ms != null ? String.valueOf(ms.getId()) : null;
        String msName = ms != null ? ms.getName() : null;
        LocalDate expiryDate = entity.getLatestExpiryDate();

        MemberStatus currentStatus = entity.getStatus();
        if (currentStatus != MemberStatus.LOCKED) {
            currentStatus = calculateMemberStatus(expiryDate);
        }

        return MemberDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .code(entity.getCode())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .dob(entity.getDob())
                .joinDate(entity.getJoinDate())
                .membershipId(msId)
                .membershipName(msName)
                .expiryDate(expiryDate)
                .status(currentStatus != null ? currentStatus.name() : "ACTIVE")
                .avatar(entity.getAvatar())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getCreatedAt())
                .build();
    }

    public MemberStatus calculateMemberStatus(LocalDate expiryDate) {
        if (expiryDate == null) return MemberStatus.ACTIVE;
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return MemberStatus.EXPIRED;
        } else if (!expiryDate.isAfter(today.plusDays(7))) {
            return MemberStatus.EXPIRING_SOON;
        } else {
            return MemberStatus.ACTIVE;
        }
    }
}
