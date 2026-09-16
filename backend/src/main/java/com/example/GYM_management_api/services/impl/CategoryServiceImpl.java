package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.repositories.CategoryRepository;
import com.example.GYM_management_api.services.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
}
