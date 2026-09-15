package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.CheckInRepository;
import com.example.GYM_management_api.services.ICheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckInServiceImpl implements ICheckInService {

    private final CheckInRepository checkInRepository;
}
