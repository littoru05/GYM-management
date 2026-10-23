package com.example.GYM_management_api.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.GYM_management_api.dtos.CheckInDto;
import com.example.GYM_management_api.entities.CheckIn;
import com.example.GYM_management_api.entities.Member;
import com.example.GYM_management_api.entities.MemberSubscription;
import com.example.GYM_management_api.entities.Membership;
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
import com.example.GYM_management_api.services.impl.CheckInServiceImpl;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ CheckInServiceImpl (Tầng Service - Quản lý Điểm danh / Check-in)")
class CheckInServiceImplTest {

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberSubscriptionRepository memberSubscriptionRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private CheckInMapper checkInMapper;

    @InjectMocks
    private CheckInServiceImpl checkInService;

    private Member sampleMember;
    private Membership sampleMembership;
    private MemberSubscription activeSub;
    private CheckIn sampleCheckIn;
    private CheckInDto sampleCheckInDto;

    @BeforeEach
    void setUp() {
        sampleMember = Member.builder()
                .code("M0001")
                .name("Nguyen Van A")
                .phone("0900000001")
                .status(MemberStatus.ACTIVE)
                .build();
        sampleMember.setId(1L);

        sampleMembership = Membership.builder()
                .name("Gói 1 Tháng")
                .build();
        sampleMembership.setId(10L);

        activeSub = MemberSubscription.builder()
                .code("SUB0001")
                .member(sampleMember)
                .membership(sampleMembership)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(25))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        sampleCheckIn = CheckIn.builder()
                .code("CI-0001")
                .member(sampleMember)
                .memberName("Nguyen Van A")
                .subscription(activeSub)
                .membershipName("Gói 1 Tháng")
                .status(CheckInStatus.SUCCESS)
                .checkInTime(LocalDateTime.now())
                .build();
        sampleCheckIn.setId(100L);

