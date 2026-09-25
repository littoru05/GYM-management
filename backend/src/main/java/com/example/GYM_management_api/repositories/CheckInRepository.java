package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.CheckIn;
import com.example.GYM_management_api.entities.enums.CheckInStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findAllByOrderByCheckInTimeDesc();

    List<CheckIn> findByCheckInTimeBetweenOrderByCheckInTimeDesc(LocalDateTime start, LocalDateTime end);

    Optional<CheckIn> findTopByMemberIdAndStatusOrderByCheckInTimeDesc(Long memberId, CheckInStatus status);

    boolean existsByCode(String code);
}
