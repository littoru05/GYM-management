package com.example.GYM_management_api.services;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.GYM_management_api.dtos.AuthResponseDto;
import com.example.GYM_management_api.dtos.LoginDto;
import com.example.GYM_management_api.dtos.RegisterDto;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.security.JwtUtil;
import com.example.GYM_management_api.services.impl.AuthServiceImpl;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ AuthServiceImpl (Tầng Service)")
class AuthServiceImplTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private Staff sampleStaff;

    @BeforeEach
    void setUp() {
        sampleStaff = Staff.builder()
                .code("STF-001")
                .name("Nguyen Van A")
                .email("admin@gym.local")
                .phone("0900000001")
                .password("hashed_password")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
        sampleStaff.setId(1L);
    }

    @Test
    @DisplayName("TC-01: Đăng nhập thành công với Email và mật khẩu chính xác")
    void login_Success_WithEmail() {
        LoginDto request = new LoginDto("admin@gym.local", "123456");

        when(staffRepository.findByEmailIgnoreCase("admin@gym.local")).thenReturn(Optional.of(sampleStaff));
        when(passwordEncoder.matches("123456", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "admin@gym.local", "ADMIN")).thenReturn("mocked.jwt.token");

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("admin@gym.local", response.getEmail());
        assertEquals("STF-001", response.getCode());
        assertEquals(StaffRole.ADMIN, response.getRole());
        assertEquals(StaffStatus.ACTIVE, response.getStatus());

        verify(staffRepository, times(1)).save(sampleStaff);
        assertNotNull(sampleStaff.getLastLogin());
    }

    @Test
    @DisplayName("TC-02: Đăng nhập thành công với tài khoản Nhân viên (STAFF) bằng Email")
    void login_Success_WithStaffEmail() {
        Staff staffUser = Staff.builder()
                .code("STF-002")
                .name("Tran Thi Staff")
                .email("staff@gym.local")
                .phone("0900000002")
                .password("hashed_staff_password")
                .role(StaffRole.STAFF)
                .status(StaffStatus.ACTIVE)
                .build();
        staffUser.setId(2L);

        LoginDto request = new LoginDto("staff@gym.local", "123456");

        when(staffRepository.findByEmailIgnoreCase("staff@gym.local")).thenReturn(Optional.of(staffUser));
        when(passwordEncoder.matches("123456", "hashed_staff_password")).thenReturn(true);
        when(jwtUtil.generateToken(2L, "staff@gym.local", "STAFF")).thenReturn("mocked.staff.jwt.token");

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.staff.jwt.token", response.getToken());
        assertEquals("staff@gym.local", response.getEmail());
        assertEquals("STF-002", response.getCode());
        assertEquals(StaffRole.STAFF, response.getRole());
        assertEquals(StaffStatus.ACTIVE, response.getStatus());
        verify(staffRepository).save(staffUser);
    }

    @Test
    @DisplayName("TC-03: Đăng nhập thất bại khi tài khoản không tồn tại trong hệ thống")
    void login_ThrowsBadCredentials_WhenUserNotFound() {
        LoginDto request = new LoginDto("notfound@gym.local", "123456");

        when(staffRepository.findByEmailIgnoreCase("notfound@gym.local")).thenReturn(Optional.empty());
        when(staffRepository.findByCode("notfound@gym.local")).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.login(request));
        assertEquals("Email hoặc mật khẩu không chính xác.", exception.getMessage());
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-04: Đăng nhập thất bại khi tài khoản đã bị khóa (LOCKED)")
    void login_ThrowsLockedException_WhenAccountLocked() {
        sampleStaff.setStatus(StaffStatus.LOCKED);
        LoginDto request = new LoginDto("admin@gym.local", "123456");

        when(staffRepository.findByEmailIgnoreCase("admin@gym.local")).thenReturn(Optional.of(sampleStaff));

        LockedException exception = assertThrows(LockedException.class, () -> authService.login(request));
        assertTrue(exception.getMessage().contains("Tài khoản của bạn đã bị khóa"));
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-05: Đăng nhập thất bại khi tài khoản chưa kích hoạt hoặc đã ngưng hoạt động (INACTIVE)")
    void login_ThrowsDisabledException_WhenAccountNotActive() {
        sampleStaff.setStatus(StaffStatus.INACTIVE);
        LoginDto request = new LoginDto("admin@gym.local", "123456");

        when(staffRepository.findByEmailIgnoreCase("admin@gym.local")).thenReturn(Optional.of(sampleStaff));

        DisabledException exception = assertThrows(DisabledException.class, () -> authService.login(request));
        assertTrue(exception.getMessage().contains("Tài khoản chưa được kích hoạt"));
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-06: Đăng nhập thất bại khi nhập sai mật khẩu")
    void login_ThrowsBadCredentials_WhenPasswordMismatch() {
        LoginDto request = new LoginDto("admin@gym.local", "wrongpassword");

        when(staffRepository.findByEmailIgnoreCase("admin@gym.local")).thenReturn(Optional.of(sampleStaff));
        when(passwordEncoder.matches("wrongpassword", "hashed_password")).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.login(request));
        assertEquals("Email hoặc mật khẩu không chính xác.", exception.getMessage());
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-07: Đăng ký thành công nhân viên mới và tự sinh mã tiền tố STF-")
    void register_Success_AutoGenerateCode() {
        RegisterDto request = RegisterDto.builder()
                .fullName("Tran Van B")
                .email("tranvanb@gym.local")
                .phone("0912345678")
                .password("secret123")
                .role(StaffRole.STAFF)
                .build();

        when(staffRepository.existsByEmailIgnoreCase("tranvanb@gym.local")).thenReturn(false);
        when(staffRepository.existsByPhone("0912345678")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret123");

        Map<String, Object> result = authService.register(request);

        assertNotNull(result);
        assertEquals("Đăng ký tài khoản thành công!", result.get("message"));
        assertEquals("tranvanb@gym.local", result.get("email"));

        ArgumentCaptor<Staff> staffCaptor = ArgumentCaptor.forClass(Staff.class);
        verify(staffRepository).save(staffCaptor.capture());

        Staff savedStaff = staffCaptor.getValue();
        assertTrue(savedStaff.getCode().startsWith("STF-"));
        assertEquals("Tran Van B", savedStaff.getName());
        assertEquals("tranvanb@gym.local", savedStaff.getEmail());
        assertEquals("0912345678", savedStaff.getPhone());
        assertEquals("encoded_secret123", savedStaff.getPassword());
        assertEquals(StaffRole.STAFF, savedStaff.getRole());
        assertEquals(StaffStatus.ACTIVE, savedStaff.getStatus());
    }

    @Test
    @DisplayName("TC-08: Đăng ký thành công nhân viên mới với mã tự chỉ định (STF-999)")
    void register_Success_CustomCode() {
        RegisterDto request = RegisterDto.builder()
                .fullName("Le Thi C")
                .email("lethic@gym.local")
                .code("STF-999")
                .password("password123")
                .role(StaffRole.ADMIN)
                .build();

        when(staffRepository.existsByEmailIgnoreCase("lethic@gym.local")).thenReturn(false);
        when(staffRepository.existsByCode("STF-999")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pwd");

        Map<String, Object> result = authService.register(request);

        assertNotNull(result);
        assertEquals("STF-999", result.get("code"));

        ArgumentCaptor<Staff> captor = ArgumentCaptor.forClass(Staff.class);
        verify(staffRepository).save(captor.capture());
        assertEquals("STF-999", captor.getValue().getCode());
        assertEquals(StaffRole.ADMIN, captor.getValue().getRole());
    }

    @Test
    @DisplayName("TC-09: Báo lỗi khi đăng ký bằng Email đã tồn tại")
    void register_ThrowsIllegalArgument_WhenEmailExists() {
        RegisterDto request = RegisterDto.builder()
                .fullName("Duplicate Email")
                .email("admin@gym.local")
                .password("123456")
                .build();

        when(staffRepository.existsByEmailIgnoreCase("admin@gym.local")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Email này đã được sử dụng.", exception.getMessage());
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-10: Báo lỗi khi đăng ký bằng Số điện thoại đã tồn tại")
    void register_ThrowsIllegalArgument_WhenPhoneExists() {
        RegisterDto request = RegisterDto.builder()
                .fullName("Duplicate Phone")
                .email("unique@gym.local")
                .phone("0900000001")
                .password("123456")
                .build();

        when(staffRepository.existsByEmailIgnoreCase("unique@gym.local")).thenReturn(false);
        when(staffRepository.existsByPhone("0900000001")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Số điện thoại này đã được sử dụng.", exception.getMessage());
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-11: Báo lỗi khi đăng ký bằng Mã nhân viên tự nhập đã tồn tại")
    void register_ThrowsIllegalArgument_WhenCodeExists() {
        RegisterDto request = RegisterDto.builder()
                .fullName("Duplicate Code")
                .email("unique@gym.local")
                .code("STF-EXIST")
                .password("123456")
                .build();

        when(staffRepository.existsByEmailIgnoreCase("unique@gym.local")).thenReturn(false);
        when(staffRepository.existsByCode("STF-EXIST")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Mã nhân viên đã tồn tại.", exception.getMessage());
        verify(staffRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-12: Tự động gán quyền mặc định STAFF khi role là null")
    void register_DefaultRoleStaff_WhenRoleIsNull() {
        RegisterDto request = RegisterDto.builder()
                .fullName("No Role User")
                .email("norole@gym.local")
                .password("123456")
                .role(null)
                .build();

        when(staffRepository.existsByEmailIgnoreCase("norole@gym.local")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");

        authService.register(request);

        ArgumentCaptor<Staff> captor = ArgumentCaptor.forClass(Staff.class);
        verify(staffRepository).save(captor.capture());
        assertEquals(StaffRole.STAFF, captor.getValue().getRole());
    }

    @Test
    @DisplayName("TC-13: Lấy thông tin hồ sơ của tài khoản đang đăng nhập thành công")
    void getCurrentUser_Success() {
        when(staffRepository.findByEmailIgnoreCase("admin@gym.local")).thenReturn(Optional.of(sampleStaff));

        AuthResponseDto response = authService.getCurrentUser("admin@gym.local");

        assertNotNull(response);
        assertEquals("STF-001", response.getCode());
        assertEquals("admin@gym.local", response.getEmail());
        assertEquals("Nguyen Van A", response.getName());
        assertEquals(StaffRole.ADMIN, response.getRole());
    }

    @Test
    @DisplayName("TC-14: Báo lỗi khi lấy thông tin tài khoản không tồn tại")
    void getCurrentUser_ThrowsIllegalArgument_WhenNotFound() {
        when(staffRepository.findByEmailIgnoreCase("unknown@gym.local")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.getCurrentUser("unknown@gym.local"));
        assertTrue(exception.getMessage().contains("Không tìm thấy thông tin tài khoản"));
    }

    @Test
    @DisplayName("TC-15: Đăng xuất thành công và xóa sạch SecurityContextHolder")
    void logout_ClearsSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(mock(org.springframework.security.core.Authentication.class));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        authService.logout();

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
