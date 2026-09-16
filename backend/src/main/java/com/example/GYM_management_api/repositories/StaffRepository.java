package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmailIgnoreCase(String email);
    Optional<Staff> findByCode(String code);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByCode(String code);
    boolean existsByPhone(String phone);
}
