package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.CheckInDto;
import com.example.GYM_management_api.entities.CheckIn;
import com.example.GYM_management_api.entities.Member;
import com.example.GYM_management_api.entities.enums.CheckInStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CheckInMapper {

    public CheckIn toEntity(CheckInDto dto, Member member) {
        if (dto == null) {
            return null;
        }

        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = "CI" + String.format("%04d", (System.currentTimeMillis() % 10000));
        }

        CheckInStatus status = CheckInStatus.SUCCESS;
        if (dto.getStatus() != null) {
            try {
                status = CheckInStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        String memberName = dto.getMemberName();
        if (memberName == null && member != null) {
            memberName = member.getName();
        }

        String avatar = dto.getAvatar();
        if (avatar == null && member != null) {
            avatar = member.getAvatar();
        }

        String msName = dto.getMembershipName();
        if (msName == null && member != null && member.getCurrentMembership() != null) {
            msName = member.getCurrentMembership().getName();
        }

        LocalDateTime checkInTime = dto.getTime() != null ? dto.getTime() : LocalDateTime.now();

        CheckIn record = CheckIn.builder()
                .code(code)
                .member(member)
                .memberName(memberName != null ? memberName : "")
                .avatar(avatar)
                .membershipName(msName)
                .checkInTime(checkInTime)
                .checkOutTime(dto.getCheckOutTime())
                .status(status)
                .reason(dto.getReason())
                .build();

        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            try {
                record.setId(Long.parseLong(dto.getId().trim()));
            } catch (NumberFormatException ignored) {}
        }

        return record;
    }

    public CheckInDto toDto(CheckIn entity) {
        if (entity == null) {
            return null;
        }

        String memberId = entity.getMember() != null ? String.valueOf(entity.getMember().getId()) : null;

        String warningLevel = "GREEN";
        if (entity.getStatus() == CheckInStatus.DENIED) {
            warningLevel = "RED";
        } else if (entity.getSubscription() != null && entity.getSubscription().getEndDate() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate endDate = entity.getSubscription().getEndDate();
            if (endDate.isBefore(today)) {
                warningLevel = "RED";
            } else if (!endDate.isAfter(today.plusDays(7))) {
                warningLevel = "YELLOW";
            }
        }

        Long staffId = entity.getStaff() != null ? entity.getStaff().getId() : null;
        String staffCode = entity.getStaff() != null ? entity.getStaff().getCode() : null;
        String staffName = entity.getStaffName();
        if (staffName == null && entity.getStaff() != null) {
            staffName = entity.getStaff().getName();
        }

        String phone = entity.getMember() != null ? entity.getMember().getPhone() : null;

        return CheckInDto.builder()
                .id(entity.getId() != null ? String.valueOf(entity.getId()) : null)
                .code(entity.getCode())
                .phone(phone)
                .memberId(memberId)
                .memberName(entity.getMemberName())
                .avatar(entity.getAvatar())
                .membershipName(entity.getMembershipName())
                .time(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "SUCCESS")
                .reason(entity.getReason())
                .warningLevel(warningLevel)
                .staffId(staffId)
                .staffCode(staffCode)
                .staffName(staffName)
                .build();
    }
}
