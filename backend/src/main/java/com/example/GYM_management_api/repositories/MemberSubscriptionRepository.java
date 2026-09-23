package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.MemberSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberSubscriptionRepository extends JpaRepository<MemberSubscription, Long> {

    List<MemberSubscription> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<MemberSubscription> findByMemberIdOrderByStartDateDesc(Long memberId);

    boolean existsByMembershipId(Long membershipId);
}
