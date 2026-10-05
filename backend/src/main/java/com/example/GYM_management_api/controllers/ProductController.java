package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.dtos.StockAdjustmentDto;
import com.example.GYM_management_api.dtos.StockImportDto;
import com.example.GYM_management_api.services.IProductService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Quản lý Sản phẩm & Kho hàng (Products & Inventory)", description = "APIs CRUD sản phẩm, nhập kho, kiểm kê kho và kiểm soát min_stock (UC12)")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private final IProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<Page<ProductDto>> getProducts(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Từ khóa tìm kiếm theo tên hoặc mã SKU", example = "Whey")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo ID danh mục", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Lọc các sản phẩm có tồn kho thấp (stock <= min_stock)", example = "false")
            @RequestParam(required = false) Boolean lowStock) {
        Page<ProductDto> result = productService.getProducts(page, size, keyword, categoryId, status, lowStock);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<Page<ProductDto>> getLowStockProducts(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Page<ProductDto> result = productService.getLowStockProducts(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    })
    public ResponseEntity<Map<String, Object>> getInventorySummary() {
        Map<String, Object> summary = productService.getInventorySummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "ID sản phẩm", example = "1")
            @PathVariable Long id) {
        ProductDto dto = productService.getProductById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<ProductDto> getProductBySku(
            @Parameter(description = "Mã SKU", example = "WHEY-GOLD-5LBS")
            @PathVariable String sku) {
        ProductDto dto = productService.getProductBySku(sku);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo sản phẩm thành công",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (số âm, danh mục không tồn tại)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Từ chối quyền (Chỉ ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Trùng mã SKU",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto request) {
        ProductDto created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "ID sản phẩm cần sửa", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductDto request) {
        ProductDto updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<?> deleteProduct(
            @Parameter(description = "ID sản phẩm cần xóa", example = "1")
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of(
                "status", HttpStatus.OK.value(),
                "message", "Xóa sản phẩm thành công."
        ));
    }

    @PostMapping("/{id}/import")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nhập kho thành công",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Số lượng nhập <= 0 hoặc giá vốn âm",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<ProductDto> importStock(
            @Parameter(description = "ID sản phẩm", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dữ liệu nhập hàng tăng tồn kho",
                    content = @Content(
                            schema = @Schema(implementation = StockImportDto.class),
                            examples = @ExampleObject(
                                    name = "Ví dụ nhập hàng",
                                    summary = "Nhập thêm 20 sản phẩm vào kho",
                                    value = "{\n  \"quantity\": 20,\n  \"cost\": 1200000,\n  \"note\": \"Nhập lô hàng mới tháng 10\"\n}"
                            )
                    )
            )
            @Valid @RequestBody StockImportDto request) {
        ProductDto updated = productService.importStock(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Điều chỉnh tồn kho thành công",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Số lượng tồn kho âm hoặc không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<ProductDto> adjustStock(
            @Parameter(description = "ID sản phẩm", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dữ liệu kiểm kê hoặc điều chỉnh tồn kho",
                    content = @Content(
                            schema = @Schema(implementation = StockAdjustmentDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "1. Chốt tồn theo kiểm kê thực tế (Khuyên dùng)",
                                            summary = "Gán trực tiếp số lượng thực tế sau kiểm kê",
                                            value = "{\n  \"actualStock\": 45,\n  \"reason\": \"Kiểm kê định kỳ tháng 10\"\n}"
                                    ),
                                    @ExampleObject(
                                            name = "2. Điều chỉnh hao hụt / xuất bớt",
                                            summary = "Cộng trừ độ lệch so với tồn cũ",
                                            value = "{\n  \"quantityChange\": -5,\n  \"reason\": \"Hao hụt rách bao bì\"\n}"
                                    )
                            }
                    )
            )
            @Valid @RequestBody StockAdjustmentDto request) {
        ProductDto updated = productService.adjustStock(id, request);
        return ResponseEntity.ok(updated);
    }
}
