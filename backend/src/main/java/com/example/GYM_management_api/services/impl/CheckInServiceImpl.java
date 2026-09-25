package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.CheckInDto;
import com.example.GYM_management_api.entities.CheckIn;
import com.example.GYM_management_api.entities.Member;
import com.example.GYM_management_api.entities.MemberSubscription;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.CheckInStatus;
import com.example.GYM_management_api.entities.enums.MemberStatus;
import com.example.GYM_management_api.entities.enums.SubscriptionStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.CheckInMapper;
import com.example.GYM_management_api.repositories.CheckInRepository;
import com.example.GYM_management_api.repositories.MemberRepository;
import com.example.GYM_management_api.repositories.MemberSubscriptionRepository;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.services.ICheckInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CheckInServiceImpl implements ICheckInService {

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final MemberSubscriptionRepository memberSubscriptionRepository;
    private final StaffRepository staffRepository;
    private final CheckInMapper checkInMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public CheckInDto checkIn(CheckInDto request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu điểm danh không được để trống.");
        }

        String rawIdentifier = request.getPhone();
        if (rawIdentifier == null || rawIdentifier.isBlank()) {
            rawIdentifier = request.getMemberId();
        }
        if (rawIdentifier == null || rawIdentifier.isBlank()) {
            rawIdentifier = request.getCode();
        }

        if (rawIdentifier == null || rawIdentifier.isBlank()) {
            throw new BadRequestException("Số điện thoại hoặc mã hội viên không được để trống.");
        }

        // 1. Tra cứu hội viên theo số điện thoại hoặc mã hội viên
        Member member = findMemberByIdentifier(rawIdentifier);

        // 2. Kiểm tra tài khoản hội viên
        if (member.getStatus() == MemberStatus.LOCKED) {
            log.warn("Từ chối check-in: Hội viên {} ({}) đang bị tạm khóa", member.getName(), member.getCode());
            throw new BadRequestException("Tài khoản hội viên đang bị tạm khóa. Vui lòng liên hệ ban quản lý phòng tập.");
        }

        // 3. Kiểm tra điều kiện gói tập
        LocalDate today = LocalDate.now();
        List<MemberSubscription> subscriptions = memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(member.getId());

        if (subscriptions.isEmpty()) {
            log.warn("Từ chối check-in: Hội viên {} ({}) chưa mua gói tập nào", member.getName(), member.getCode());
            throw new BadRequestException("Hội viên chưa đăng ký bất kỳ gói tập nào trong hệ thống.");
        }

        // Tìm gói tập ACTIVE và đang trong khoảng thời gian hợp lệ (start_date <= today <= end_date)
        MemberSubscription activeValidSubscription = subscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .filter(s -> !s.getStartDate().isAfter(today) && !s.getEndDate().isBefore(today))
                .max(java.util.Comparator.comparing(MemberSubscription::getEndDate))
                .orElse(null);

        if (activeValidSubscription == null) {
            // Xác định lý do từ chối cụ thể để thông báo rõ ràng cho quầy lễ tân
            MemberSubscription latestExpired = subscriptions.stream()
                    .filter(s -> s.getEndDate().isBefore(today))
                    .max(java.util.Comparator.comparing(MemberSubscription::getEndDate))
                    .orElse(null);

            MemberSubscription futureSub = subscriptions.stream()
                    .filter(s -> s.getStartDate().isAfter(today) && s.getStatus() == SubscriptionStatus.ACTIVE)
                    .min(java.util.Comparator.comparing(MemberSubscription::getStartDate))
                    .orElse(null);

            if (futureSub != null && latestExpired == null) {
                String startFormatted = futureSub.getStartDate().format(DATE_FORMATTER);
                log.warn("Từ chối check-in: Gói tập của hội viên {} chưa đến ngày kích hoạt ({})", member.getCode(), startFormatted);
                throw new BadRequestException("Gói tập của hội viên chưa đến ngày kích hoạt (bắt đầu từ ngày " + startFormatted + ").");
            } else if (latestExpired != null) {
                String expiryFormatted = latestExpired.getEndDate().format(DATE_FORMATTER);
                log.warn("Từ chối check-in: Gói tập của hội viên {} đã hết hạn vào ngày {}", member.getCode(), expiryFormatted);
                throw new BadRequestException("Gói tập của hội viên đã hết hạn vào ngày " + expiryFormatted + ". Vui lòng gia hạn gói tập để tiếp tục vào phòng.");
            } else {
                log.warn("Từ chối check-in: Hội viên {} không có gói tập ACTIVE hợp lệ", member.getCode());
                throw new BadRequestException("Hội viên không có gói tập nào đang hoạt động hợp lệ.");
            }
        }

        // 4. Ngăn chặn gian lận quẹt thẻ liên tiếp (Anti-fraud / Double check-in)
        Optional<CheckIn> lastCheckInOpt = checkInRepository.findTopByMemberIdAndStatusOrderByCheckInTimeDesc(member.getId(), CheckInStatus.SUCCESS);
        if (lastCheckInOpt.isPresent()) {
            LocalDateTime lastCheckInTime = lastCheckInOpt.get().getCheckInTime();
            // Nếu hội viên vừa check-in trong vòng 5 phút qua
            if (lastCheckInTime != null && lastCheckInTime.isAfter(LocalDateTime.now().minusMinutes(5))) {
                String timeFormatted = lastCheckInTime.format(TIME_FORMATTER);
                log.warn("Cảnh báo gian lận quẹt thẻ: Hội viên {} ({}) vừa check-in lúc {}", member.getName(), member.getCode(), timeFormatted);
                throw new BadRequestException("Cảnh báo quẹt thẻ trùng lặp: Hội viên vừa điểm danh lúc " + timeFormatted
                        + ". Vui lòng không quẹt thẻ nhiều lần liên tiếp để ngăn ngừa gian lận chuyển thẻ.");
            }
        }

        // 5. Thẻ hợp lệ -> Ghi nhận lượt check-in mới
        String checkInCode = generateNextCheckInCode();
        String membershipName = (activeValidSubscription.getMembership() != null)
                ? activeValidSubscription.getMembership().getName()
                : "Gói tập";

        String reason = (request.getReason() != null && !request.getReason().isBlank())
                ? request.getReason().trim()
                : "Điểm danh thành công";

        Staff staff = getCurrentAuthenticatedStaff();
        String staffName = (staff != null) ? staff.getName() : "Quầy lễ tân";

        CheckIn checkIn = CheckIn.builder()
                .code(checkInCode)
                .member(member)
                .memberName(member.getName())
                .avatar(member.getAvatar())
                .subscription(activeValidSubscription)
                .membershipName(membershipName)
                .staff(staff)
                .staffName(staffName)
                .checkInTime(LocalDateTime.now())
                .status(CheckInStatus.SUCCESS)
                .reason(reason)
                .build();

        CheckIn saved = checkInRepository.save(checkIn);
        log.info("Check-in thành công: Mã: {}, Hội viên: {} ({}), Gói: {}",
                saved.getCode(), member.getName(), member.getCode(), membershipName);

        return checkInMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckInDto> getTodayCheckIns() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59, 999999999);

        List<CheckIn> todayCheckIns = checkInRepository.findByCheckInTimeBetweenOrderByCheckInTimeDesc(startOfDay, endOfDay);
        return todayCheckIns.stream().map(checkInMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckInDto> getAllCheckIns() {
        List<CheckIn> list = checkInRepository.findAllByOrderByCheckInTimeDesc();
        return list.stream().map(checkInMapper::toDto).collect(Collectors.toList());
    }

    private Member findMemberByIdentifier(String rawIdentifier) {
        String identifier = rawIdentifier.trim();

        // 1. Thử tìm theo số điện thoại đã chuẩn hóa
        String normalizedPhone = normalizePhone(identifier);
        Optional<Member> memberByPhone = memberRepository.findByPhone(normalizedPhone);
        if (memberByPhone.isPresent()) {
            return memberByPhone.get();
        }

        // 2. Thử tìm theo mã hội viên code
        Optional<Member> memberByCode = memberRepository.findByCode(identifier.toUpperCase());
        if (memberByCode.isPresent()) {
            return memberByCode.get();
        }

        // 3. Thử tìm theo ID số
        try {
            Long id = Long.parseLong(identifier);
            Optional<Member> memberById = memberRepository.findById(id);
            if (memberById.isPresent()) {
                return memberById.get();
            }
        } catch (NumberFormatException ignored) {}

        throw new ResourceNotFoundException("Không tìm thấy hội viên với mã hoặc số điện thoại: " + identifier);
    }

    private String normalizePhone(String rawPhone) {
        if (rawPhone == null) return "";
        String phone = rawPhone.trim().replaceAll("[\\s-.]", "");
        if (phone.startsWith("+84")) {
            phone = "0" + phone.substring(3);
        } else if (phone.startsWith("84") && phone.length() == 11) {
            phone = "0" + phone.substring(2);
        }
        return phone;
    }

    private synchronized String generateNextCheckInCode() {
        long seq = 1;
        String code = "CI" + String.format("%06d", (System.currentTimeMillis() % 1000000));
        while (checkInRepository.existsByCode(code)) {
            seq++;
            code = "CI" + String.format("%06d", ((System.currentTimeMillis() + seq) % 1000000));
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

