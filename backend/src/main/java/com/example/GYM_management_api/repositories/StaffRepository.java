package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Staff;
import com.example.GYM_management_api.entities.enums.StaffRole;
import com.example.GYM_management_api.entities.enums.StaffStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByEmailIgnoreCase(String email);

    Optional<Staff> findByCode(String code);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCode(String code);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Query("SELECT s FROM Staff s WHERE " +
           "(:keyword IS NULL OR :keyword = '' " +
           " OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR s.phone LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:role IS NULL OR s.role = :role) " +
           "AND (:status IS NULL OR s.status = :status)")
    Page<Staff> searchStaffs(@Param("keyword") String keyword,
                             @Param("role") StaffRole role,
                             @Param("status") StaffStatus status,
                             Pageable pageable);
}
