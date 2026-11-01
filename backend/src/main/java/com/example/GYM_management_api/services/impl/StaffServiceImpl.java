package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.StaffDto;
import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.StaffMapper;
import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.services.IStaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StaffServiceImpl implements IStaffService {

    private final StaffRepository staffRepository;
    private final StaffMapper staffMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<StaffDto> getStaffs(int page, int size, String keyword, String role, String status) {
        int pageIndex = Math.max(0, page);
        int pageSize = size <= 0 ? 10 : size;

        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        StaffRole roleEnum = null;
        if (role != null && !role.trim().isBlank()) {
            try {
                roleEnum = StaffRole.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Giá trị role không hợp lệ: {}", role);
            }
        }

        StaffStatus statusEnum = null;
        if (status != null && !status.trim().isBlank()) {
            try {
                statusEnum = StaffStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Giá trị status không hợp lệ: {}", status);
            }
        }

        String searchKeyword = (keyword != null && !keyword.trim().isBlank()) ? keyword.trim() : null;

        Page<Staff> staffPage = staffRepository.searchStaffs(searchKeyword, roleEnum, statusEnum, pageable);
        return staffPage.map(staffMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffDto getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));
        return staffMapper.toDto(staff);
    }

    @Override
    public StaffDto createStaff(StaffDto dto) {
        String email = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : "";
        if (email.isBlank()) {
            throw new BadRequestException("Email không được để trống.");
        }

        if (staffRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email này đã được sử dụng trong hệ thống.");
        }

        if (dto.getPhone() != null && !dto.getPhone().trim().isBlank()) {
            String phone = dto.getPhone().trim();
            if (staffRepository.existsByPhone(phone)) {
                throw new DuplicateResourceException("Số điện thoại này đã được sử dụng trong hệ thống.");
            }
        }

        String code = dto.getCode() != null ? dto.getCode().trim().toUpperCase() : null;
        if (code != null && !code.isBlank()) {
            if (staffRepository.existsByCode(code)) {
                throw new DuplicateResourceException("Mã nhân viên đã tồn tại.");
            }
        } else {
            code = "STF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        Staff staff = staffMapper.toEntity(dto);
        staff.setCode(code);

        // Mã hóa mật khẩu bằng BCrypt
        String rawPassword = (dto.getPassword() != null && !dto.getPassword().trim().isBlank())
                ? dto.getPassword().trim()
                : "123456";
        staff.setPassword(passwordEncoder.encode(rawPassword));

        Staff savedStaff = staffRepository.save(staff);
        log.info("Tạo mới nhân viên thành công: ID={}, Email={}, Role={}", savedStaff.getId(), savedStaff.getEmail(), savedStaff.getRole());
        return staffMapper.toDto(savedStaff);
    }

    @Override
    public StaffDto updateStaff(Long id, StaffDto dto) {
        return updateStaff(id, dto, null);
    }

    @Override
    public StaffDto updateStaff(Long id, StaffDto dto, String currentAdminEmail) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));

        // Chặn tự khóa, tự vô hiệu hóa hoặc tự hạ quyền Admin đang đăng nhập
        if (isCurrentAdmin(staff, currentAdminEmail)) {
            if (dto.getStatus() != null && !dto.getStatus().trim().isBlank()) {
                try {
                    StaffStatus newStatus = StaffStatus.valueOf(dto.getStatus().trim().toUpperCase());
                    if (newStatus != StaffStatus.ACTIVE) {
                        throw new BadRequestException("Bạn không thể tự khóa hoặc vô hiệu hóa tài khoản quản trị viên đang đăng nhập.");
                    }
                } catch (IllegalArgumentException ignored) {}
            }

            if (dto.getRole() != null && !dto.getRole().trim().isBlank()) {
                try {
                    StaffRole newRole = StaffRole.valueOf(dto.getRole().trim().toUpperCase());
                    if (newRole != StaffRole.ADMIN) {
                        throw new BadRequestException("Bạn không thể tự hạ quyền quản trị viên của chính mình.");
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (dto.getEmail() != null && !dto.getEmail().trim().isBlank()) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            if (staffRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, id)) {
                throw new DuplicateResourceException("Email này đã được sử dụng bởi một tài khoản khác.");
            }
        }

        if (dto.getPhone() != null && !dto.getPhone().trim().isBlank()) {
            String newPhone = dto.getPhone().trim();
            if (staffRepository.existsByPhoneAndIdNot(newPhone, id)) {
                throw new DuplicateResourceException("Số điện thoại này đã được sử dụng bởi một tài khoản khác.");
            }
        }

        if (dto.getCode() != null && !dto.getCode().trim().isBlank()) {
            String newCode = dto.getCode().trim().toUpperCase();
            if (!newCode.equalsIgnoreCase(staff.getCode()) && staffRepository.existsByCode(newCode)) {
                throw new DuplicateResourceException("Mã nhân viên đã tồn tại.");
            }
        }

        staffMapper.updateEntityFromDto(dto, staff);

        // Đổi mật khẩu nếu client truyền mật khẩu mới
        if (dto.getPassword() != null && !dto.getPassword().trim().isBlank()) {
            staff.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
            log.info("Cập nhật mật khẩu mới cho nhân viên ID={}", id);
        }

        Staff updatedStaff = staffRepository.save(staff);
        log.info("Cập nhật thông tin nhân viên thành công: ID={}", id);
        return staffMapper.toDto(updatedStaff);
    }

    @Override
    public StaffDto toggleStaffStatus(Long id, String currentAdminEmail) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));

        // Chặn tự khóa tài khoản Admin đang đăng nhập
        if (isCurrentAdmin(staff, currentAdminEmail)) {
            throw new BadRequestException("Bạn không thể tự khóa tài khoản quản trị viên đang đăng nhập.");
        }

        if (staff.getStatus() == StaffStatus.ACTIVE) {
            staff.setStatus(StaffStatus.LOCKED);
            log.info("Đã khóa tài khoản nhân viên: ID={}, Email={}", staff.getId(), staff.getEmail());
        } else {
            staff.setStatus(StaffStatus.ACTIVE);
            log.info("Đã mở khóa tài khoản nhân viên: ID={}, Email={}", staff.getId(), staff.getEmail());
        }

        Staff updated = staffRepository.save(staff);
        return staffMapper.toDto(updated);
    }

    @Override
    public void deleteStaff(Long id, String currentAdminEmail) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));

        // Chặn tự xóa tài khoản Admin đang đăng nhập
        if (isCurrentAdmin(staff, currentAdminEmail)) {
            throw new BadRequestException("Bạn không thể tự xóa tài khoản quản trị viên đang đăng nhập.");
        }

        // Xóa mềm bằng cách chuyển trạng thái sang INACTIVE
        staff.setStatus(StaffStatus.INACTIVE);
        staffRepository.save(staff);
        log.info("Đã chuyển trạng thái nhân viên ID={} sang INACTIVE", id);
    }

    private boolean isCurrentAdmin(Staff targetStaff, String currentAdminEmail) {
        if (currentAdminEmail == null || currentAdminEmail.isBlank()) {
            org.springframework.security.core.Authentication auth = 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                currentAdminEmail = auth.getName();
            }
        }

        if (currentAdminEmail != null && !currentAdminEmail.isBlank()) {
            String identifier = currentAdminEmail.trim();

            // 1. So khớp trực tiếp với Email hoặc Code của nhân viên mục tiêu
            if (identifier.equalsIgnoreCase(targetStaff.getEmail()) || identifier.equalsIgnoreCase(targetStaff.getCode())) {
                return true;
            }

            // 2. Tra cứu tài khoản của admin đang đăng nhập trong DB để so sánh theo ID
            Staff currentAdmin = staffRepository.findByEmailIgnoreCase(identifier)
                    .or(() -> staffRepository.findByCode(identifier))
                    .orElse(null);

            if (currentAdmin != null && (currentAdmin.getId().equals(targetStaff.getId())
                    || (targetStaff.getEmail() != null && targetStaff.getEmail().equalsIgnoreCase(currentAdmin.getEmail())))) {
                return true;
            }
        }

        return false;
    }
}
