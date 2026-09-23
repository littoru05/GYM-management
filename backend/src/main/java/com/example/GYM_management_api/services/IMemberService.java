package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.MemberDto;
import com.example.GYM_management_api.dtos.SubscriptionRequestDto;
import org.springframework.data.domain.Page;

public interface IMemberService {

    Page<MemberDto> getMembers(int page, int size, String keyword, String status);

    MemberDto getMemberByIdOrCode(String idOrCode);

    MemberDto createMember(MemberDto request);

    MemberDto updateMember(Long id, MemberDto request);

    void deleteMember(Long id);

    MemberDto registerSubscription(Long memberId, SubscriptionRequestDto request);
}
