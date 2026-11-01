package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.CategoryDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ICategoryService {

    Page<CategoryDto> getCategories(int page, int size, String keyword, String status);

    List<CategoryDto> getAllCategories(String status);

    CategoryDto getCategoryById(Long id);

    CategoryDto createCategory(CategoryDto dto);

    CategoryDto updateCategory(Long id, CategoryDto dto);

    void deleteCategory(Long id);
}
