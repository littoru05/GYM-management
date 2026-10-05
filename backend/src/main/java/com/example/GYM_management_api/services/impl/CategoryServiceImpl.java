package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.CategoryDto;
import com.example.GYM_management_api.entities.Category;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.CategoryMapper;
import com.example.GYM_management_api.repositories.CategoryRepository;
import com.example.GYM_management_api.repositories.ProductRepository;
import com.example.GYM_management_api.services.ICategoryService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getCategories(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"));

        Specification<Category> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String search = "%" + keyword.trim().toLowerCase() + "%";
                Predicate codePredicate = cb.like(cb.lower(root.get("code")), search);
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), search);
                predicates.add(cb.or(codePredicate, namePredicate));
            }

            if (status != null && !status.trim().isEmpty()) {
                try {
                    CommonStatus statusEnum = CommonStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {}
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return categoryRepository.findAll(spec, pageable).map(categoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(String status) {
        if (status != null && !status.trim().isEmpty()) {
            try {
                CommonStatus statusEnum = CommonStatus.valueOf(status.trim().toUpperCase());
                return categoryRepository.findByStatus(statusEnum).stream()
                        .map(categoryMapper::toDto)
                        .toList();
            } catch (IllegalArgumentException ignored) {}
        }
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        return categoryMapper.toDto(category);
    }

    @Override
    public CategoryDto createCategory(CategoryDto dto) {
        if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
            String code = dto.getCode().trim().toUpperCase();
            if (categoryRepository.existsByCode(code)) {
                throw new DuplicateResourceException("Mã danh mục '" + code + "' đã tồn tại trong hệ thống.");
            }
        }

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            String name = dto.getName().trim();
            if (categoryRepository.existsByName(name)) {
                throw new DuplicateResourceException("Tên danh mục '" + name + "' đã tồn tại trong hệ thống.");
            }
        }

        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryRepository.save(category);
        log.info("Đã tạo danh mục mới: ID={}, Code={}, Name={}", saved.getId(), saved.getCode(), saved.getName());
        return categoryMapper.toDto(saved);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
            String code = dto.getCode().trim().toUpperCase();
            if (categoryRepository.existsByCodeAndIdNot(code, id)) {
                throw new DuplicateResourceException("Mã danh mục '" + code + "' đã được sử dụng bởi danh mục khác.");
            }
        }

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            String name = dto.getName().trim();
            if (categoryRepository.existsByNameAndIdNot(name, id)) {
                throw new DuplicateResourceException("Tên danh mục '" + name + "' đã được sử dụng bởi danh mục khác.");
            }
        }

        categoryMapper.updateEntityFromDto(dto, category);
        Category updated = categoryRepository.save(category);
        log.info("Đã cập nhật danh mục: ID={}, Code={}, Name={}", updated.getId(), updated.getCode(), updated.getName());
        return categoryMapper.toDto(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Danh mục đang chứa sản phẩm liên kết, không thể xóa khỏi hệ thống.");
        }

        categoryRepository.delete(category);
        log.info("Đã xóa danh mục: ID={}, Code={}", id, category.getCode());
    }
}
