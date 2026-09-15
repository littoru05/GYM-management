package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.PromotionRepository;
import com.example.GYM_management_api.services.IPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionServiceImpl implements IPromotionService {

    private final PromotionRepository promotionRepository;
}
