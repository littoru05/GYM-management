package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Membership;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByCode(String code);

    Optional<Membership> findByCode(String code);

    List<Membership> findByStatus(CommonStatus status);
}
