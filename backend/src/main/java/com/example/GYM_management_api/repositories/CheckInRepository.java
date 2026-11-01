package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.CheckIn;
import com.example.GYM_management_api.entities.enums.CheckInStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    interface DailyCheckInProjection {
        String getReportDate();
        Long getCheckInCount();
    }

    List<CheckIn> findAllByOrderByCheckInTimeDesc();

    List<CheckIn> findByCheckInTimeBetweenOrderByCheckInTimeDesc(LocalDateTime start, LocalDateTime end);

    Optional<CheckIn> findTopByMemberIdAndStatusOrderByCheckInTimeDesc(Long memberId, CheckInStatus status);

    boolean existsByCode(String code);

    @Query(value = """
            SELECT 
                DATE_FORMAT(c.check_in_time, '%Y-%m-%d') AS reportDate,
                COUNT(c.id) AS checkInCount
            FROM check_ins c
            WHERE c.check_in_time >= :startTime AND c.check_in_time <= :endTime
              AND c.status = 'SUCCESS'
            GROUP BY DATE_FORMAT(c.check_in_time, '%Y-%m-%d')
            ORDER BY reportDate ASC
            """, nativeQuery = true)
    List<DailyCheckInProjection> getDailyCheckInReport(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
