package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.dtos.StockAdjustmentDto;
import com.example.GYM_management_api.dtos.StockImportDto;
import com.example.GYM_management_api.services.IProductService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử tầng Controller: ProductController (Tầng Controller - Quản lý Sản phẩm & Kho hàng)")
class ProductControllerTest {

    @Mock
    private IProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductDto sampleProductDto;

    @BeforeEach
    void setUp() {
        sampleProductDto = ProductDto.builder()
                .id("1")
                .sku("WHEY-GOLD-5LBS")
                .name("Sữa bột Whey Gold Standard 5lbs")
                .category("SUPPLEMENT")
                .categoryId("1")
                .price(new BigDecimal("1650000"))
                .cost(new BigDecimal("1200000"))
                .stock(50)
                .minStock(10)
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("TC-175: Gọi API GET /api/products trả về HTTP 200 OK với danh sách sản phẩm phân trang")
    void getProducts_Returns200() {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.getProducts(0, 10, "whey", 1L, "ACTIVE", false)).thenReturn(page);

        ResponseEntity<Page<ProductDto>> response = productController.getProducts(0, 10, "whey", 1L, "ACTIVE", false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("WHEY-GOLD-5LBS", response.getBody().getContent().get(0).getSku());
        verify(productService, times(1)).getProducts(0, 10, "whey", 1L, "ACTIVE", false);
    }

    @Test
    @DisplayName("TC-176: Gọi API GET /api/products/low-stock trả về HTTP 200 OK với danh sách sản phẩm sắp hết hàng")
    void getLowStockProducts_Returns200() {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.getLowStockProducts(0, 10)).thenReturn(page);

        ResponseEntity<Page<ProductDto>> response = productController.getLowStockProducts(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(productService, times(1)).getLowStockProducts(0, 10);
    }

    @Test
    @DisplayName("TC-177: Gọi API GET /api/products/inventory-summary trả về HTTP 200 OK với thống kê tổng quan kho")
    void getInventorySummary_Returns200() {
        Map<String, Object> summary = Map.of(
                "totalProducts", 25L,
                "totalStock", 600L,
                "lowStockCount", 2L,
                "totalInventoryCost", new BigDecimal("180000000")
        );
        when(productService.getInventorySummary()).thenReturn(summary);

        ResponseEntity<Map<String, Object>> response = productController.getInventorySummary();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(25L, response.getBody().get("totalProducts"));
        verify(productService, times(1)).getInventorySummary();
    }

    @Test
    @DisplayName("TC-178: Gọi API GET /api/products/{id} trả về HTTP 200 OK khi tìm thấy sản phẩm theo ID")
    void getProductById_Returns200() {
        when(productService.getProductById(1L)).thenReturn(sampleProductDto);

        ResponseEntity<ProductDto> response = productController.getProductById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("WHEY-GOLD-5LBS", response.getBody().getSku());
        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("TC-179: Gọi API GET /api/products/sku/{sku} trả về HTTP 200 OK khi tìm thấy sản phẩm theo SKU")
    void getProductBySku_Returns200() {
        when(productService.getProductBySku("WHEY-GOLD-5LBS")).thenReturn(sampleProductDto);

        ResponseEntity<ProductDto> response = productController.getProductBySku("WHEY-GOLD-5LBS");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().getId());
        verify(productService, times(1)).getProductBySku("WHEY-GOLD-5LBS");
    }

    @Test
    @DisplayName("TC-180: Gọi API POST /api/products trả về HTTP 201 CREATED khi tạo mới sản phẩm thành công")
    void createProduct_Returns201() {
        ProductDto request = ProductDto.builder().name("Whey Isolate").sku("WHEY-ISO").build();
        when(productService.createProduct(request)).thenReturn(sampleProductDto);

        ResponseEntity<ProductDto> response = productController.createProduct(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(productService, times(1)).createProduct(request);
    }

    @Test
    @DisplayName("TC-181: Gọi API PUT /api/products/{id} trả về HTTP 200 OK khi cập nhật sản phẩm thành công")
    void updateProduct_Returns200() {
        ProductDto request = ProductDto.builder().name("Whey Gold Cập Nhật").build();
        when(productService.updateProduct(1L, request)).thenReturn(sampleProductDto);

        ResponseEntity<ProductDto> response = productController.updateProduct(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(productService, times(1)).updateProduct(1L, request);
    }

    @Test
    @DisplayName("TC-182: Gọi API DELETE /api/products/{id} trả về HTTP 200 OK khi xóa sản phẩm thành công")
    void deleteProduct_Returns200() {
        doNothing().when(productService).deleteProduct(1L);

        ResponseEntity<?> response = productController.deleteProduct(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    @DisplayName("TC-183: Gọi API POST /api/products/{id}/import trả về HTTP 200 OK khi nhập hàng thành công")
    void importStock_Returns200() {
        StockImportDto request = StockImportDto.builder().quantity(15).cost(new BigDecimal("1200000")).build();
        when(productService.importStock(1L, request)).thenReturn(sampleProductDto);

        ResponseEntity<ProductDto> response = productController.importStock(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(productService, times(1)).importStock(1L, request);
    }

    @Test
    @DisplayName("TC-184: Gọi API POST /api/products/{id}/adjust trả về HTTP 200 OK khi điều chỉnh tồn kho thành công")
    void adjustStock_Returns200() {
        StockAdjustmentDto request = StockAdjustmentDto.builder().actualStock(45).reason("Kiểm kê").build();
        when(productService.adjustStock(1L, request)).thenReturn(sampleProductDto);

        ResponseEntity<ProductDto> response = productController.adjustStock(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(productService, times(1)).adjustStock(1L, request);
    }
}

