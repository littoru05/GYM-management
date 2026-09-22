package com.example.GYM_management_api.security;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.utils.TestReportWatcher;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@DisplayName("Kiểm thử nghiệp vụ AppUserDetailsService (Tầng Security)")
class AppUserDetailsServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private AppUserDetailsService userDetailsService;

    private Staff activeStaff;

    @BeforeEach
    void setUp() {
        activeStaff = Staff.builder()
                .code("STF-001")
                .name("Nguyen Van A")
                .email("admin@gym.local")
                .password("encoded_pass")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("TC-16: Spring Security tải thông tin UserDetails thành công bằng Email")
    void loadUserByUsername_Success_WithEmail() {
        when(staffRepository.findByEmailIgnoreCase("admin@gym.local")).thenReturn(Optional.of(activeStaff));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@gym.local");

        assertNotNull(userDetails);
        assertEquals("admin@gym.local", userDetails.getUsername());
        assertEquals("encoded_pass", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("TC-17: Spring Security tải thông tin UserDetails thành công cho tài khoản Nhân viên (STAFF) bằng Email")
    void loadUserByUsername_Success_WithStaffEmail() {
        Staff staffUser = Staff.builder()
                .code("STF-002")
                .name("Tran Thi Staff")
                .email("staff@gym.local")
                .password("encoded_staff_pass")
                .role(StaffRole.STAFF)
                .status(StaffStatus.ACTIVE)
                .build();

        when(staffRepository.findByEmailIgnoreCase("staff@gym.local")).thenReturn(Optional.of(staffUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("staff@gym.local");

        assertNotNull(userDetails);
        assertEquals("staff@gym.local", userDetails.getUsername());
        assertEquals("encoded_staff_pass", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF")));
    }

    @Test
    @DisplayName("TC-18: Báo lỗi UsernameNotFoundException khi không tìm thấy tài khoản người dùng")
    void loadUserByUsername_ThrowsUsernameNotFoundException_WhenNotFound() {
        when(staffRepository.findByEmailIgnoreCase("unknown@gym.local")).thenReturn(Optional.empty());
        when(staffRepository.findByCode("unknown@gym.local")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown@gym.local"));

        assertTrue(exception.getMessage().contains("Không tìm thấy người dùng"));
    }

    @Test
    @DisplayName("TC-19: Kiểm tra các cờ bảo mật khi tài khoản bị khóa (LOCKED)")
    void loadUserByUsername_ChecksStatus_LockedAndInactive() {
        activeStaff.setStatus(StaffStatus.LOCKED);
        when(staffRepository.findByEmailIgnoreCase("locked@gym.local")).thenReturn(Optional.of(activeStaff));

        UserDetails userDetailsLocked = userDetailsService.loadUserByUsername("locked@gym.local");
        assertFalse(userDetailsLocked.isAccountNonLocked(), "Tài khoản LOCKED thì accountNonLocked phải là false");
        assertFalse(userDetailsLocked.isEnabled(), "Tài khoản không phải ACTIVE thì enabled phải là false");
    }
}

