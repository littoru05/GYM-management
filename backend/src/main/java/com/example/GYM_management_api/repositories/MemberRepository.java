package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    boolean existsByCode(String code);

    Optional<Member> findByCode(String code);

    Optional<Member> findByPhone(String phone);

    @Query("SELECT m FROM Member m WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Member> searchMembers(@Param("keyword") String keyword, Pageable pageable);
}
