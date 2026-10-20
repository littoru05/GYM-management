package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Transaction;
import com.example.GYM_management_api.entities.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    interface DailyRevenueProjection {
        String getReportDate();
        BigDecimal getMembershipRevenue();
        BigDecimal getRetailRevenue();
        BigDecimal getTotalRevenue();
    }

    Optional<Transaction> findByCode(String code);

    boolean existsByCode(String code);

    List<Transaction> findByTypeAndTransactionTimeBetweenOrderByTransactionTimeDesc(
            TransactionType type, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByTransactionTimeBetweenOrderByTransactionTimeDesc(
            LocalDateTime start, LocalDateTime end);

    List<Transaction> findByTypeOrderByTransactionTimeDesc(TransactionType type);

    @Query(value = """
            SELECT 
                DATE_FORMAT(t.transaction_time, '%Y-%m-%d') AS reportDate,
                COALESCE(SUM(CASE WHEN t.type = 'MEMBERSHIP' THEN t.amount ELSE 0 END), 0) AS membershipRevenue,
                COALESCE(SUM(CASE WHEN t.type = 'RETAIL' THEN t.amount ELSE 0 END), 0) AS retailRevenue,
                COALESCE(SUM(t.amount), 0) AS totalRevenue
            FROM transactions t
            WHERE t.transaction_time >= :startTime AND t.transaction_time <= :endTime
            GROUP BY DATE_FORMAT(t.transaction_time, '%Y-%m-%d')
            ORDER BY reportDate ASC
            """, nativeQuery = true)
    List<DailyRevenueProjection> getDailyRevenueReport(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
