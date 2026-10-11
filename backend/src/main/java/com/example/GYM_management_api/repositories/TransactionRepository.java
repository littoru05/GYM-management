package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Transaction;
import com.example.GYM_management_api.entities.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByCode(String code);

    boolean existsByCode(String code);

    List<Transaction> findByTypeAndTransactionTimeBetweenOrderByTransactionTimeDesc(
            TransactionType type, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByTransactionTimeBetweenOrderByTransactionTimeDesc(
            LocalDateTime start, LocalDateTime end);

    List<Transaction> findByTypeOrderByTransactionTimeDesc(TransactionType type);
}
