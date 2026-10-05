package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.CategoryDto;
import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.services.ICategoryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Quản lý Danh mục (Categories)", description = "APIs CRUD danh mục sản phẩm (UC12)")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Page<CategoryDto>> getCategories(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Từ khóa tìm kiếm theo mã hoặc tên", example = "Nước")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) String status) {
        Page<CategoryDto> result = categoryService.getCategories(page, size, keyword, status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class))))
    })
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) String status) {
        List<CategoryDto> result = categoryService.getAllCategories(status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "ID danh mục", example = "1")
            @PathVariable Long id) {
        CategoryDto dto = categoryService.getCategoryById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo danh mục thành công",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Từ chối quyền (Chỉ ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Trùng mã hoặc tên danh mục",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto request) {
        CategoryDto created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "ID danh mục cần sửa", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto request) {
        CategoryDto updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa do ràng buộc sản phẩm",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID danh mục cần xóa", example = "1")
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of(
                "status", HttpStatus.OK.value(),
                "message", "Xóa danh mục thành công."
        ));
    }
}
