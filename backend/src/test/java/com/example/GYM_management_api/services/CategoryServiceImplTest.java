package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.CategoryDto;
import com.example.GYM_management_api.entities.Category;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.CategoryMapper;
import com.example.GYM_management_api.repositories.CategoryRepository;
import com.example.GYM_management_api.repositories.ProductRepository;
import com.example.GYM_management_api.services.impl.CategoryServiceImpl;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ CategoryServiceImpl (Tầng Service - Quản lý Danh mục)")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category sampleCategory;
    private CategoryDto sampleCategoryDto;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .code("CAT-SUPP")
                .name("Thực phẩm bổ sung")
                .description("Các loại sữa Whey, Mass, BCAA và Pre-workout")
                .status(CommonStatus.ACTIVE)
                .build();
        sampleCategory.setId(1L);

        sampleCategoryDto = CategoryDto.builder()
                .id("1")
                .code("CAT-SUPP")
                .name("Thực phẩm bổ sung")
                .description("Các loại sữa Whey, Mass, BCAA và Pre-workout")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-126: Lấy danh sách danh mục có phân trang và tìm kiếm theo từ khóa (keyword)")
    void getCategories_WithKeyword_ReturnsPagedCategories() {
        Page<Category> page = new PageImpl<>(List.of(sampleCategory));
        when(categoryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toDto(sampleCategory)).thenReturn(sampleCategoryDto);

        Page<CategoryDto> result = categoryService.getCategories(0, 10, "bổ sung", null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CAT-SUPP", result.getContent().get(0).getCode());
        verify(categoryRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-127: Lấy danh sách danh mục có lọc theo trạng thái (status ACTIVE/INACTIVE)")
    void getCategories_WithStatusFilter_ReturnsFilteredCategories() {
        Page<Category> page = new PageImpl<>(List.of(sampleCategory));
        when(categoryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toDto(sampleCategory)).thenReturn(sampleCategoryDto);

        Page<CategoryDto> result = categoryService.getCategories(0, 10, null, "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ACTIVE", result.getContent().get(0).getStatus());
        verify(categoryRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-128: Lấy toàn bộ danh mục hoạt động (getAllCategories) để hiển thị dropdown")
    void getAllCategories_ActiveStatus_ReturnsCategoryList() {
        when(categoryRepository.findByStatus(CommonStatus.ACTIVE)).thenReturn(List.of(sampleCategory));
        when(categoryMapper.toDto(sampleCategory)).thenReturn(sampleCategoryDto);

        List<CategoryDto> result = categoryService.getAllCategories("ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CAT-SUPP", result.get(0).getCode());
        verify(categoryRepository, times(1)).findByStatus(CommonStatus.ACTIVE);
    }

    @Test
    @DisplayName("TC-129: Lấy chi tiết danh mục theo ID thành công khi ID tồn tại")
    void getCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryMapper.toDto(sampleCategory)).thenReturn(sampleCategoryDto);

        CategoryDto result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("CAT-SUPP", result.getCode());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TC-130: Báo lỗi ResourceNotFoundException khi tìm chi tiết danh mục với ID không tồn tại")
    void getCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getCategoryById(999L)
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy danh mục với ID: 999"));
        verify(categoryRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("TC-131: Tạo mới danh mục thành công với thông tin hợp lệ")
    void createCategory_Success() {
        CategoryDto request = CategoryDto.builder()
                .code("CAT-DRINK")
                .name("Nước giải khát")
                .description("Các loại nước khoáng, nước tăng lực thể thao")
                .status("ACTIVE")
                .build();

        Category createdEntity = Category.builder()
                .code("CAT-DRINK")
                .name("Nước giải khát")
                .description("Các loại nước khoáng, nước tăng lực thể thao")
                .status(CommonStatus.ACTIVE)
                .build();
        createdEntity.setId(2L);

        CategoryDto createdDto = CategoryDto.builder()
                .id("2")
                .code("CAT-DRINK")
                .name("Nước giải khát")
                .status("ACTIVE")
                .build();

        when(categoryRepository.existsByCode("CAT-DRINK")).thenReturn(false);
        when(categoryRepository.existsByName("Nước giải khát")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(createdEntity);
        when(categoryRepository.save(createdEntity)).thenReturn(createdEntity);
        when(categoryMapper.toDto(createdEntity)).thenReturn(createdDto);

        CategoryDto result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("2", result.getId());
        assertEquals("CAT-DRINK", result.getCode());
        verify(categoryRepository, times(1)).existsByCode("CAT-DRINK");
        verify(categoryRepository, times(1)).existsByName("Nước giải khát");
        verify(categoryRepository, times(1)).save(createdEntity);
    }

    @Test
    @DisplayName("TC-132: Báo lỗi DuplicateResourceException khi tạo danh mục với mã Code đã tồn tại")
    void createCategory_DuplicateCode_ThrowsException() {
        CategoryDto request = CategoryDto.builder()
                .code("CAT-SUPP")
                .name("Tên danh mục khác")
                .build();

        when(categoryRepository.existsByCode("CAT-SUPP")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                categoryService.createCategory(request)
        );

        assertTrue(ex.getMessage().contains("Mã danh mục 'CAT-SUPP' đã tồn tại trong hệ thống."));
        verify(categoryRepository, times(1)).existsByCode("CAT-SUPP");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-133: Báo lỗi DuplicateResourceException khi tạo danh mục với tên Name đã tồn tại")
    void createCategory_DuplicateName_ThrowsException() {
        CategoryDto request = CategoryDto.builder()
                .code("CAT-NEW")
                .name("Thực phẩm bổ sung")
                .build();

        when(categoryRepository.existsByCode("CAT-NEW")).thenReturn(false);
        when(categoryRepository.existsByName("Thực phẩm bổ sung")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                categoryService.createCategory(request)
        );

        assertTrue(ex.getMessage().contains("Tên danh mục 'Thực phẩm bổ sung' đã tồn tại trong hệ thống."));
        verify(categoryRepository, times(1)).existsByName("Thực phẩm bổ sung");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-134: Cập nhật thông tin danh mục thành công khi dữ liệu hợp lệ")
    void updateCategory_Success() {
        CategoryDto updateRequest = CategoryDto.builder()
                .code("CAT-SUPP-EDIT")
                .name("Thực phẩm bổ sung (Đã cập nhật)")
                .description("Mô tả mới")
                .status("ACTIVE")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.existsByCodeAndIdNot("CAT-SUPP-EDIT", 1L)).thenReturn(false);
        when(categoryRepository.existsByNameAndIdNot("Thực phẩm bổ sung (Đã cập nhật)", 1L)).thenReturn(false);
        doAnswer(invocation -> {
            sampleCategory.setCode("CAT-SUPP-EDIT");
            sampleCategory.setName("Thực phẩm bổ sung (Đã cập nhật)");
            return null;
        }).when(categoryMapper).updateEntityFromDto(updateRequest, sampleCategory);
        when(categoryRepository.save(sampleCategory)).thenReturn(sampleCategory);

        CategoryDto updatedDto = CategoryDto.builder()
                .id("1")
                .code("CAT-SUPP-EDIT")
                .name("Thực phẩm bổ sung (Đã cập nhật)")
                .status("ACTIVE")
                .build();
        when(categoryMapper.toDto(sampleCategory)).thenReturn(updatedDto);

        CategoryDto result = categoryService.updateCategory(1L, updateRequest);

        assertNotNull(result);
        assertEquals("CAT-SUPP-EDIT", result.getCode());
        assertEquals("Thực phẩm bổ sung (Đã cập nhật)", result.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(sampleCategory);
    }

    @Test
    @DisplayName("TC-135: Báo lỗi ResourceNotFoundException khi cập nhật danh mục với ID không tồn tại")
    void updateCategory_NotFound_ThrowsException() {
        CategoryDto updateRequest = CategoryDto.builder().name("Cập nhật").build();
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                categoryService.updateCategory(999L, updateRequest)
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy danh mục với ID: 999"));
        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-136: Báo lỗi DuplicateResourceException khi cập nhật trùng mã Code của danh mục khác")
    void updateCategory_DuplicateCode_ThrowsException() {
        CategoryDto updateRequest = CategoryDto.builder().code("CAT-DUPLICATE").name("Mới").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.existsByCodeAndIdNot("CAT-DUPLICATE", 1L)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                categoryService.updateCategory(1L, updateRequest)
        );

        assertTrue(ex.getMessage().contains("Mã danh mục 'CAT-DUPLICATE' đã được sử dụng bởi danh mục khác."));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-137: Báo lỗi DuplicateResourceException khi cập nhật trùng tên Name của danh mục khác")
    void updateCategory_DuplicateName_ThrowsException() {
        CategoryDto updateRequest = CategoryDto.builder().code("CAT-OK").name("Tên Trùng Lặp").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.existsByCodeAndIdNot("CAT-OK", 1L)).thenReturn(false);
        when(categoryRepository.existsByNameAndIdNot("Tên Trùng Lặp", 1L)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                categoryService.updateCategory(1L, updateRequest)
        );

        assertTrue(ex.getMessage().contains("Tên danh mục 'Tên Trùng Lặp' đã được sử dụng bởi danh mục khác."));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-138: Xóa danh mục thành công khi danh mục không chứa sản phẩm liên kết")
    void deleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);
        doNothing().when(categoryRepository).delete(sampleCategory);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));

        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).existsByCategoryId(1L);
        verify(categoryRepository, times(1)).delete(sampleCategory);
    }

    @Test
    @DisplayName("TC-139: Báo lỗi ResourceNotFoundException khi xóa danh mục với ID không tồn tại")
    void deleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                categoryService.deleteCategory(999L)
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy danh mục với ID: 999"));
        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("TC-140: Báo lỗi BadRequestException khi xóa danh mục đang có sản phẩm liên kết")
    void deleteCategory_HasProducts_ThrowsBadRequest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                categoryService.deleteCategory(1L)
        );

        assertTrue(ex.getMessage().contains("Danh mục đang chứa sản phẩm liên kết, không thể xóa khỏi hệ thống."));
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).existsByCategoryId(1L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
