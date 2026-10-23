package com.example.GYM_management_api.controllers;

import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.dtos.StockAdjustmentDto;
import com.example.GYM_management_api.dtos.StockImportDto;
import com.example.GYM_management_api.exceptions.GlobalExceptionHandler;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.security.JwtRequestFilter;
import com.example.GYM_management_api.services.IProductService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ExtendWith(TestReportWatcher.class)
@DisplayName("Kiểm thử tầng Controller: ProductController (Tầng Controller - Quản lý Sản phẩm & Kho hàng)")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private IProductService productService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

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
    void getProducts_Returns200() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.getProducts(0, 10, "whey", 1L, "ACTIVE", false)).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "whey")
                        .param("categoryId", "1")
                        .param("status", "ACTIVE")
                        .param("lowStock", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("WHEY-GOLD-5LBS"))
                .andExpect(jsonPath("$.content[0].name").value("Sữa bột Whey Gold Standard 5lbs"));

        verify(productService).getProducts(0, 10, "whey", 1L, "ACTIVE", false);
    }

    @Test
    @DisplayName("TC-176: Gọi API GET /api/products/low-stock trả về HTTP 200 OK với danh sách sản phẩm sắp hết hàng")
    void getLowStockProducts_Returns200() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleProductDto));
        when(productService.getLowStockProducts(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/products/low-stock")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("WHEY-GOLD-5LBS"));

        verify(productService).getLowStockProducts(0, 10);
    }

    @Test
    @DisplayName("TC-177: Gọi API GET /api/products/inventory-summary trả về HTTP 200 OK với thống kê tổng quan kho")
    void getInventorySummary_Returns200() throws Exception {
        Map<String, Object> summary = Map.of(
                "totalProducts", 25L,
                "totalStock", 600L,
                "lowStockCount", 2L,
                "totalInventoryCost", new BigDecimal("180000000")
        );
        when(productService.getInventorySummary()).thenReturn(summary);

        mockMvc.perform(get("/api/products/inventory-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(25L))
                .andExpect(jsonPath("$.totalStock").value(600L));

        verify(productService).getInventorySummary();
    }

    @Test
    @DisplayName("TC-178: Gọi API GET /api/products/{id} trả về HTTP 200 OK khi tìm thấy sản phẩm theo ID")
    void getProductById_Returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleProductDto);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("WHEY-GOLD-5LBS"));

        verify(productService).getProductById(1L);
    }

    @Test
    @DisplayName("TC-179: Gọi API GET /api/products/sku/{sku} trả về HTTP 200 OK khi tìm thấy sản phẩm theo SKU")
    void getProductBySku_Returns200() throws Exception {
        when(productService.getProductBySku("WHEY-GOLD-5LBS")).thenReturn(sampleProductDto);

        mockMvc.perform(get("/api/products/sku/WHEY-GOLD-5LBS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));

        verify(productService).getProductBySku("WHEY-GOLD-5LBS");
    }

    @Test
    @DisplayName("TC-180: Gọi API POST /api/products trả về HTTP 201 CREATED khi tạo mới sản phẩm thành công")
    void createProduct_Returns201() throws Exception {
        ProductDto request = ProductDto.builder()
                .name("Whey Isolate")
                .sku("WHEY-ISO")
                .category("SUPPLEMENT")
                .price(new BigDecimal("1850000"))
                .cost(new BigDecimal("1300000"))
                .build();
        when(productService.createProduct(any(ProductDto.class))).thenReturn(sampleProductDto);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("WHEY-GOLD-5LBS"));

        verify(productService).createProduct(any(ProductDto.class));
    }

    @Test
    @DisplayName("TC-181: Gọi API PUT /api/products/{id} trả về HTTP 200 OK khi cập nhật sản phẩm thành công")
    void updateProduct_Returns200() throws Exception {
        ProductDto request = ProductDto.builder()
                .name("Whey Gold Cập Nhật")
                .sku("WHEY-GOLD-5LBS")
                .category("SUPPLEMENT")
                .price(new BigDecimal("1700000"))
                .cost(new BigDecimal("1250000"))
                .build();
        when(productService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(sampleProductDto);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("WHEY-GOLD-5LBS"));

        verify(productService).updateProduct(eq(1L), any(ProductDto.class));
    }

    @Test
    @DisplayName("TC-182: Gọi API DELETE /api/products/{id} trả về HTTP 200 OK khi xóa sản phẩm thành công")
    void deleteProduct_Returns200() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(productService).deleteProduct(1L);
    }

    @Test
    @DisplayName("TC-183: Gọi API POST /api/products/{id}/import trả về HTTP 200 OK khi nhập hàng thành công")
    void importStock_Returns200() throws Exception {
        StockImportDto request = StockImportDto.builder().quantity(15).cost(new BigDecimal("1200000")).build();
        when(productService.importStock(eq(1L), any(StockImportDto.class))).thenReturn(sampleProductDto);

        mockMvc.perform(post("/api/products/1/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("WHEY-GOLD-5LBS"));

        verify(productService).importStock(eq(1L), any(StockImportDto.class));
    }

    @Test
    @DisplayName("TC-184: Gọi API POST /api/products/{id}/adjust trả về HTTP 200 OK khi điều chỉnh tồn kho thành công")
    void adjustStock_Returns200() throws Exception {
        StockAdjustmentDto request = StockAdjustmentDto.builder().actualStock(45).reason("Kiểm kê").build();
        when(productService.adjustStock(eq(1L), any(StockAdjustmentDto.class))).thenReturn(sampleProductDto);

        mockMvc.perform(post("/api/products/1/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("WHEY-GOLD-5LBS"));

        verify(productService).adjustStock(eq(1L), any(StockAdjustmentDto.class));
    }

    @Test
    @DisplayName("TC-185: Gọi API GET /api/products/{id} không tìm thấy trả về HTTP 404 NOT FOUND")
    void getProductById_NotFound_Returns404() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: 999"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
