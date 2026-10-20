package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Promotion;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

    Optional<Promotion> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Promotion> findByStatus(CommonStatus status);

    @Modifying
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount + 1 WHERE p.id = :id AND p.usedCount < p.usageLimit")
    int incrementUsedCountIfAvailable(@Param("id") Long id);
}
