package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    List<VoucherUsage> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<VoucherUsage> findByVoucherCodeIgnoreCaseOrderByCreatedAtDesc(String voucherCode);

    long countByVoucherCodeIgnoreCase(String voucherCode);
}
