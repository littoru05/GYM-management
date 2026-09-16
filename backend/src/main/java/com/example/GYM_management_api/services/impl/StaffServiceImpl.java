package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.StaffRepository;
import com.example.GYM_management_api.services.IStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements IStaffService {

    private final StaffRepository staffRepository;
}
