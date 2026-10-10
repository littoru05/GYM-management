package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.CategoryDto;
import com.example.GYM_management_api.services.ICategoryService;
import com.example.GYM_management_api.utils.TestReportWatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử tầng Controller: CategoryController (Tầng Controller - Quản lý Danh mục)")
class CategoryControllerTest {

    @Mock
    private ICategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryDto sampleCategoryDto;

    @BeforeEach
    void setUp() {
        sampleCategoryDto = CategoryDto.builder()
                .id("1")
                .code("CAT-SUPP")
                .name("Thực phẩm bổ sung")
                .description("Sữa đạm và dinh dưỡng thể thao")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-169: Gọi API GET /api/categories trả về HTTP 200 OK với danh sách phân trang")
    void getCategories_Returns200() {
        Page<CategoryDto> page = new PageImpl<>(List.of(sampleCategoryDto));
        when(categoryService.getCategories(0, 10, "supp", "ACTIVE")).thenReturn(page);

        ResponseEntity<Page<CategoryDto>> response = categoryController.getCategories(0, 10, "supp", "ACTIVE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("CAT-SUPP", response.getBody().getContent().get(0).getCode());
        verify(categoryService, times(1)).getCategories(0, 10, "supp", "ACTIVE");
    }

    @Test
    @DisplayName("TC-170: Gọi API GET /api/categories/all trả về HTTP 200 OK với toàn bộ danh mục")
    void getAllCategories_Returns200() {
        when(categoryService.getAllCategories("ACTIVE")).thenReturn(List.of(sampleCategoryDto));

        ResponseEntity<List<CategoryDto>> response = categoryController.getAllCategories("ACTIVE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("CAT-SUPP", response.getBody().get(0).getCode());
        verify(categoryService, times(1)).getAllCategories("ACTIVE");
    }

    @Test
    @DisplayName("TC-171: Gọi API GET /api/categories/{id} trả về HTTP 200 OK khi tìm thấy danh mục")
    void getCategoryById_Returns200() {
        when(categoryService.getCategoryById(1L)).thenReturn(sampleCategoryDto);

        ResponseEntity<CategoryDto> response = categoryController.getCategoryById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CAT-SUPP", response.getBody().getCode());
        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    @DisplayName("TC-172: Gọi API POST /api/categories trả về HTTP 201 CREATED khi tạo mới danh mục thành công")
    void createCategory_Returns201() {
        CategoryDto request = CategoryDto.builder().name("Dụng cụ tập").code("CAT-GEAR").build();
        when(categoryService.createCategory(request)).thenReturn(sampleCategoryDto);

        ResponseEntity<CategoryDto> response = categoryController.createCategory(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(categoryService, times(1)).createCategory(request);
    }

    @Test
    @DisplayName("TC-173: Gọi API PUT /api/categories/{id} trả về HTTP 200 OK khi cập nhật danh mục thành công")
    void updateCategory_Returns200() {
        CategoryDto request = CategoryDto.builder().name("Thực phẩm bổ sung cao cấp").build();
        when(categoryService.updateCategory(1L, request)).thenReturn(sampleCategoryDto);

        ResponseEntity<CategoryDto> response = categoryController.updateCategory(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(categoryService, times(1)).updateCategory(1L, request);
    }

    @Test
    @DisplayName("TC-174: Gọi API DELETE /api/categories/{id} trả về HTTP 200 OK khi xóa danh mục thành công")
    void deleteCategory_Returns200() {
        doNothing().when(categoryService).deleteCategory(1L);

        ResponseEntity<?> response = categoryController.deleteCategory(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(categoryService, times(1)).deleteCategory(1L);
    }
}

