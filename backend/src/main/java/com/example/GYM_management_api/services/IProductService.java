package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.dtos.StockAdjustmentDto;
import com.example.GYM_management_api.dtos.StockImportDto;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface IProductService {

    Page<ProductDto> getProducts(int page, int size, String keyword, Long categoryId, String status, Boolean lowStock);

    ProductDto getProductById(Long id);

    ProductDto getProductBySku(String sku);

    ProductDto createProduct(ProductDto dto);

    ProductDto updateProduct(Long id, ProductDto dto);

    void deleteProduct(Long id);

    ProductDto importStock(Long id, StockImportDto dto);

    ProductDto adjustStock(Long id, StockAdjustmentDto dto);

    Page<ProductDto> getLowStockProducts(int page, int size);

    Map<String, Object> getInventorySummary();
}