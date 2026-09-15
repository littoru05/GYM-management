package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.MembershipRepository;
import com.example.GYM_management_api.services.IMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipServiceImpl implements IMembershipService {

    private final MembershipRepository membershipRepository;
}
