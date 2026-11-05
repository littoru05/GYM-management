package com.example.GYM_management_api.mappers;

import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.entities.Category;
import com.example.GYM_management_api.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử ProductMapper: xử lý trường imageUrl và placeholder")
class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    @DisplayName("resolveImageUrl trả về ảnh mặc định khi chuỗi null hoặc rỗng")
    void testResolveImageUrl_FallbackPlaceholder() {
        assertEquals(ProductMapper.DEFAULT_PRODUCT_PLACEHOLDER, mapper.resolveImageUrl(null));
        assertEquals(ProductMapper.DEFAULT_PRODUCT_PLACEHOLDER, mapper.resolveImageUrl(""));
        assertEquals(ProductMapper.DEFAULT_PRODUCT_PLACEHOLDER, mapper.resolveImageUrl("   "));
    }

    @Test
    @DisplayName("resolveImageUrl trả về link ảnh hợp lệ đã trim")
    void testResolveImageUrl_ValidUrl() {
        String url = "https://images.unsplash.com/photo-example?w=600";
        assertEquals(url, mapper.resolveImageUrl("  " + url + "  "));
    }

    @Test
    @DisplayName("toEntity từ ProductDto gán placeholder nếu imageUrl null hoặc rỗng")
    void testToEntity_FromProductDto_Placeholder() {
        ProductDto request = ProductDto.builder()
                .name("Nước khoáng Lavie 500ml")
                .sku("WATER-LAVIE-500")
                .price(new BigDecimal("10000"))
                .cost(new BigDecimal("5000"))
                .imageUrl("")
                .build();

        Product product = mapper.toEntity(request, null);
        assertNotNull(product);
        assertEquals(ProductMapper.DEFAULT_PRODUCT_PLACEHOLDER, product.getImageUrl());
        assertEquals(ProductMapper.DEFAULT_PRODUCT_PLACEHOLDER, product.getImage());
    }

    @Test
    @DisplayName("toDto trả về đầy đủ imageUrl")
    void testToDto_ContainsImageUrl() {
        String testUrl = "https://images.unsplash.com/photo-water?w=600";
        Category category = Category.builder().code("DRINK").build();
        category.setId(1L);

        Product product = Product.builder()
                .sku("WATER-01")
                .name("Nước suối")
                .price(new BigDecimal("10000"))
                .cost(new BigDecimal("5000"))
                .imageUrl(testUrl)
                .category(category)
                .build();
        product.setId(1L);

        ProductDto dto = mapper.toDto(product);
        assertNotNull(dto);
        assertEquals("1", dto.getId());
        assertEquals("WATER-01", dto.getSku());
        assertEquals(testUrl, dto.getImageUrl());
    }

    @Test
    @DisplayName("updateEntityFromDto cập nhật imageUrl mới vào entity")
    void testUpdateEntityFromDto() {
        Product product = Product.builder()
                .sku("GLOVE-01")
                .name("Găng tay tập gym")
                .imageUrl(ProductMapper.DEFAULT_PRODUCT_PLACEHOLDER)
                .build();
        product.setId(1L);

        String newUrl = "https://images.unsplash.com/photo-gloves?w=600";
        ProductDto request = ProductDto.builder()
                .name("Găng tay tập gym cao cấp")
                .imageUrl(newUrl)
                .build();

        mapper.updateEntityFromDto(request, product, null);
        assertEquals("Găng tay tập gym cao cấp", product.getName());
        assertEquals(newUrl, product.getImageUrl());
        assertEquals(newUrl, product.getImage());
    }
}

