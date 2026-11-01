package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.TransactionRepository;
import com.example.GYM_management_api.services.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository transactionRepository;
}
