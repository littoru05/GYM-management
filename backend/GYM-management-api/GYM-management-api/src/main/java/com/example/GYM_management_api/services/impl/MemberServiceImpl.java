package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.MemberRepository;
import com.example.GYM_management_api.services.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements IMemberService {

    private final MemberRepository memberRepository;
}
