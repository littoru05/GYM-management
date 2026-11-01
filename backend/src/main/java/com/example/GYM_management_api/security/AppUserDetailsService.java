package com.example.GYM_management_api.security;

import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import com.example.GYM_management_api.repositories.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Staff staff = staffRepository.findByEmailIgnoreCase(usernameOrEmail)
                .or(() -> staffRepository.findByCode(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email hoặc mã: " + usernameOrEmail));

        boolean enabled = staff.getStatus() == StaffStatus.ACTIVE;
        boolean accountNonLocked = staff.getStatus() != StaffStatus.LOCKED;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + staff.getRole().name())
        );

        return new User(
                staff.getEmail(),
                staff.getPassword(),
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities
        );
    }
}
