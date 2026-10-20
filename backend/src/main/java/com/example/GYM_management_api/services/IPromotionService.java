package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.PromotionDto;
import com.example.GYM_management_api.dtos.VoucherApplyRequestDto;
import com.example.GYM_management_api.dtos.VoucherApplyResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IPromotionService {

    Page<PromotionDto> getPromotions(int page, int size, String keyword, String status);

    List<PromotionDto> getAllActivePromotions();

    PromotionDto getPromotionById(Long id);

    PromotionDto createPromotion(PromotionDto request);

    PromotionDto updatePromotion(Long id, PromotionDto request);

    void deletePromotion(Long id);

    VoucherApplyResponseDto applyVoucher(VoucherApplyRequestDto request);
}
