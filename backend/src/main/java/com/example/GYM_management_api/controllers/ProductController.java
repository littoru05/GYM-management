package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ErrorResponseDto;
import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.dtos.StockAdjustmentDto;
import com.example.GYM_management_api.dtos.StockImportDto;
import com.example.GYM_management_api.services.IProductService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Quản lý Sản phẩm & Kho hàng (Products & Inventory)", description = "APIs CRUD sản phẩm, bán hàng POS, nhập kho, kiểm kê kho và kiểm soát min_stock")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

        private final IProductService productService;

        @GetMapping({ "/products", "/staff/products", "/pos/products" })
        @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
        @Operation(summary = "Lấy danh sách sản phẩm", description = "Hỗ trợ các route /api/products, /api/staff/products, /api/pos/products kèm phân trang, tìm kiếm và trả về imageUrl.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
                        @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<Page<ProductDto>> getProducts(
                        @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng bản ghi mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Từ khóa tìm kiếm theo tên hoặc mã SKU", example = "Whey") @RequestParam(required = false) String keyword,
                        @Parameter(description = "Lọc theo ID danh mục", example = "1") @RequestParam(required = false) Long categoryId,
                        @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)", example = "ACTIVE") @RequestParam(required = false) String status,
                        @Parameter(description = "Lọc các sản phẩm có tồn kho thấp (stock <= min_stock)", example = "false") @RequestParam(required = false) Boolean lowStock) {
                Page<ProductDto> result = productService.getProducts(page, size, keyword, categoryId, status, lowStock);
                return ResponseEntity.ok(result);
        }

        @GetMapping({ "/products/low-stock", "/staff/products/low-stock" })
        @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
        @Operation(summary = "Lấy danh sách sản phẩm sắp hết hàng (stock <= min_stock)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
        })
        public ResponseEntity<Page<ProductDto>> getLowStockProducts(
                        @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng bản ghi mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size) {
                Page<ProductDto> result = productService.getLowStockProducts(page, size);
                return ResponseEntity.ok(result);
        }

        @GetMapping({ "/products/inventory-summary", "/admin/products/inventory-summary" })
        @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
        @Operation(summary = "Thống kê tổng quan kho hàng")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
        })
        public ResponseEntity<Map<String, Object>> getInventorySummary() {
                Map<String, Object> summary = productService.getInventorySummary();
                return ResponseEntity.ok(summary);
        }

        @GetMapping({ "/products/{id}", "/staff/products/{id}", "/pos/products/{id}", "/admin/products/{id}" })
        @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
        @Operation(summary = "Lấy chi tiết sản phẩm theo ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công", content = @Content(schema = @Schema(implementation = ProductDto.class))),
                        @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<ProductDto> getProductById(
                        @Parameter(description = "ID sản phẩm", example = "1") @PathVariable Long id) {
                ProductDto dto = productService.getProductById(id);
                return ResponseEntity.ok(dto);
        }

        @GetMapping({ "/products/sku/{sku}", "/staff/products/sku/{sku}", "/pos/products/sku/{sku}" })
        @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
        @Operation(summary = "Lấy chi tiết sản phẩm theo mã SKU")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công", content = @Content(schema = @Schema(implementation = ProductDto.class))),
                        @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<ProductDto> getProductBySku(
                        @Parameter(description = "Mã SKU", example = "WHEY-GOLD-5LBS") @PathVariable String sku) {
                ProductDto dto = productService.getProductBySku(sku);
                return ResponseEntity.ok(dto);
        }

        @PostMapping({ "/admin/products", "/products" })
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Tạo sản phẩm mới", description = "Tiếp nhận ProductDto kèm chuỗi imageUrl, nếu rỗng sẽ gán ảnh placeholder mặc định.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Tạo sản phẩm thành công", content = @Content(schema = @Schema(implementation = ProductDto.class))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (số âm, danh mục không tồn tại)", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "403", description = "Từ chối quyền (Chỉ ADMIN)", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "409", description = "Trùng mã SKU", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto request) {
                ProductDto created = productService.createProduct(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        @PutMapping({ "/admin/products/{id}", "/products/{id}" })
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Cập nhật sản phẩm", description = "Tiếp nhận ProductDto kèm chuỗi imageUrl, lưu vào DB và trả về ProductDto đầy đủ.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content(schema = @Schema(implementation = ProductDto.class))),
                        @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<ProductDto> updateProduct(
                        @Parameter(description = "ID sản phẩm cần sửa", example = "1") @PathVariable Long id,
                        @Valid @RequestBody ProductDto request) {
                ProductDto updated = productService.updateProduct(id, request);
                return ResponseEntity.ok(updated);
        }

        @DeleteMapping({ "/admin/products/{id}", "/products/{id}" })
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xóa sản phẩm")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Xóa thành công"),
                        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<Map<String, Object>> deleteProduct(
                        @Parameter(description = "ID sản phẩm", example = "1") @PathVariable Long id) {
                productService.deleteProduct(id);
                return ResponseEntity.ok(Map.of(
                                "status", HttpStatus.OK.value(),
                                "message", "Xóa sản phẩm thành công."));
        }

        @PostMapping({ "/admin/products/{id}/import", "/products/{id}/import" })
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Nhập thêm hàng vào kho")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Nhập kho thành công", content = @Content(schema = @Schema(implementation = ProductDto.class))),
                        @ApiResponse(responseCode = "400", description = "Số lượng nhập <= 0 hoặc giá vốn âm", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<ProductDto> importStock(
                        @Parameter(description = "ID sản phẩm", example = "1") @PathVariable Long id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dữ liệu nhập hàng tăng tồn kho", content = @Content(schema = @Schema(implementation = StockImportDto.class), examples = @ExampleObject(name = "Ví dụ nhập hàng", summary = "Nhập thêm 20 sản phẩm vào kho", value = "{\n  \"quantity\": 20,\n  \"cost\": 1200000,\n  \"note\": \"Nhập lô hàng mới tháng 10\"\n}"))) @Valid @RequestBody StockImportDto request) {
                ProductDto updated = productService.importStock(id, request);
                return ResponseEntity.ok(updated);
        }

        @PostMapping({ "/admin/products/{id}/adjust", "/products/{id}/adjust" })
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Điều chỉnh số lượng kiểm kê kho")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Điều chỉnh tồn kho thành công", content = @Content(schema = @Schema(implementation = ProductDto.class))),
                        @ApiResponse(responseCode = "400", description = "Số lượng tồn kho âm hoặc không hợp lệ", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        public ResponseEntity<ProductDto> adjustStock(
                        @Parameter(description = "ID sản phẩm", example = "1") @PathVariable Long id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dữ liệu kiểm kê hoặc điều chỉnh tồn kho", content = @Content(schema = @Schema(implementation = StockAdjustmentDto.class), examples = {
                                        @ExampleObject(name = "1. Chốt tồn theo kiểm kê thực tế (Khuyên dùng)", summary = "Gán trực tiếp số lượng thực tế sau kiểm kê", value = "{\n  \"actualStock\": 45,\n  \"reason\": \"Kiểm kê định kỳ tháng 10\"\n}"),
                                        @ExampleObject(name = "2. Điều chỉnh hao hụt / xuất bớt", summary = "Cộng trừ độ lệch so với tồn cũ", value = "{\n  \"quantityChange\": -5,\n  \"reason\": \"Hao hụt rách bao bì\"\n}")
                        })) @Valid @RequestBody StockAdjustmentDto request) {
                ProductDto updated = productService.adjustStock(id, request);
                return ResponseEntity.ok(updated);
        }
}
