package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query("SELECT m FROM Member m WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND m.status = com.example.GYM_management_api.entities.enums.MemberStatus.LOCKED")
    Page<Member> searchLockedMembers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND m.status <> com.example.GYM_management_api.entities.enums.MemberStatus.LOCKED " +
            "AND (" +
            "  (SELECT MAX(s.endDate) FROM MemberSubscription s WHERE s.member = m) < :today " +
            "  OR ((SELECT MAX(s.endDate) FROM MemberSubscription s WHERE s.member = m) IS NULL " +
            "      AND m.status = com.example.GYM_management_api.entities.enums.MemberStatus.EXPIRED)" +
            ")")
    Page<Member> searchExpiredMembers(@Param("keyword") String keyword, @Param("today") LocalDate today, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND m.status <> com.example.GYM_management_api.entities.enums.MemberStatus.LOCKED " +
            "AND (" +
            "  (SELECT MAX(s.endDate) FROM MemberSubscription s WHERE s.member = m) BETWEEN :today AND :soon " +
            "  OR ((SELECT MAX(s.endDate) FROM MemberSubscription s WHERE s.member = m) IS NULL " +
            "      AND m.status = com.example.GYM_management_api.entities.enums.MemberStatus.EXPIRING_SOON)" +
            ")")
    Page<Member> searchExpiringSoonMembers(@Param("keyword") String keyword, @Param("today") LocalDate today, @Param("soon") LocalDate soon, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND m.status <> com.example.GYM_management_api.entities.enums.MemberStatus.LOCKED " +
            "AND (" +
            "  (SELECT MAX(s.endDate) FROM MemberSubscription s WHERE s.member = m) > :soon " +
            "  OR ((SELECT MAX(s.endDate) FROM MemberSubscription s WHERE s.member = m) IS NULL " +
            "      AND m.status = com.example.GYM_management_api.entities.enums.MemberStatus.ACTIVE)" +
            ")")
    Page<Member> searchActiveMembers(@Param("keyword") String keyword, @Param("soon") LocalDate soon, Pageable pageable);
}
