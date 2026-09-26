package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
    boolean existsByMembershipId(Long membershipId);
}
