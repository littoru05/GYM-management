package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.VoucherValidateRequestDto;
import com.example.GYM_management_api.dtos.VoucherValidateResponseDto;

public interface IVoucherService {

    VoucherValidateResponseDto validateVoucher(VoucherValidateRequestDto request);

    VoucherValidateResponseDto validateVoucherInternal(String code, Long membershipId, java.math.BigDecimal orderAmount, Long memberId);
}
