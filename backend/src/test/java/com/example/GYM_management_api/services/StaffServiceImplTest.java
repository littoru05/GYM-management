package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.LoginDto;
import com.example.GYM_management_api.dtos.StaffDto;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.StaffMapper;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.security.JwtUtil;
import com.example.GYM_management_api.services.impl.AuthServiceImpl;
import com.example.GYM_management_api.services.impl.StaffServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private StaffMapper staffMapper = new StaffMapper();

    @InjectMocks
    private StaffServiceImpl staffService;

    private Staff sampleStaff;
    private StaffDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleStaff = Staff.builder()
                .code("STF-100001")
                .name("Nguyễn Văn Nhân Viên")
                .email("staff01@gym.com")
                .phone("0901112223")
                .password("$2a$10$encryptedPasswordSample")
                .role(StaffRole.STAFF)
                .status(StaffStatus.ACTIVE)
                .lastLogin(LocalDateTime.now())
                .build();
        sampleStaff.setId(10L);

        sampleDto = StaffDto.builder()
                .name("Nguyễn Văn Nhân Viên")
                .email("staff01@gym.com")
                .phone("0901112223")
                .password("password123")
                .role("STAFF")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Tạo nhân viên thành công - Mật khẩu được mã hóa BCrypt")
    void createStaff_Success() {
        when(staffRepository.existsByEmailIgnoreCase("staff01@gym.com")).thenReturn(false);
        when(staffRepository.existsByPhone("0901112223")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedBCryptSecret");
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        StaffDto result = staffService.createStaff(sampleDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("10");
        assertThat(result.getEmail()).isEqualTo("staff01@gym.com");
        assertThat(result.getPassword()).isNull(); // Đảm bảo DTO không trả mật khẩu
        verify(passwordEncoder).encode("password123");
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    @DisplayName("Tạo nhân viên thất bại - Báo lỗi 409 khi trùng Email")
    void createStaff_DuplicateEmail_ThrowsException() {
        when(staffRepository.existsByEmailIgnoreCase("staff01@gym.com")).thenReturn(true);

        assertThatThrownBy(() -> staffService.createStaff(sampleDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email này đã được sử dụng");

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Tạo nhân viên thất bại - Báo lỗi 409 khi trùng SĐT")
    void createStaff_DuplicatePhone_ThrowsException() {
        when(staffRepository.existsByEmailIgnoreCase("staff01@gym.com")).thenReturn(false);
        when(staffRepository.existsByPhone("0901112223")).thenReturn(true);

        assertThatThrownBy(() -> staffService.createStaff(sampleDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Số điện thoại này đã được sử dụng");

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Lấy chi tiết nhân viên thành công")
    void getStaffById_Success() {
        when(staffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));

        StaffDto result = staffService.getStaffById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("10");
        assertThat(result.getName()).isEqualTo(sampleStaff.getName());
    }

    @Test
    @DisplayName("Lấy chi tiết nhân viên thất bại khi không tồn tại ID")
    void getStaffById_NotFound() {
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> staffService.getStaffById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy nhân viên với ID: 999");
    }

    @Test
    @DisplayName("Cập nhật thông tin nhân viên thành công")
    void updateStaff_Success() {
        when(staffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(staffRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(10L))).thenReturn(false);
        when(staffRepository.existsByPhoneAndIdNot(anyString(), eq(10L))).thenReturn(false);
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$newHashedPassword");
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffDto updateDto = StaffDto.builder()
                .name("Tên Mới")
                .email("staff01_updated@gym.com")
                .phone("0909999888")
                .password("newpass123")
                .role("STAFF")
                .status("ACTIVE")
                .build();

        StaffDto result = staffService.updateStaff(10L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Tên Mới");
        assertThat(result.getEmail()).isEqualTo("staff01_updated@gym.com");
        verify(passwordEncoder).encode("newpass123");
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    @DisplayName("Cập nhật thất bại - Chặn tự khóa tài khoản Admin đang đăng nhập qua PUT")
    void updateStaff_SelfLock_ThrowsBadRequestException() {
        Staff adminStaff = Staff.builder()
                .code("ADM-001")
                .name("Quản Trị Viên")
                .email("admin@gym.com")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        adminStaff.setId(1L);

        when(staffRepository.findById(1L)).thenReturn(Optional.of(adminStaff));

        StaffDto updateDto = StaffDto.builder()
                .name("Quản Trị Viên Mới")
                .email("admin@gym.com")
                .status("LOCKED")
                .build();

        assertThatThrownBy(() -> staffService.updateStaff(1L, updateDto, "admin@gym.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bạn không thể tự khóa hoặc vô hiệu hóa tài khoản quản trị viên đang đăng nhập");

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Cập nhật thất bại - Chặn tự hạ quyền Admin của chính mình qua PUT")
    void updateStaff_SelfDemote_ThrowsBadRequestException() {
        Staff adminStaff = Staff.builder()
                .code("ADM-001")
                .name("Quản Trị Viên")
                .email("admin@gym.com")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        adminStaff.setId(1L);

        when(staffRepository.findById(1L)).thenReturn(Optional.of(adminStaff));

        StaffDto updateDto = StaffDto.builder()
                .name("Quản Trị Viên Mới")
                .email("admin@gym.com")
                .role("STAFF")
                .build();

        assertThatThrownBy(() -> staffService.updateStaff(1L, updateDto, "admin@gym.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bạn không thể tự hạ quyền quản trị viên của chính mình");

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Khóa tài khoản nhân viên khác thành công (ACTIVE -> LOCKED)")
    void toggleStaffStatus_LockSuccess() {
        sampleStaff.setStatus(StaffStatus.ACTIVE);
        when(staffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffDto result = staffService.toggleStaffStatus(10L, "admin@gym.com");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StaffStatus.LOCKED.name());
        verify(staffRepository).save(sampleStaff);
    }

    @Test
    @DisplayName("Mở khóa tài khoản nhân viên (LOCKED -> ACTIVE)")
    void toggleStaffStatus_UnlockSuccess() {
        sampleStaff.setStatus(StaffStatus.LOCKED);
        when(staffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffDto result = staffService.toggleStaffStatus(10L, "admin@gym.com");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StaffStatus.ACTIVE.name());
        verify(staffRepository).save(sampleStaff);
    }

    @Test
    @DisplayName("Chặn tự khóa tài khoản Admin đang đăng nhập bằng Email - Báo lỗi 400 Bad Request")
    void toggleStaffStatus_SelfLock_ThrowsBadRequestException() {
        Staff adminStaff = Staff.builder()
                .code("ADM-001")
                .name("Quản Trị Viên")
                .email("admin@gym.com")
                .password("$2a$10$hashed")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        adminStaff.setId(1L);

        when(staffRepository.findById(1L)).thenReturn(Optional.of(adminStaff));

        assertThatThrownBy(() -> staffService.toggleStaffStatus(1L, "admin@gym.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bạn không thể tự khóa tài khoản quản trị viên đang đăng nhập");

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Chặn tự khóa tài khoản Admin đang đăng nhập bằng Mã Code (ADM-001) - Báo lỗi 400 Bad Request")
    void toggleStaffStatus_SelfLock_ByCode_ThrowsBadRequestException() {
        Staff adminStaff = Staff.builder()
                .code("ADM-001")
                .name("Quản Trị Viên")
                .email("admin@gym.com")
                .password("$2a$10$hashed")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        adminStaff.setId(1L);

        when(staffRepository.findById(1L)).thenReturn(Optional.of(adminStaff));

        assertThatThrownBy(() -> staffService.toggleStaffStatus(1L, "ADM-001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bạn không thể tự khóa tài khoản quản trị viên đang đăng nhập");

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Chặn tự khóa tài khoản Admin đang đăng nhập thông qua đối chiếu ID trong DB")
    void toggleStaffStatus_SelfLock_ByIdMatch_ThrowsBadRequestException() {
        Staff targetStaff = Staff.builder()
                .code("ADM-001")
                .name("Quản Trị Viên")
                .email("admin_primary@gym.com")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        targetStaff.setId(1L);

        // Giả sử đăng nhập bằng email phụ nhưng tra ra cùng bản ghi ID = 1L
        Staff currentAdminFromDb = Staff.builder()
                .code("ADM-001")
                .name("Quản Trị Viên")
                .email("admin_primary@gym.com")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        currentAdminFromDb.setId(1L);

        when(staffRepository.findById(1L)).thenReturn(Optional.of(targetStaff));
        when(staffRepository.findByEmailIgnoreCase("admin_secondary@gym.com")).thenReturn(Optional.of(currentAdminFromDb));

        assertThatThrownBy(() -> staffService.toggleStaffStatus(1L, "admin_secondary@gym.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bạn không thể tự khóa tài khoản quản trị viên đang đăng nhập");

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Chặn tự khóa tài khoản khi tham số email rỗng nhưng SecurityContextHolder chứa thông tin Admin")
    void toggleStaffStatus_SelfLock_ViaSecurityContext_ThrowsBadRequestException() {
        Staff adminStaff = Staff.builder()
                .code("ADM-001")
                .name("Quản Trị Viên")
                .email("admin@gym.com")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        adminStaff.setId(1L);

        when(staffRepository.findById(1L)).thenReturn(Optional.of(adminStaff));

        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("admin@gym.com");
        when(auth.getPrincipal()).thenReturn("admin@gym.com");

        org.springframework.security.core.context.SecurityContext context = mock(org.springframework.security.core.context.SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        try {
            assertThatThrownBy(() -> staffService.toggleStaffStatus(1L, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Bạn không thể tự khóa tài khoản quản trị viên đang đăng nhập");
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("Chặn tự xóa tài khoản Admin đang đăng nhập")
    void deleteStaff_SelfDelete_ThrowsBadRequestException() {
        Staff adminStaff = Staff.builder()
                .code("ADM-001")
                .name("Quản Trị Viên")
                .email("admin@gym.com")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        adminStaff.setId(1L);

        when(staffRepository.findById(1L)).thenReturn(Optional.of(adminStaff));

        assertThatThrownBy(() -> staffService.deleteStaff(1L, "admin@gym.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bạn không thể tự xóa tài khoản quản trị viên đang đăng nhập");

        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    @DisplayName("DoD: Tài khoản có trạng thái LOCKED đăng nhập ở UC04 sẽ bị chặn với LockedException")
    void uc04_Login_LockedAccount_ThrowsLockedException() {
        Staff lockedStaff = Staff.builder()
                .code("STF-LOCKED")
                .name("Nhân viên bị khóa")
                .email("locked@gym.com")
                .password("$2a$10$hashedPassword")
                .role(StaffRole.STAFF)
                .status(StaffStatus.LOCKED)
                .build();

        StaffRepository authStaffRepo = mock(StaffRepository.class);
        PasswordEncoder authPasswordEncoder = mock(PasswordEncoder.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);

        AuthServiceImpl authService = new AuthServiceImpl(authStaffRepo, authPasswordEncoder, jwtUtil);

        when(authStaffRepo.findByEmailIgnoreCase("locked@gym.com")).thenReturn(Optional.of(lockedStaff));

        LoginDto loginDto = new LoginDto("locked@gym.com", "password123");

        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(LockedException.class)
                .hasMessageContaining("Tài khoản của bạn đã bị khóa");
    }
}
