package com.example.GYM_management_api.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.GYM_management_api.dtos.MemberSubscriptionDto;
import com.example.GYM_management_api.entities.Member;
import com.example.GYM_management_api.entities.MemberSubscription;
import com.example.GYM_management_api.entities.Membership;
import com.example.GYM_management_api.entities.Transaction;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.entities.enums.MemberStatus;
import com.example.GYM_management_api.entities.enums.SubscriptionStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.MemberSubscriptionMapper;
import com.example.GYM_management_api.repositories.MemberRepository;
import com.example.GYM_management_api.repositories.MemberSubscriptionRepository;
import com.example.GYM_management_api.repositories.MembershipRepository;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.repositories.TransactionRepository;
import com.example.GYM_management_api.services.impl.MemberSubscriptionServiceImpl;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ MemberSubscriptionServiceImpl (Tầng Service - Quản lý Đăng ký Gói tập)")
class MemberSubscriptionServiceImplTest {

    @Mock
    private MemberSubscriptionRepository memberSubscriptionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MemberSubscriptionMapper memberSubscriptionMapper;

    @InjectMocks
    private MemberSubscriptionServiceImpl subscriptionService;

    private Member sampleMember;
    private Membership sampleMembership;
    private MemberSubscription sampleSubscription;
    private MemberSubscriptionDto sampleSubscriptionDto;

