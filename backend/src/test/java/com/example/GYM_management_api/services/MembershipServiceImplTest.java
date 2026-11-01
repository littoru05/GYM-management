package com.example.GYM_management_api.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.example.GYM_management_api.services.impl.MembershipServiceImpl;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ MembershipServiceImpl (Tầng Service - Quản lý Gói tập)")
class MembershipServiceImplTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private MemberSubscriptionRepository memberSubscriptionRepository;

    @Mock
    private TransactionDetailRepository transactionDetailRepository;

    @Mock
    private MembershipMapper membershipMapper;

    @InjectMocks
    private MembershipServiceImpl membershipService;

    private Membership sampleMembership;
    private MembershipDto sampleMembershipDto;

    @BeforeEach
    void setUp() {
        sampleMembership = Membership.builder()
                .code("PKG-GOLD")
                .name("Gói Gold 12 Tháng")
                .durationMonths(12)
                .price(BigDecimal.valueOf(5000000))
                .status(CommonStatus.ACTIVE)
                .build();
        sampleMembership.setId(1L);

        sampleMembershipDto = MembershipDto.builder()
                .id("1")
                .code("PKG-GOLD")
                .name("Gói Gold 12 Tháng")
                .durationMonths(12)
                .price(BigDecimal.valueOf(5000000))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-61: Lấy tất cả gói tập không truyền trạng thái thành công")
    void getAllMemberships_NoFilter_ReturnsAll() {
        when(membershipRepository.findAll()).thenReturn(List.of(sampleMembership));
        when(membershipMapper.toDto(sampleMembership)).thenReturn(sampleMembershipDto);

        List<MembershipDto> list = membershipService.getAllMemberships(null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("PKG-GOLD", list.get(0).getCode());
        verify(membershipRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("TC-62: Lấy danh sách gói tập có lọc trạng thái ACTIVE thành công")
    void getAllMemberships_FilterActive_ReturnsActiveOnly() {
        when(membershipRepository.findByStatus(CommonStatus.ACTIVE)).thenReturn(List.of(sampleMembership));
        when(membershipMapper.toDto(sampleMembership)).thenReturn(sampleMembershipDto);

        List<MembershipDto> list = membershipService.getAllMemberships("ACTIVE");

        assertNotNull(list);
        assertEquals(1, list.size());
        verify(membershipRepository, times(1)).findByStatus(CommonStatus.ACTIVE);
    }

    @Test
    @DisplayName("TC-63: Lấy danh sách gói tập với trạng thái không hợp lệ thì tự fallback về findAll")
    void getAllMemberships_InvalidStatus_ReturnsAll() {
        when(membershipRepository.findAll()).thenReturn(List.of(sampleMembership));
        when(membershipMapper.toDto(sampleMembership)).thenReturn(sampleMembershipDto);

        List<MembershipDto> list = membershipService.getAllMemberships("INVALID_STATUS");

        assertNotNull(list);
        assertEquals(1, list.size());
        verify(membershipRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("TC-64: Lấy chi tiết gói tập theo ID thành công")
    void getMembershipById_Success() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(sampleMembership));
        when(membershipMapper.toDto(sampleMembership)).thenReturn(sampleMembershipDto);

        MembershipDto result = membershipService.getMembershipById(1L);

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Gói Gold 12 Tháng", result.getName());
    }

    @Test
    @DisplayName("TC-65: Báo lỗi ResourceNotFoundException khi lấy gói tập với ID không tồn tại")
    void getMembershipById_NotFound_ThrowsResourceNotFound() {
        when(membershipRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> membershipService.getMembershipById(999L));
    }

    @Test
    @DisplayName("TC-66: Tạo mới gói tập thành công và tự động sinh mã code nếu để trống")
    void createMembership_Success_AutoGeneratesCode() {
        MembershipDto req = MembershipDto.builder()
                .name("Goi Tap Basic")
                .durationMonths(1)
                .price(BigDecimal.valueOf(500000))
                .build();

        Membership entity = Membership.builder()
                .name("Goi Tap Basic")
                .durationMonths(1)
                .price(BigDecimal.valueOf(500000))
                .status(CommonStatus.ACTIVE)
                .build();
        entity.setId(2L);

        MembershipDto resDto = MembershipDto.builder()
                .id("2")
                .name("Goi Tap Basic")
                .durationMonths(1)
                .price(BigDecimal.valueOf(500000))
                .build();

        when(membershipRepository.existsByNameIgnoreCase("Goi Tap Basic")).thenReturn(false);
        when(membershipMapper.toEntity(req)).thenReturn(entity);
        when(membershipRepository.save(entity)).thenReturn(entity);
        when(membershipMapper.toDto(entity)).thenReturn(resDto);

        MembershipDto created = membershipService.createMembership(req);

        assertNotNull(created);
        assertEquals("Goi Tap Basic", created.getName());
        assertNotNull(req.getCode());
        assertTrue(req.getCode().startsWith("PKG-"));
        verify(membershipRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("TC-67: Tạo mới gói tập thành công với mã code tự chỉ định")
    void createMembership_Success_CustomCode() {
        MembershipDto req = MembershipDto.builder()
                .code("PKG-CUSTOM")
                .name("Goi Tap Custom")
                .durationMonths(3)
                .price(BigDecimal.valueOf(1200000))
                .build();

        Membership entity = Membership.builder().code("PKG-CUSTOM").name("Goi Tap Custom").build();
        entity.setId(3L);

        when(membershipRepository.existsByNameIgnoreCase("Goi Tap Custom")).thenReturn(false);
        when(membershipRepository.existsByCode("PKG-CUSTOM")).thenReturn(false);
        when(membershipMapper.toEntity(req)).thenReturn(entity);
        when(membershipRepository.save(entity)).thenReturn(entity);
        when(membershipMapper.toDto(entity)).thenReturn(req);

        MembershipDto created = membershipService.createMembership(req);

        assertNotNull(created);
        assertEquals("PKG-CUSTOM", created.getCode());
    }

    @Test
    @DisplayName("TC-68: Báo lỗi BadRequestException khi tạo gói tập với tên trống")
    void createMembership_BlankName_ThrowsBadRequest() {
        MembershipDto reqEmpty = MembershipDto.builder().name("").durationMonths(1).price(BigDecimal.valueOf(100)).build();
        assertThrows(BadRequestException.class, () -> membershipService.createMembership(reqEmpty));

        MembershipDto reqNull = MembershipDto.builder().name(null).durationMonths(1).price(BigDecimal.valueOf(100)).build();
        assertThrows(BadRequestException.class, () -> membershipService.createMembership(reqNull));
    }

    @Test
    @DisplayName("TC-69: Báo lỗi BadRequestException khi thời hạn gói tập nhỏ hơn hoặc bằng 0")
    void createMembership_InvalidDuration_ThrowsBadRequest() {
        MembershipDto reqZero = MembershipDto.builder().name("Test").durationMonths(0).price(BigDecimal.valueOf(100)).build();
        assertThrows(BadRequestException.class, () -> membershipService.createMembership(reqZero));

        MembershipDto reqNeg = MembershipDto.builder().name("Test").durationMonths(-1).price(BigDecimal.valueOf(100)).build();
        assertThrows(BadRequestException.class, () -> membershipService.createMembership(reqNeg));
    }

    @Test
    @DisplayName("TC-70: Báo lỗi BadRequestException khi đơn giá gói tập là số âm")
    void createMembership_NegativePrice_ThrowsBadRequest() {
        MembershipDto req = MembershipDto.builder().name("Test").durationMonths(1).price(BigDecimal.valueOf(-1000)).build();
        assertThrows(BadRequestException.class, () -> membershipService.createMembership(req));
    }

    @Test
    @DisplayName("TC-71: Báo lỗi DuplicateResourceException khi tên gói tập đã tồn tại trong hệ thống")
    void createMembership_DuplicateName_ThrowsDuplicateResource() {
        MembershipDto req = MembershipDto.builder().name("Gói Gold 12 Tháng").durationMonths(12).price(BigDecimal.valueOf(5000000)).build();
        when(membershipRepository.existsByNameIgnoreCase("Gói Gold 12 Tháng")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> membershipService.createMembership(req));
        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-72: Báo lỗi DuplicateResourceException khi mã gói tập tùy chỉnh đã tồn tại")
    void createMembership_DuplicateCode_ThrowsDuplicateResource() {
        MembershipDto req = MembershipDto.builder().code("PKG-EXIST").name("Goi Tap Moi").durationMonths(1).price(BigDecimal.valueOf(500000)).build();
        when(membershipRepository.existsByNameIgnoreCase("Goi Tap Moi")).thenReturn(false);
        when(membershipRepository.existsByCode("PKG-EXIST")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> membershipService.createMembership(req));
    }

    @Test
    @DisplayName("TC-73: Cập nhật thông tin gói tập thành công")
    void updateMembership_Success() {
        MembershipDto updateDto = MembershipDto.builder()
                .name("Gói Gold 12 Tháng VIP")
                .durationMonths(12)
                .price(BigDecimal.valueOf(5500000))
                .build();

        when(membershipRepository.findById(1L)).thenReturn(Optional.of(sampleMembership));
        when(membershipRepository.existsByNameIgnoreCaseAndIdNot("Gói Gold 12 Tháng VIP", 1L)).thenReturn(false);
        when(membershipRepository.save(sampleMembership)).thenReturn(sampleMembership);
        when(membershipMapper.toDto(sampleMembership)).thenReturn(sampleMembershipDto);

        MembershipDto updated = membershipService.updateMembership(1L, updateDto);

        assertNotNull(updated);
        verify(membershipMapper, times(1)).updateEntityFromDto(updateDto, sampleMembership);
        verify(membershipRepository, times(1)).save(sampleMembership);
    }

    @Test
    @DisplayName("TC-74: Báo lỗi ResourceNotFoundException khi cập nhật gói tập không tồn tại")
    void updateMembership_NotFound_ThrowsResourceNotFound() {
        when(membershipRepository.findById(999L)).thenReturn(Optional.empty());

        MembershipDto req = MembershipDto.builder().name("New Name").build();
        assertThrows(ResourceNotFoundException.class, () -> membershipService.updateMembership(999L, req));
    }

    @Test
    @DisplayName("TC-75: Báo lỗi DuplicateResourceException khi đổi tên gói tập trùng với gói tập khác")
    void updateMembership_DuplicateNameOfOther_ThrowsDuplicate() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(sampleMembership));
        when(membershipRepository.existsByNameIgnoreCaseAndIdNot("Goi Bi Trung", 1L)).thenReturn(true);

        MembershipDto req = MembershipDto.builder().name("Goi Bi Trung").build();
        assertThrows(DuplicateResourceException.class, () -> membershipService.updateMembership(1L, req));
    }

    @Test
    @DisplayName("TC-76: Xóa mềm (chuyển sang INACTIVE) khi gói tập đã có hội viên mua hoặc có giao dịch")
    void deleteMembership_WithUsage_SoftDeletesAsInactive() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(sampleMembership));
        when(memberSubscriptionRepository.existsByMembershipId(1L)).thenReturn(true);

        membershipService.deleteMembership(1L);

        assertEquals(CommonStatus.INACTIVE, sampleMembership.getStatus());
        verify(membershipRepository, times(1)).save(sampleMembership);
        verify(membershipRepository, never()).delete(any());
    }

    @Test
    @DisplayName("TC-77: Xóa cứng vĩnh viễn khi gói tập chưa từng phát sinh dữ liệu sử dụng")
    void deleteMembership_WithoutUsage_HardDeletes() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(sampleMembership));
        when(memberSubscriptionRepository.existsByMembershipId(1L)).thenReturn(false);
        when(transactionDetailRepository.existsByMembershipId(1L)).thenReturn(false);

        membershipService.deleteMembership(1L);

        verify(membershipRepository, times(1)).delete(sampleMembership);
    }

    @Test
    @DisplayName("TC-78: Báo lỗi ResourceNotFoundException khi xóa gói tập không tồn tại")
    void deleteMembership_NotFound_ThrowsResourceNotFound() {
        when(membershipRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> membershipService.deleteMembership(999L));
    }
}
