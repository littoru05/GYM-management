package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.MemberDto;
import com.example.GYM_management_api.dtos.SubscriptionRequestDto;
import com.example.GYM_management_api.entities.Member;
import com.example.GYM_management_api.entities.MemberSubscription;
import com.example.GYM_management_api.entities.Membership;
import com.example.GYM_management_api.entities.enums.MemberStatus;
import com.example.GYM_management_api.entities.enums.SubscriptionStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.MemberMapper;
import com.example.GYM_management_api.repositories.MemberRepository;
import com.example.GYM_management_api.repositories.MemberSubscriptionRepository;
import com.example.GYM_management_api.repositories.MembershipRepository;
import com.example.GYM_management_api.services.IMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImpl implements IMemberService {

    private final MemberRepository memberRepository;
    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final MembershipRepository membershipRepository;
    private final MemberMapper memberMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<MemberDto> getMembers(int page, int size, String keyword, String status) {
        int pageIndex = Math.max(page, 0);
        int pageSize = size > 0 ? size : 10;

        // Sắp xếp theo thời gian tạo mới nhất
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

        String trimmedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String trimmedStatus = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;

        Page<Member> memberPage;
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(7);

        if (trimmedStatus == null) {
            memberPage = memberRepository.searchMembers(trimmedKeyword, pageable);
        } else {
            switch (trimmedStatus) {
                case "LOCKED":
                    memberPage = memberRepository.searchLockedMembers(trimmedKeyword, pageable);
                    break;
                case "EXPIRED":
                    memberPage = memberRepository.searchExpiredMembers(trimmedKeyword, today, pageable);
                    break;
                case "EXPIRING_SOON":
                    memberPage = memberRepository.searchExpiringSoonMembers(trimmedKeyword, today, soon, pageable);
                    break;
                case "ACTIVE":
                    memberPage = memberRepository.searchActiveMembers(trimmedKeyword, soon, pageable);
                    break;
                default:
                    memberPage = memberRepository.searchMembers(trimmedKeyword, pageable);
            }
        }

        return memberPage.map(memberMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDto getMemberByIdOrCode(String idOrCode) {
        if (idOrCode == null || idOrCode.isBlank()) {
            throw new BadRequestException("ID hoặc mã hội viên không được để trống.");
        }

        String search = idOrCode.trim();
        Member member = null;

        // Thử tìm theo ID số trước
        try {
            Long id = Long.parseLong(search);
            member = memberRepository.findById(id).orElse(null);
        } catch (NumberFormatException ignored) {
            // Không phải ID số, tiếp tục tìm theo mã
        }

        // Nếu chưa tìm thấy, tìm theo mã hội viên code
        if (member == null) {
            member = memberRepository.findByCode(search)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên với ID hoặc mã: " + search));
        }

        // Nạp lịch sử gói tập nếu có
        List<MemberSubscription> subscriptions = memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(member.getId());
        if (subscriptions != null && !subscriptions.isEmpty()) {
            member.setSubscriptions(subscriptions);
        }

        return memberMapper.toDto(member);
    }

    @Override
    public MemberDto createMember(MemberDto request) {
        // Đảm bảo không mang ID client gửi lên để database tự sinh IDENTITY
        request.setId(null);

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Họ tên hội viên không được để trống.");
        }

        String phone = normalizePhone(request.getPhone());
        validatePhoneFormat(phone);

        // Kiểm tra trùng lặp số điện thoại
        if (memberRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Số điện thoại '" + phone + "' đã tồn tại trong hệ thống.");
        }

        // Tự động sinh mã hội viên duy nhất theo quy chuẩn định sẵn (ví dụ: M0001)
        String generatedCode = generateNextMemberCode();

        Member entity = memberMapper.toEntity(request, generatedCode);
        entity.setPhone(phone);

        Member saved = memberRepository.save(entity);
        log.info("Đã tạo mới hồ sơ hội viên ID: {}, Mã: {}, Tên: {}, SĐT: {}",
                saved.getId(), saved.getCode(), saved.getName(), saved.getPhone());

        return memberMapper.toDto(saved);
    }

    @Override
    public MemberDto updateMember(Long id, MemberDto request) {
        Member existing = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên với ID: " + id));

        // Kiểm tra và validate số điện thoại nếu có cập nhật
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String newPhone = normalizePhone(request.getPhone());
            validatePhoneFormat(newPhone);

            if (memberRepository.existsByPhoneAndIdNot(newPhone, id)) {
                throw new DuplicateResourceException("Số điện thoại '" + newPhone + "' đã được sử dụng bởi hội viên khác.");
            }
            request.setPhone(newPhone);
        }

        memberMapper.updateEntityFromDto(request, existing);
        Member updated = memberRepository.save(existing);
        log.info("Đã cập nhật hồ sơ hội viên ID: {}, Mã: {}, Tên: {}", updated.getId(), updated.getCode(), updated.getName());

        List<MemberSubscription> subscriptions = memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(updated.getId());
        if (subscriptions != null && !subscriptions.isEmpty()) {
            updated.setSubscriptions(subscriptions);
        }

        return memberMapper.toDto(updated);
    }

    @Override
    public void deleteMember(Long id) {
        Member existing = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên với ID: " + id));

        List<MemberSubscription> subscriptions = memberSubscriptionRepository.findByMemberIdOrderByCreatedAtDesc(id);
        if (subscriptions != null && !subscriptions.isEmpty()) {
            // Xóa mềm: Khóa tài khoản hội viên vì đã có lịch sử đăng ký gói tập
            existing.setStatus(MemberStatus.LOCKED);
            memberRepository.save(existing);
            log.info("Hội viên ID: {} đã có lịch sử gói tập, đã chuyển trạng thái sang LOCKED (Xóa mềm).", id);
        } else {
            // Xóa cứng nếu chưa từng có lịch sử sử dụng
            memberRepository.delete(existing);
            log.info("Đã xóa vĩnh viễn hồ sơ hội viên ID: {}.", id);
        }
    }

    @Override
    public MemberDto registerSubscription(Long memberId, SubscriptionRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên với ID: " + memberId));

        Membership membership = membershipRepository.findById(request.getMembershipId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + request.getMembershipId()));

        if (request.getStartDate() == null) {
            throw new BadRequestException("Vui lòng chọn ngày bắt đầu kích hoạt gói tập.");
        }

        MemberSubscription subscription = MemberSubscription.builder()
                .code(generateNextSubscriptionCode())
                .member(member)
                .membership(membership)
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusMonths(membership.getDurationMonths()))
                .paidPrice(membership.getPrice())
                .status(SubscriptionStatus.ACTIVE)
                .notes(request.getNotes())
                .build();
        memberSubscriptionRepository.save(subscription);

        if (member.getStatus() == MemberStatus.LOCKED) {
            member.setStatus(MemberStatus.ACTIVE);
            memberRepository.save(member);
        }

        log.info("Đã đăng ký gói tập '{}' cho hội viên ID: {}, mã đăng ký: {}",
                membership.getName(), memberId, subscription.getCode());

        Member reloaded = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên với ID: " + memberId));
        List<MemberSubscription> subscriptions = memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(memberId);
        reloaded.setSubscriptions(subscriptions);

        return memberMapper.toDto(reloaded);
    }

    private synchronized String generateNextSubscriptionCode() {
        long seq = memberSubscriptionRepository.count() + 1;
        String code = String.format("SUB-%04d", seq);
        while (memberSubscriptionRepository.existsByCode(code)) {
            seq++;
            code = String.format("SUB-%04d", seq);
        }
        return code;
    }

    private synchronized String generateNextMemberCode() {
        long seq = 1;
        String code = String.format("M%04d", seq);
        while (memberRepository.existsByCode(code)) {
            seq++;
            code = String.format("M%04d", seq);
        }
        return code;
    }

    private String normalizePhone(String rawPhone) {
        if (rawPhone == null) return null;
        String phone = rawPhone.trim().replaceAll("[\\s-.]", "");
        if (phone.startsWith("+84")) {
            phone = "0" + phone.substring(3);
        } else if (phone.startsWith("84") && phone.length() == 11) {
            phone = "0" + phone.substring(2);
        }
        return phone;
    }

    private void validatePhoneFormat(String phone) {
        if (phone == null || !phone.matches("^0[0-9]{9}$")) {
            throw new BadRequestException("Số điện thoại không đúng định dạng chuẩn Việt Nam (10 chữ số bắt đầu bằng số 0).");
        }
    }
}
