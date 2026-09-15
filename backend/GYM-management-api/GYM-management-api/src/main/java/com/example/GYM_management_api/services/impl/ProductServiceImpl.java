package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.ProductRepository;
import com.example.GYM_management_api.services.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
}
