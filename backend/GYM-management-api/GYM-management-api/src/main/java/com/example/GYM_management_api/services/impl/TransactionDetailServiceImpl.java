package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.TransactionDetailRepository;
import com.example.GYM_management_api.services.ITransactionDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionDetailServiceImpl implements ITransactionDetailService {

    private final TransactionDetailRepository transactionDetailRepository;
}
