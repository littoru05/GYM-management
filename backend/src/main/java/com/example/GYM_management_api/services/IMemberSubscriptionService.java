package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.MemberSubscriptionDto;

import java.util.List;

public interface IMemberSubscriptionService {

    MemberSubscriptionDto createSubscription(MemberSubscriptionDto request);

    List<MemberSubscriptionDto> getSubscriptionsByMember(String memberIdOrCode);

    List<MemberSubscriptionDto> getAllSubscriptions();

    MemberSubscriptionDto getSubscriptionById(Long id);
}