        sampleCheckInDto = CheckInDto.builder()
                .id("100")
                .code("CI-0001")
                .memberId("1")
                .memberName("Nguyen Van A")
                .membershipName("Gói 1 Tháng")
                .status("SUCCESS")
                .time(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("TC-93: Báo lỗi BadRequestException khi request check-in là null")
    void checkIn_NullRequest_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> checkInService.checkIn(null));
    }

    @Test
    @DisplayName("TC-94: Báo lỗi BadRequestException khi không cung cấp bất kỳ định danh nào (phone, memberId, code)")
    void checkIn_MissingIdentifier_ThrowsBadRequest() {
        CheckInDto req = CheckInDto.builder().phone(null).memberId(null).code(null).build();
        assertThrows(BadRequestException.class, () -> checkInService.checkIn(req));

        CheckInDto reqBlank = CheckInDto.builder().phone("  ").memberId("").code("").build();
        assertThrows(BadRequestException.class, () -> checkInService.checkIn(reqBlank));
    }

    @Test
    @DisplayName("TC-95: Báo lỗi ResourceNotFoundException khi không tìm thấy hội viên theo SĐT hoặc mã")
    void checkIn_MemberNotFound_ThrowsResourceNotFound() {
        CheckInDto req = CheckInDto.builder().phone("0999999999").build();
        when(memberRepository.findByPhone("0999999999")).thenReturn(Optional.empty());
        when(memberRepository.findByCode("0999999999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkInService.checkIn(req));
    }

    @Test
    @DisplayName("TC-96: Báo lỗi BadRequestException khi tài khoản hội viên đang bị tạm khóa (LOCKED)")
    void checkIn_MemberLocked_ThrowsBadRequest() {
        sampleMember.setStatus(MemberStatus.LOCKED);
        CheckInDto req = CheckInDto.builder().phone("0900000001").build();

        when(memberRepository.findByPhone("0900000001")).thenReturn(Optional.of(sampleMember));

        assertThrows(BadRequestException.class, () -> checkInService.checkIn(req));
    }

    @Test
    @DisplayName("TC-97: Báo lỗi BadRequestException khi hội viên chưa từng đăng ký bất kỳ gói tập nào")
    void checkIn_NoSubscriptions_ThrowsBadRequest() {
        CheckInDto req = CheckInDto.builder().phone("0900000001").build();

        when(memberRepository.findByPhone("0900000001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> checkInService.checkIn(req));
    }

    @Test
    @DisplayName("TC-98: Báo lỗi BadRequestException khi gói tập của hội viên chưa đến ngày kích hoạt (ở tương lai)")
    void checkIn_FutureSubscription_ThrowsBadRequest() {
        MemberSubscription futureSub = MemberSubscription.builder()
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        CheckInDto req = CheckInDto.builder().phone("0900000001").build();

        when(memberRepository.findByPhone("0900000001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(List.of(futureSub));

        assertThrows(BadRequestException.class, () -> checkInService.checkIn(req));
    }

    @Test
    @DisplayName("TC-99: Báo lỗi BadRequestException khi gói tập của hội viên đã hết hạn")
    void checkIn_ExpiredSubscription_ThrowsBadRequest() {
        MemberSubscription expiredSub = MemberSubscription.builder()
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().minusDays(1))
                .status(SubscriptionStatus.EXPIRED)
                .build();

        CheckInDto req = CheckInDto.builder().phone("0900000001").build();

        when(memberRepository.findByPhone("0900000001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(List.of(expiredSub));

        assertThrows(BadRequestException.class, () -> checkInService.checkIn(req));
    }

    @Test
    @DisplayName("TC-100: Báo lỗi BadRequestException chống gian lận (Anti-fraud) khi quẹt thẻ 2 lần trong vòng 5 phút")
    void checkIn_AntiFraudDuplicateWithin5Minutes_ThrowsBadRequest() {
        CheckIn recentCheckIn = CheckIn.builder()
                .checkInTime(LocalDateTime.now().minusMinutes(2))
                .status(CheckInStatus.SUCCESS)
                .build();

        CheckInDto req = CheckInDto.builder().phone("0900000001").build();

        when(memberRepository.findByPhone("0900000001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(List.of(activeSub));
        when(checkInRepository.findTopByMemberIdAndStatusOrderByCheckInTimeDesc(1L, CheckInStatus.SUCCESS))
                .thenReturn(Optional.of(recentCheckIn));

        assertThrows(BadRequestException.class, () -> checkInService.checkIn(req));
        verify(checkInRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-101: Điểm danh thành công bằng Số điện thoại khi gói tập hợp lệ và không trùng lặp")
    void checkIn_Success_ByPhone() {
        CheckInDto req = CheckInDto.builder().phone("0900000001").build();

        when(memberRepository.findByPhone("0900000001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(List.of(activeSub));
        when(checkInRepository.findTopByMemberIdAndStatusOrderByCheckInTimeDesc(1L, CheckInStatus.SUCCESS))
                .thenReturn(Optional.empty());
        when(checkInRepository.existsByCode(anyString())).thenReturn(false);
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(sampleCheckIn);
        when(checkInMapper.toDto(sampleCheckIn)).thenReturn(sampleCheckInDto);

        CheckInDto result = checkInService.checkIn(req);

        assertNotNull(result);
        assertEquals("CI-0001", result.getCode());
        assertEquals("SUCCESS", result.getStatus());
        verify(checkInRepository, times(1)).save(any(CheckIn.class));
    }

    @Test
    @DisplayName("TC-102: Điểm danh thành công bằng Mã hội viên (M0001)")
    void checkIn_Success_ByMemberCode() {
        CheckInDto req = CheckInDto.builder().phone("M0001").build();

        when(memberRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(memberRepository.findByCode("M0001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(List.of(activeSub));
        when(checkInRepository.findTopByMemberIdAndStatusOrderByCheckInTimeDesc(1L, CheckInStatus.SUCCESS))
                .thenReturn(Optional.empty());
        when(checkInRepository.existsByCode(anyString())).thenReturn(false);
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(sampleCheckIn);
        when(checkInMapper.toDto(sampleCheckIn)).thenReturn(sampleCheckInDto);

        CheckInDto result = checkInService.checkIn(req);

        assertNotNull(result);
        assertEquals("M0001", sampleMember.getCode());
    }

    @Test
    @DisplayName("TC-103: Điểm danh thành công khi lần check-in trước đã cách hơn 5 phút (hợp lệ)")
    void checkIn_Success_After5Minutes() {
        CheckIn olderCheckIn = CheckIn.builder()
                .checkInTime(LocalDateTime.now().minusMinutes(10))
                .status(CheckInStatus.SUCCESS)
                .build();

        CheckInDto req = CheckInDto.builder().phone("0900000001").build();

        when(memberRepository.findByPhone("0900000001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(List.of(activeSub));
        when(checkInRepository.findTopByMemberIdAndStatusOrderByCheckInTimeDesc(1L, CheckInStatus.SUCCESS))
                .thenReturn(Optional.of(olderCheckIn));
        when(checkInRepository.existsByCode(anyString())).thenReturn(false);
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(sampleCheckIn);
        when(checkInMapper.toDto(sampleCheckIn)).thenReturn(sampleCheckInDto);

        CheckInDto result = checkInService.checkIn(req);

        assertNotNull(result);
        verify(checkInRepository, times(1)).save(any(CheckIn.class));
    }

    @Test
    @DisplayName("TC-104: Lấy danh sách điểm danh hôm nay thành công")
    void getTodayCheckIns_Success() {
        when(checkInRepository.findByCheckInTimeBetweenOrderByCheckInTimeDesc(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(sampleCheckIn));
        when(checkInMapper.toDto(sampleCheckIn)).thenReturn(sampleCheckInDto);

        List<CheckInDto> list = checkInService.getTodayCheckIns();

        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("TC-105: Lấy toàn bộ lịch sử điểm danh thành công")
    void getAllCheckIns_Success() {
        when(checkInRepository.findAllByOrderByCheckInTimeDesc()).thenReturn(List.of(sampleCheckIn));
        when(checkInMapper.toDto(sampleCheckIn)).thenReturn(sampleCheckInDto);

        List<CheckInDto> list = checkInService.getAllCheckIns();

        assertNotNull(list);
        assertEquals(1, list.size());
    }
}
