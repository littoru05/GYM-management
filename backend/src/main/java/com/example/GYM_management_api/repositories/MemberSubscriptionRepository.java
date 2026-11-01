package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.MemberSubscription;
import com.example.GYM_management_api.entities.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberSubscriptionRepository extends JpaRepository<MemberSubscription, Long> {

    List<MemberSubscription> findAllByOrderByCreatedAtDesc();

    List<MemberSubscription> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<MemberSubscription> findByMemberIdOrderByStartDateDesc(Long memberId);

    List<MemberSubscription> findByMemberIdAndStatusOrderByEndDateDesc(Long memberId, SubscriptionStatus status);

    boolean existsByMembershipId(Long membershipId);

    boolean existsByCode(String code);
}
