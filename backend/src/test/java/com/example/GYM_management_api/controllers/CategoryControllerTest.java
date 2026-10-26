package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.CategoryDto;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.ICategoryService;
import com.example.GYM_management_api.utils.TestReportWatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: CategoryController (Tầng Controller - Quản lý Danh mục)")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ICategoryService categoryService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

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
    void getCategories_Returns200() throws Exception {
        Page<CategoryDto> page = new PageImpl<>(List.of(sampleCategoryDto));
        when(categoryService.getCategories(0, 10, "supp", "ACTIVE")).thenReturn(page);

        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "supp")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("CAT-SUPP"))
                .andExpect(jsonPath("$.content[0].name").value("Thực phẩm bổ sung"));

        verify(categoryService).getCategories(0, 10, "supp", "ACTIVE");
    }

    @Test
    @DisplayName("TC-170: Gọi API GET /api/categories/all trả về HTTP 200 OK với toàn bộ danh mục")
    void getAllCategories_Returns200() throws Exception {
        when(categoryService.getAllCategories("ACTIVE")).thenReturn(List.of(sampleCategoryDto));

        mockMvc.perform(get("/api/categories/all")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CAT-SUPP"));

        verify(categoryService).getAllCategories("ACTIVE");
    }

    @Test
    @DisplayName("TC-171: Gọi API GET /api/categories/{id} trả về HTTP 200 OK khi tìm thấy danh mục")
    void getCategoryById_Returns200() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(sampleCategoryDto);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CAT-SUPP"));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    @DisplayName("TC-172: Gọi API POST /api/categories trả về HTTP 201 CREATED khi tạo mới danh mục thành công")
    void createCategory_Returns201() throws Exception {
        CategoryDto request = CategoryDto.builder().name("Dụng cụ tập").code("CAT-GEAR").build();
        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(sampleCategoryDto);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CAT-SUPP"));

        verify(categoryService).createCategory(any(CategoryDto.class));
    }

    @Test
    @DisplayName("TC-173: Gọi API PUT /api/categories/{id} trả về HTTP 200 OK khi cập nhật danh mục thành công")
    void updateCategory_Returns200() throws Exception {
        CategoryDto request = CategoryDto.builder().name("Thực phẩm bổ sung cao cấp").build();
        when(categoryService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(sampleCategoryDto);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CAT-SUPP"));

        verify(categoryService).updateCategory(eq(1L), any(CategoryDto.class));
    }

    @Test
    @DisplayName("TC-174: Gọi API DELETE /api/categories/{id} trả về HTTP 200 OK khi xóa danh mục thành công")
    void deleteCategory_Returns200() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    @DisplayName("TC-175: Gọi API GET /api/categories/{id} không tìm thấy trả về HTTP 404 NOT FOUND")
    void getCategoryById_NotFound_Returns404() throws Exception {
        when(categoryService.getCategoryById(999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy danh mục với ID: 999"));

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy danh mục với ID: 999"));
    }
}