    @BeforeEach
    void setUp() {
        sampleMember = Member.builder()
                .code("M0001")
                .name("Nguyen Van A")
                .phone("0900000001")
                .status(MemberStatus.ACTIVE)
                .subscriptions(new ArrayList<>())
                .build();
        sampleMember.setId(1L);

        sampleMembership = Membership.builder()
                .code("PKG-1M")
                .name("Gói 1 Tháng")
                .durationMonths(1)
                .price(BigDecimal.valueOf(500000))
                .status(CommonStatus.ACTIVE)
                .build();
        sampleMembership.setId(10L);

        sampleSubscription = MemberSubscription.builder()
                .code("SUB0001")
                .member(sampleMember)
                .membership(sampleMembership)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .paidPrice(BigDecimal.valueOf(500000))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        sampleSubscription.setId(100L);

        sampleSubscriptionDto = MemberSubscriptionDto.builder()
                .id(100L)
                .code("SUB0001")
                .memberId(1L)
                .memberCode("M0001")
                .membershipId(10L)
                .membershipName("Gói 1 Tháng")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .paidPrice(BigDecimal.valueOf(500000))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-79: Báo lỗi BadRequestException khi request đăng ký gói tập là null")
    void createSubscription_NullRequest_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> subscriptionService.createSubscription(null));
    }

    @Test
    @DisplayName("TC-80: Báo lỗi BadRequestException khi tài khoản hội viên đang bị khóa (LOCKED)")
    void createSubscription_MemberLocked_ThrowsBadRequest() {
        sampleMember.setStatus(MemberStatus.LOCKED);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));

        MemberSubscriptionDto req = MemberSubscriptionDto.builder().memberId(1L).membershipId(10L).build();
        assertThrows(BadRequestException.class, () -> subscriptionService.createSubscription(req));
    }

    @Test
    @DisplayName("TC-81: Báo lỗi BadRequestException khi không truyền membershipId")
    void createSubscription_NullMembershipId_ThrowsBadRequest() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));

        MemberSubscriptionDto req = MemberSubscriptionDto.builder().memberId(1L).membershipId(null).build();
        assertThrows(BadRequestException.class, () -> subscriptionService.createSubscription(req));
    }

    @Test
    @DisplayName("TC-82: Báo lỗi ResourceNotFoundException khi gói tập không tồn tại")
    void createSubscription_MembershipNotFound_ThrowsResourceNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(membershipRepository.findById(999L)).thenReturn(Optional.empty());

        MemberSubscriptionDto req = MemberSubscriptionDto.builder().memberId(1L).membershipId(999L).build();
        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.createSubscription(req));
    }

    @Test
    @DisplayName("TC-83: Báo lỗi BadRequestException khi gói tập đã ngưng hoạt động (INACTIVE)")
    void createSubscription_MembershipInactive_ThrowsBadRequest() {
        sampleMembership.setStatus(CommonStatus.INACTIVE);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(membershipRepository.findById(10L)).thenReturn(Optional.of(sampleMembership));

        MemberSubscriptionDto req = MemberSubscriptionDto.builder().memberId(1L).membershipId(10L).build();
        assertThrows(BadRequestException.class, () -> subscriptionService.createSubscription(req));
    }

    @Test
    @DisplayName("TC-84: Đăng ký mới thành công bắt đầu từ hôm nay và tạo giao dịch thanh toán")
    void createSubscription_Success_NewRegistration() {
        MemberSubscriptionDto req = MemberSubscriptionDto.builder()
                .memberId(1L)
                .membershipId(10L)
                .paymentMethod("CASH")
                .paidPrice(BigDecimal.valueOf(500000))
                .notes("Dang ky moi")
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(membershipRepository.findById(10L)).thenReturn(Optional.of(sampleMembership));
        when(memberSubscriptionRepository.findByMemberIdAndStatusOrderByEndDateDesc(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        when(memberSubscriptionRepository.existsByCode(anyString())).thenReturn(false);
        when(memberSubscriptionRepository.save(any(MemberSubscription.class))).thenReturn(sampleSubscription);
        when(memberSubscriptionMapper.toDto(sampleSubscription)).thenReturn(sampleSubscriptionDto);

        MemberSubscriptionDto result = subscriptionService.createSubscription(req);

        assertNotNull(result);
        assertEquals("SUB0001", result.getCode());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(memberSubscriptionRepository, times(1)).save(any(MemberSubscription.class));
    }

    @Test
    @DisplayName("TC-85: Gia hạn gói tập cộng dồn ngày tiếp nối hạn cũ khi hội viên đang còn gói active")
    void createSubscription_RenewalConsecutive_ExtendsFromPreviousExpiry() {
        LocalDate currentExpiry = LocalDate.now().plusDays(10);
        MemberSubscription activeSub = MemberSubscription.builder()
                .status(SubscriptionStatus.ACTIVE)
                .endDate(currentExpiry)
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(membershipRepository.findById(10L)).thenReturn(Optional.of(sampleMembership));
        when(memberSubscriptionRepository.findByMemberIdAndStatusOrderByEndDateDesc(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(activeSub));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        when(memberSubscriptionRepository.existsByCode(anyString())).thenReturn(false);
        when(memberSubscriptionRepository.save(any(MemberSubscription.class))).thenAnswer(i -> {
            MemberSubscription sub = (MemberSubscription) i.getArguments()[0];
            assertEquals(currentExpiry.plusDays(1), sub.getStartDate());
            assertEquals(currentExpiry.plusDays(1).plusMonths(1), sub.getEndDate());
            return sub;
        });
        when(memberSubscriptionMapper.toDto(any(MemberSubscription.class))).thenReturn(sampleSubscriptionDto);

        MemberSubscriptionDto req = MemberSubscriptionDto.builder()
                .memberId(1L)
                .membershipId(10L)
                .paymentMethod("BANK_TRANSFER")
                .build();

        MemberSubscriptionDto result = subscriptionService.createSubscription(req);

        assertNotNull(result);
        verify(memberSubscriptionRepository, times(1)).save(any(MemberSubscription.class));
    }

    @Test
    @DisplayName("TC-86: Báo lỗi BadRequestException khi phương thức thanh toán không hợp lệ")
    void createSubscription_InvalidPaymentMethod_ThrowsBadRequest() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(membershipRepository.findById(10L)).thenReturn(Optional.of(sampleMembership));
        when(memberSubscriptionRepository.findByMemberIdAndStatusOrderByEndDateDesc(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(new ArrayList<>());

        MemberSubscriptionDto req = MemberSubscriptionDto.builder()
                .memberId(1L)
                .membershipId(10L)
                .paymentMethod("INVALID_PAYMENT")
                .build();

        assertThrows(BadRequestException.class, () -> subscriptionService.createSubscription(req));
    }

    @Test
    @DisplayName("TC-87: Tự động dùng đơn giá niêm yết của gói khi client không truyền paidPrice")
    void createSubscription_DefaultPrice_UsesMembershipPrice() {
        MemberSubscriptionDto req = MemberSubscriptionDto.builder()
                .memberId(1L)
                .membershipId(10L)
                .paidPrice(null)
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(membershipRepository.findById(10L)).thenReturn(Optional.of(sampleMembership));
        when(memberSubscriptionRepository.findByMemberIdAndStatusOrderByEndDateDesc(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        when(memberSubscriptionRepository.existsByCode(anyString())).thenReturn(false);
        when(memberSubscriptionRepository.save(any(MemberSubscription.class))).thenAnswer(i -> {
            MemberSubscription sub = (MemberSubscription) i.getArguments()[0];
            assertEquals(BigDecimal.valueOf(500000), sub.getPaidPrice());
            return sub;
        });
        when(memberSubscriptionMapper.toDto(any())).thenReturn(sampleSubscriptionDto);

        MemberSubscriptionDto result = subscriptionService.createSubscription(req);

        assertNotNull(result);
    }

    @Test
    @DisplayName("TC-88: Tra cứu danh sách gói tập của hội viên theo ID thành công")
    void getSubscriptionsByMember_ById_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(List.of(sampleSubscription));
        when(memberSubscriptionMapper.toDto(sampleSubscription)).thenReturn(sampleSubscriptionDto);

        List<MemberSubscriptionDto> list = subscriptionService.getSubscriptionsByMember("1");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("SUB0001", list.get(0).getCode());
    }

    @Test
    @DisplayName("TC-89: Tra cứu danh sách gói tập của hội viên theo mã Code thành công")
    void getSubscriptionsByMember_ByCode_Success() {
        when(memberRepository.findByCode("M0001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(List.of(sampleSubscription));
        when(memberSubscriptionMapper.toDto(sampleSubscription)).thenReturn(sampleSubscriptionDto);

        List<MemberSubscriptionDto> list = subscriptionService.getSubscriptionsByMember("M0001");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("M0001", list.get(0).getMemberCode());
    }

    @Test
    @DisplayName("TC-90: Báo lỗi BadRequestException khi tìm kiếm lịch sử với ID/mã rỗng hoặc null")
    void getSubscriptionsByMember_BlankInput_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> subscriptionService.getSubscriptionsByMember(""));
        assertThrows(BadRequestException.class, () -> subscriptionService.getSubscriptionsByMember(null));
    }

    @Test
    @DisplayName("TC-91: Lấy chi tiết hợp đồng gói tập theo ID thành công")
    void getSubscriptionById_Success() {
        when(memberSubscriptionRepository.findById(100L)).thenReturn(Optional.of(sampleSubscription));
        when(memberSubscriptionMapper.toDto(sampleSubscription)).thenReturn(sampleSubscriptionDto);

        MemberSubscriptionDto result = subscriptionService.getSubscriptionById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    @DisplayName("TC-92: Báo lỗi ResourceNotFoundException khi lấy gói tập theo ID không tồn tại")
    void getSubscriptionById_NotFound_ThrowsResourceNotFound() {
        when(memberSubscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.getSubscriptionById(999L));
    }
}
