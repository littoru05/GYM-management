package com.example.GYM_management_api.services;

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
import com.example.GYM_management_api.services.impl.MemberServiceImpl;
import com.example.GYM_management_api.utils.TestReportWatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ MemberServiceImpl (Tầng Service - Quản lý Hội viên)")
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberSubscriptionRepository memberSubscriptionRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member sampleMember;
    private MemberDto sampleMemberDto;

    @BeforeEach
    void setUp() {
        sampleMember = Member.builder()
                .code("M0001")
                .name("Nguyen Van A")
                .phone("0900000001")
                .email("vana@gym.local")
                .status(MemberStatus.ACTIVE)
                .joinDate(LocalDate.now())
                .subscriptions(new ArrayList<>())
                .build();
        sampleMember.setId(1L);

        sampleMemberDto = MemberDto.builder()
                .id("1")
                .code("M0001")
                .name("Nguyen Van A")
                .phone("0900000001")
                .email("vana@gym.local")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-40: Lấy danh sách hội viên mặc định (không lọc trạng thái) thành công")
    void getMembers_Default_ReturnsPagedMembers() {
        Page<Member> page = new PageImpl<>(List.of(sampleMember));
        when(memberRepository.searchMembers(isNull(), any(Pageable.class))).thenReturn(page);
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        Page<MemberDto> result = memberService.getMembers(0, 10, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("M0001", result.getContent().get(0).getCode());
        verify(memberRepository, times(1)).searchMembers(isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-41: Lấy danh sách hội viên lọc theo trạng thái ACTIVE")
    void getMembers_FilterActive_ReturnsActiveMembers() {
        Page<Member> page = new PageImpl<>(List.of(sampleMember));
        when(memberRepository.searchActiveMembers(isNull(), any(LocalDate.class), any(Pageable.class))).thenReturn(page);
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        Page<MemberDto> result = memberService.getMembers(0, 10, null, "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(memberRepository, times(1)).searchActiveMembers(isNull(), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-42: Lấy danh sách hội viên lọc theo trạng thái LOCKED")
    void getMembers_FilterLocked_ReturnsLockedMembers() {
        sampleMember.setStatus(MemberStatus.LOCKED);
        Page<Member> page = new PageImpl<>(List.of(sampleMember));
        when(memberRepository.searchLockedMembers(isNull(), any(Pageable.class))).thenReturn(page);
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        Page<MemberDto> result = memberService.getMembers(0, 10, null, "LOCKED");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(memberRepository, times(1)).searchLockedMembers(isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-43: Lấy danh sách hội viên lọc theo trạng thái EXPIRED")
    void getMembers_FilterExpired_ReturnsExpiredMembers() {
        Page<Member> page = new PageImpl<>(List.of(sampleMember));
        when(memberRepository.searchExpiredMembers(isNull(), any(LocalDate.class), any(Pageable.class))).thenReturn(page);
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        Page<MemberDto> result = memberService.getMembers(0, 10, null, "EXPIRED");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(memberRepository, times(1)).searchExpiredMembers(isNull(), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-44: Lấy danh sách hội viên lọc theo trạng thái EXPIRING_SOON")
    void getMembers_FilterExpiringSoon_ReturnsExpiringSoonMembers() {
        Page<Member> page = new PageImpl<>(List.of(sampleMember));
        when(memberRepository.searchExpiringSoonMembers(isNull(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class))).thenReturn(page);
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        Page<MemberDto> result = memberService.getMembers(0, 10, null, "EXPIRING_SOON");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(memberRepository, times(1)).searchExpiringSoonMembers(isNull(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-45: Tra cứu thông tin hội viên theo ID số thành công")
    void getMemberByIdOrCode_ById_ReturnsMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(Collections.emptyList());
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        MemberDto result = memberService.getMemberByIdOrCode("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("M0001", result.getCode());
        verify(memberRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TC-46: Tra cứu thông tin hội viên theo mã Code thành công")
    void getMemberByIdOrCode_ByCode_ReturnsMember() {
        when(memberRepository.findByCode("M0001")).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(Collections.emptyList());
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        MemberDto result = memberService.getMemberByIdOrCode("M0001");

        assertNotNull(result);
        assertEquals("M0001", result.getCode());
        verify(memberRepository, times(1)).findByCode("M0001");
    }

    @Test
    @DisplayName("TC-47: Báo lỗi BadRequestException khi tìm kiếm với mã/ID trống hoặc null")
    void getMemberByIdOrCode_BlankInput_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> memberService.getMemberByIdOrCode(""));
        assertThrows(BadRequestException.class, () -> memberService.getMemberByIdOrCode("   "));
        assertThrows(BadRequestException.class, () -> memberService.getMemberByIdOrCode(null));
    }

    @Test
    @DisplayName("TC-48: Báo lỗi ResourceNotFoundException khi không tìm thấy hội viên")
    void getMemberByIdOrCode_NotFound_ThrowsResourceNotFound() {
        when(memberRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.getMemberByIdOrCode("UNKNOWN"));
    }

    @Test
    @DisplayName("TC-49: Thêm mới hội viên thành công và chuẩn hóa số điện thoại quốc tế (+84)")
    void createMember_Success_NormalizesPhone() {
        MemberDto request = MemberDto.builder()
                .name("Tran Thi B")
                .phone("+84912345678")
                .email("thib@gym.local")
                .build();

        Member entityToSave = Member.builder()
                .code("M0001")
                .name("Tran Thi B")
                .phone("0912345678")
                .email("thib@gym.local")
                .status(MemberStatus.ACTIVE)
                .build();
        entityToSave.setId(2L);

        MemberDto expectedDto = MemberDto.builder()
                .id("2")
                .code("M0001")
                .name("Tran Thi B")
                .phone("0912345678")
                .build();

        when(memberRepository.existsByPhone("0912345678")).thenReturn(false);
        when(memberRepository.existsByCode(anyString())).thenReturn(false);
        when(memberMapper.toEntity(eq(request), anyString())).thenReturn(entityToSave);
        when(memberRepository.save(any(Member.class))).thenReturn(entityToSave);
        when(memberMapper.toDto(entityToSave)).thenReturn(expectedDto);

        MemberDto result = memberService.createMember(request);

        assertNotNull(result);
        assertEquals("2", result.getId());
        assertEquals("0912345678", result.getPhone());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("TC-50: Báo lỗi BadRequestException khi thêm hội viên với tên trống")
    void createMember_BlankName_ThrowsBadRequest() {
        MemberDto request = MemberDto.builder().name("").phone("0912345678").build();
        assertThrows(BadRequestException.class, () -> memberService.createMember(request));

        MemberDto requestNull = MemberDto.builder().name(null).phone("0912345678").build();
        assertThrows(BadRequestException.class, () -> memberService.createMember(requestNull));
    }

    @Test
    @DisplayName("TC-51: Báo lỗi BadRequestException khi số điện thoại sai định dạng (không đủ 10 số hoặc chứa chữ)")
    void createMember_InvalidPhone_ThrowsBadRequest() {
        MemberDto requestWrongLength = MemberDto.builder().name("Test").phone("0912345").build();
        assertThrows(BadRequestException.class, () -> memberService.createMember(requestWrongLength));

        MemberDto requestHasLetters = MemberDto.builder().name("Test").phone("090abc1234").build();
        assertThrows(BadRequestException.class, () -> memberService.createMember(requestHasLetters));
    }

    @Test
    @DisplayName("TC-52: Báo lỗi DuplicateResourceException khi số điện thoại đã tồn tại")
    void createMember_DuplicatePhone_ThrowsDuplicateResource() {
        MemberDto request = MemberDto.builder().name("Test").phone("0900000001").build();
        when(memberRepository.existsByPhone("0900000001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> memberService.createMember(request));
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-53: Cập nhật thông tin hội viên thành công")
    void updateMember_Success() {
        MemberDto updateReq = MemberDto.builder()
                .name("Nguyen Van A Updated")
                .phone("0900000002")
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.existsByPhoneAndIdNot("0900000002", 1L)).thenReturn(false);
        when(memberRepository.save(sampleMember)).thenReturn(sampleMember);
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(Collections.emptyList());
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        MemberDto result = memberService.updateMember(1L, updateReq);

        assertNotNull(result);
        verify(memberMapper, times(1)).updateEntityFromDto(updateReq, sampleMember);
        verify(memberRepository, times(1)).save(sampleMember);
    }

    @Test
    @DisplayName("TC-54: Báo lỗi ResourceNotFoundException khi cập nhật hội viên không tồn tại")
    void updateMember_NotFound_ThrowsResourceNotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        MemberDto updateReq = MemberDto.builder().name("Test").build();
        assertThrows(ResourceNotFoundException.class, () -> memberService.updateMember(999L, updateReq));
    }

    @Test
    @DisplayName("TC-55: Báo lỗi DuplicateResourceException khi cập nhật SĐT bị trùng với hội viên khác")
    void updateMember_DuplicatePhoneOfOther_ThrowsDuplicate() {
        MemberDto updateReq = MemberDto.builder().phone("0988888888").build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberRepository.existsByPhoneAndIdNot("0988888888", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> memberService.updateMember(1L, updateReq));
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-56: Xóa mềm (chuyển sang LOCKED) khi hội viên đã có lịch sử gói tập")
    void deleteMember_WithSubscriptions_SoftDeletesAsLocked() {
        MemberSubscription sub = MemberSubscription.builder().build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(sub));

        memberService.deleteMember(1L);

        assertEquals(MemberStatus.LOCKED, sampleMember.getStatus());
        verify(memberRepository, times(1)).save(sampleMember);
        verify(memberRepository, never()).delete(any());
    }

    @Test
    @DisplayName("TC-57: Xóa cứng vĩnh viễn khi hội viên chưa từng đăng ký gói tập nào")
    void deleteMember_WithoutSubscriptions_HardDeletes() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(memberSubscriptionRepository.findByMemberIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        memberService.deleteMember(1L);

        verify(memberRepository, times(1)).delete(sampleMember);
    }

    @Test
    @DisplayName("TC-58: Báo lỗi ResourceNotFoundException khi xóa hội viên không tồn tại")
    void deleteMember_NotFound_ThrowsResourceNotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> memberService.deleteMember(999L));
    }

    @Test
    @DisplayName("TC-59: Đăng ký gói tập thành công và tự động mở khóa hội viên bị LOCKED sang ACTIVE")
    void registerSubscription_Success_UnlocksMember() {
        sampleMember.setStatus(MemberStatus.LOCKED);

        Membership membership = Membership.builder()
                .name("VIP 1 Thang")
                .durationMonths(1)
                .price(BigDecimal.valueOf(500000))
                .build();
        membership.setId(10L);

        SubscriptionRequestDto request = new SubscriptionRequestDto(10L, LocalDate.now(), "Dang ky tai quay");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(membershipRepository.findById(10L)).thenReturn(Optional.of(membership));
        when(memberSubscriptionRepository.count()).thenReturn(0L);
        when(memberSubscriptionRepository.existsByCode(anyString())).thenReturn(false);
        when(memberSubscriptionRepository.findByMemberIdOrderByStartDateDesc(1L)).thenReturn(Collections.emptyList());
        when(memberMapper.toDto(sampleMember)).thenReturn(sampleMemberDto);

        MemberDto result = memberService.registerSubscription(1L, request);

        assertNotNull(result);
        assertEquals(MemberStatus.ACTIVE, sampleMember.getStatus());
        verify(memberSubscriptionRepository, times(1)).save(any(MemberSubscription.class));
        verify(memberRepository, atLeastOnce()).save(sampleMember);
    }

    @Test
    @DisplayName("TC-60: Báo lỗi BadRequestException khi đăng ký gói tập mà không chọn ngày bắt đầu")
    void registerSubscription_NullStartDate_ThrowsBadRequest() {
        Membership membership = Membership.builder().name("Goi Tap").durationMonths(1).build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(membershipRepository.findById(10L)).thenReturn(Optional.of(membership));

        SubscriptionRequestDto request = new SubscriptionRequestDto(10L, null, null);

        assertThrows(BadRequestException.class, () -> memberService.registerSubscription(1L, request));
    }
}
