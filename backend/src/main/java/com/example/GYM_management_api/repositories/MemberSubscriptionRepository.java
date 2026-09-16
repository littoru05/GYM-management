package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.MemberSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberSubscriptionRepository extends JpaRepository<MemberSubscription, Long> {
}
