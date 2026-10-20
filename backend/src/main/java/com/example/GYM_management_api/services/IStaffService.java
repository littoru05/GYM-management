package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.StaffDto;
import org.springframework.data.domain.Page;

public interface IStaffService {

    Page<StaffDto> getStaffs(int page, int size, String keyword, String role, String status);

    StaffDto getStaffById(Long id);

    StaffDto createStaff(StaffDto dto);

    StaffDto updateStaff(Long id, StaffDto dto);

    StaffDto updateStaff(Long id, StaffDto dto, String currentAdminEmail);

    StaffDto toggleStaffStatus(Long id, String currentAdminEmail);

    void deleteStaff(Long id, String currentAdminEmail);
}
