package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.MembershipDto;
import com.example.GYM_management_api.entities.Membership;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.MembershipMapper;
import com.example.GYM_management_api.repositories.MemberSubscriptionRepository;
import com.example.GYM_management_api.repositories.MembershipRepository;
import com.example.GYM_management_api.repositories.TransactionDetailRepository;
import com.example.GYM_management_api.services.IMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MembershipServiceImpl implements IMembershipService {

    private final MembershipRepository membershipRepository;
    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final TransactionDetailRepository transactionDetailRepository;
    private final MembershipMapper membershipMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MembershipDto> getAllMemberships(String status) {
        List<Membership> memberships;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            try {
                CommonStatus commonStatus = CommonStatus.valueOf(status.trim().toUpperCase());
                memberships = membershipRepository.findByStatus(commonStatus);
            } catch (IllegalArgumentException e) {
                memberships = membershipRepository.findAll();
            }
        } else {
            memberships = membershipRepository.findAll();
        }

        return memberships.stream()
                .map(membershipMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipDto getMembershipById(Long id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + id));
        return membershipMapper.toDto(membership);
    }

    @Override
    public MembershipDto createMembership(MembershipDto dto) {
        // Đảm bảo không mang ID client gửi lên để database tự sinh IDENTITY
        dto.setId(null);

        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Tên gói tập không được để trống.");
        }

        Integer duration = dto.getDurationMonths() != null ? dto.getDurationMonths() : dto.getDuration();
        if (duration == null || duration <= 0) {
            throw new BadRequestException("Thời hạn gói tập phải lớn hơn 0.");
        }

        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Đơn giá niêm yết không được để trống và không được âm.");
        }

        String name = dto.getName().trim();
        if (membershipRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Tên gói tập '" + name + "' đã tồn tại trong hệ thống.");
        }

        String code = dto.getCode();
        if (code == null || code.isBlank()) {
            code = "PKG-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } else {
            code = code.trim().toUpperCase();
            if (membershipRepository.existsByCode(code)) {
                throw new DuplicateResourceException("Mã gói tập '" + code + "' đã tồn tại trong hệ thống.");
            }
        }
        dto.setCode(code);

        Membership entity = membershipMapper.toEntity(dto);
        Membership saved = membershipRepository.save(entity);
        log.info("Đã tạo mới gói tập ID: {}, Mã: {}, Tên: {}", saved.getId(), saved.getCode(), saved.getName());
        return membershipMapper.toDto(saved);
    }

    @Override
    public MembershipDto updateMembership(Long id, MembershipDto dto) {
        Membership existing = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + id));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String newName = dto.getName().trim();
            if (membershipRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
                throw new DuplicateResourceException("Tên gói tập '" + newName + "' đã được sử dụng bởi gói tập khác.");
            }
        }

        Integer duration = dto.getDurationMonths() != null ? dto.getDurationMonths() : dto.getDuration();
        if (duration != null && duration <= 0) {
            throw new BadRequestException("Thời hạn gói tập phải lớn hơn 0.");
        }

        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Đơn giá niêm yết không được âm.");
        }

        membershipMapper.updateEntityFromDto(dto, existing);
        Membership updated = membershipRepository.save(existing);
        log.info("Đã cập nhật gói tập ID: {}, Tên: {}", updated.getId(), updated.getName());
        return membershipMapper.toDto(updated);
    }

    @Override
    public void deleteMembership(Long id) {
        Membership existing = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + id));

        boolean hasSubscriptions = memberSubscriptionRepository.existsByMembershipId(id);
        boolean hasTransactions = transactionDetailRepository.existsByMembershipId(id);

        if (hasSubscriptions || hasTransactions) {
            // Xóa mềm: Chuyển sang INACTIVE để bảo vệ dữ liệu kế toán và đăng ký của hội viên
            existing.setStatus(CommonStatus.INACTIVE);
            membershipRepository.save(existing);
            log.info("Gói tập ID: {} đã có dữ liệu sử dụng, đã chuyển sang trạng thái INACTIVE (Xóa mềm).", id);
        } else {
            // Xóa cứng nếu chưa từng có phát sinh sử dụng
            membershipRepository.delete(existing);
            log.info("Đã xóa vĩnh viễn gói tập ID: {}.", id);
        }
    }
}
