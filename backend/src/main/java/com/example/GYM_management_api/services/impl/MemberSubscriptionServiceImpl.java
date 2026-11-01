package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.MemberSubscriptionDto;
import com.example.GYM_management_api.dtos.VoucherValidateResponseDto;
import com.example.GYM_management_api.entities.Member;
import com.example.GYM_management_api.entities.MemberSubscription;
import com.example.GYM_management_api.entities.Membership;
import com.example.GYM_management_api.entities.Promotion;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.Transaction;
import com.example.GYM_management_api.entities.VoucherUsage;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.entities.enums.MemberStatus;
import com.example.GYM_management_api.entities.enums.PaymentMethod;
import com.example.GYM_management_api.entities.enums.SubscriptionStatus;
import com.example.GYM_management_api.entities.enums.TransactionType;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.MemberSubscriptionMapper;
import com.example.GYM_management_api.repositories.MemberRepository;
import com.example.GYM_management_api.repositories.MemberSubscriptionRepository;
import com.example.GYM_management_api.repositories.MembershipRepository;
import com.example.GYM_management_api.repositories.PromotionRepository;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.repositories.TransactionRepository;
import com.example.GYM_management_api.repositories.VoucherUsageRepository;
import com.example.GYM_management_api.services.IMemberSubscriptionService;
import com.example.GYM_management_api.services.IVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberSubscriptionServiceImpl implements IMemberSubscriptionService {

    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final StaffRepository staffRepository;
    private final TransactionRepository transactionRepository;
    private final MemberSubscriptionMapper memberSubscriptionMapper;
    private final IVoucherService voucherService;
    private final PromotionRepository promotionRepository;
    private final VoucherUsageRepository voucherUsageRepository;

    @Override
    public MemberSubscriptionDto createSubscription(MemberSubscriptionDto request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu đăng ký gói tập không được để trống.");
        }

        // 1. Xác thực hội viên
        Member member = resolveMember(request);
        if (member.getStatus() == MemberStatus.LOCKED) {
            throw new BadRequestException("Tài khoản hội viên đang bị tạm khóa, không thể kích hoạt gói tập.");
        }

        // 2. Xác thực gói tập
        if (request.getMembershipId() == null) {
            throw new BadRequestException("ID gói tập không được để trống.");
        }
        Membership membership = membershipRepository.findById(request.getMembershipId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói tập với ID: " + request.getMembershipId()));

        if (membership.getStatus() != CommonStatus.ACTIVE) {
            throw new BadRequestException("Gói tập '" + membership.getName() + "' hiện không còn kinh doanh hoặc đã ngưng áp dụng.");
        }

        // 3. Tính toán ngày bắt đầu và kết thúc (Xử lý gia hạn cộng dồn)
        LocalDate today = LocalDate.now();
        List<MemberSubscription> activeSubs = memberSubscriptionRepository
                .findByMemberIdAndStatusOrderByEndDateDesc(member.getId(), SubscriptionStatus.ACTIVE);

        LocalDate latestActiveExpiry = null;
        if (!activeSubs.isEmpty()) {
            latestActiveExpiry = activeSubs.get(0).getEndDate();
        }

        LocalDate actualStartDate;
        if (latestActiveExpiry != null && !latestActiveExpiry.isBefore(today)) {
            // Gia hạn: Hội viên vẫn còn gói tập cũ còn hạn -> ngày bắt đầu tự động cộng dồn tiếp nối
            actualStartDate = latestActiveExpiry.plusDays(1);
            log.info("Hội viên ID {} (Mã {}) gia hạn gói tập. Ngày bắt đầu tự động cộng dồn tiếp nối sau {}: {}",
                    member.getId(), member.getCode(), latestActiveExpiry, actualStartDate);
        } else {
            // Đăng ký mới hoặc gói cũ đã hết hạn
            actualStartDate = (request.getStartDate() != null && !request.getStartDate().isBefore(today))
                    ? request.getStartDate()
                    : today;
        }

        int durationMonths = membership.getDurationMonths() != null ? membership.getDurationMonths() : 1;
        LocalDate actualEndDate = actualStartDate.plusMonths(durationMonths);

        // 4. Giá thanh toán & Xử lý voucher giảm giá
        BigDecimal subtotal = membership.getPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal paidPrice = subtotal;
        Promotion appliedPromotion = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            VoucherValidateResponseDto voucherInfo = voucherService.validateVoucherInternal(
                    request.getVoucherCode(),
                    membership.getId(),
                    subtotal,
                    member.getId()
            );
            discountAmount = voucherInfo.getDiscountAmount();
            paidPrice = voucherInfo.getFinalPrice();

            // Lấy entity promotion để liên kết và cập nhật usedCount
            appliedPromotion = promotionRepository.findByCodeIgnoreCase(voucherInfo.getCode()).orElse(null);
            if (appliedPromotion != null) {
                int newUsedCount = (appliedPromotion.getUsedCount() != null ? appliedPromotion.getUsedCount() : 0) + 1;
                appliedPromotion.setUsedCount(newUsedCount);
                promotionRepository.save(appliedPromotion);
            }
        } else if (request.getPaidPrice() != null && request.getPaidPrice().compareTo(BigDecimal.ZERO) >= 0) {
            paidPrice = request.getPaidPrice();
            if (paidPrice.compareTo(subtotal) < 0) {
                discountAmount = subtotal.subtract(paidPrice);
            }
        }

        PaymentMethod paymentMethod = PaymentMethod.CASH;
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            try {
                paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Phương thức thanh toán '" + request.getPaymentMethod() + "' không hợp lệ (hỗ trợ: CASH, BANK_TRANSFER, CARD, E_WALLET).");
            }
        }

        // 5. Lưu giao dịch tài chính Transaction
        Staff staff = getCurrentAuthenticatedStaff();
        String staffName = (staff != null) ? staff.getName() : "Quầy lễ tân";

        String description = "Kích hoạt hợp đồng gói tập: " + membership.getName() + " (" + durationMonths + " tháng)";
        if (appliedPromotion != null) {
            description += " [Voucher: " + appliedPromotion.getCode() + "]";
        }

        Transaction transaction = Transaction.builder()
                .code("TXN" + String.format("%04d", (System.currentTimeMillis() % 1000000)))
                .transactionTime(LocalDateTime.now())
                .type(TransactionType.MEMBERSHIP)
                .member(member)
                .memberName(member.getName())
                .staff(staff)
                .staffName(staffName)
                .promotion(appliedPromotion)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .amount(paidPrice)
                .paymentMethod(paymentMethod)
                .description(description)
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Lưu lịch sử sử dụng voucher nếu có
        if (appliedPromotion != null) {
            VoucherUsage voucherUsage = VoucherUsage.builder()
                    .voucherCode(appliedPromotion.getCode())
                    .promotion(appliedPromotion)
                    .memberId(member.getId())
                    .membershipId(membership.getId())
                    .transactionId(savedTransaction.getId())
                    .discountAmount(discountAmount)
                    .createdAt(LocalDateTime.now())
                    .build();
            voucherUsageRepository.save(voucherUsage);
            log.info("Đã lưu lịch sử sử dụng voucher '{}' cho hội viên ID={}, giảm {}",
                    appliedPromotion.getCode(), member.getId(), discountAmount);
        }

        // 6. Lưu hợp đồng gói tập MemberSubscription
        String subCode = generateNextSubscriptionCode();
        String notes = request.getNotes() != null && !request.getNotes().isBlank()
                ? request.getNotes().trim()
                : "Đăng ký tại quầy lễ tân";

        MemberSubscription subscription = MemberSubscription.builder()
                .code(subCode)
                .member(member)
                .membership(membership)
                .transaction(savedTransaction)
                .startDate(actualStartDate)
                .endDate(actualEndDate)
                .paidPrice(paidPrice)
                .status(SubscriptionStatus.ACTIVE)
                .notes(notes)
                .build();

        MemberSubscription savedSub = memberSubscriptionRepository.save(subscription);

        // 7. Cập nhật trạng thái hội viên sang ACTIVE nếu chưa active
        if (member.getStatus() != MemberStatus.ACTIVE) {
            member.setStatus(MemberStatus.ACTIVE);
            memberRepository.save(member);
        }

        log.info("Kích hoạt thành công hợp đồng gói tập: Mã: {}, Hội viên: {}, Gói: {}, Hạn dùng: {} -> {}",
                savedSub.getCode(), member.getName(), membership.getName(), actualStartDate, actualEndDate);

        return memberSubscriptionMapper.toDto(savedSub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberSubscriptionDto> getSubscriptionsByMember(String memberIdOrCode) {
        if (memberIdOrCode == null || memberIdOrCode.isBlank()) {
            throw new BadRequestException("ID hoặc mã hội viên không được để trống.");
        }

        Member member = null;
        try {
            Long id = Long.parseLong(memberIdOrCode.trim());
            member = memberRepository.findById(id).orElse(null);
        } catch (NumberFormatException ignored) {}

        if (member == null) {
            member = memberRepository.findByCode(memberIdOrCode.trim().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên: " + memberIdOrCode));
        }

        List<MemberSubscription> list = memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(member.getId());
        return list.stream().map(memberSubscriptionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberSubscriptionDto> getAllSubscriptions() {
        List<MemberSubscription> list = memberSubscriptionRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(memberSubscriptionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MemberSubscriptionDto getSubscriptionById(Long id) {
        MemberSubscription sub = memberSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin gói tập với ID: " + id));
        return memberSubscriptionMapper.toDto(sub);
    }

    private Member resolveMember(MemberSubscriptionDto request) {
        if (request.getMemberId() != null) {
            return memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên với ID: " + request.getMemberId()));
        }

        if (request.getMemberCode() != null && !request.getMemberCode().isBlank()) {
            return memberRepository.findByCode(request.getMemberCode().trim().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hội viên với mã: " + request.getMemberCode()));
        }

        throw new BadRequestException("Vui lòng cung cấp ID (memberId) hoặc Mã hội viên (memberCode).");
    }

    private synchronized String generateNextSubscriptionCode() {
        long seq = 1;
        String code = String.format("SUB%04d", seq);
        while (memberSubscriptionRepository.existsByCode(code)) {
            seq++;
            code = String.format("SUB%04d", seq);
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
        } catch (Exception ignored) {}
        return null;
    }
}

