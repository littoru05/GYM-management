package com.example.GYM_management_api.services;

import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.dtos.StockAdjustmentDto;
import com.example.GYM_management_api.dtos.StockImportDto;
import com.example.GYM_management_api.entities.Category;
import com.example.GYM_management_api.entities.Notification;
import com.example.GYM_management_api.entities.Product;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.ProductMapper;
import com.example.GYM_management_api.repositories.CategoryRepository;
import com.example.GYM_management_api.repositories.NotificationRepository;
import com.example.GYM_management_api.repositories.ProductRepository;
import com.example.GYM_management_api.repositories.TransactionDetailRepository;
import com.example.GYM_management_api.services.impl.ProductServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, TestReportWatcher.class})
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("Kiểm thử nghiệp vụ ProductServiceImpl (Tầng Service - Quản lý Sản phẩm & Kho hàng)")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionDetailRepository transactionDetailRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category sampleCategory;
    private Product sampleProduct;
    private ProductDto sampleProductDto;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .code("SUPPLEMENT")
                .name("Thực phẩm bổ sung")
                .status(CommonStatus.ACTIVE)
                .build();
        sampleCategory.setId(1L);

        sampleProduct = Product.builder()
                .sku("WHEY-GOLD-5LBS")
                .name("Sữa bột Whey Gold Standard 5lbs")
                .category(sampleCategory)
                .price(new BigDecimal("1650000"))
                .cost(new BigDecimal("1200000"))
                .stock(50)
                .minStock(10)
                .status(CommonStatus.ACTIVE)
                .build();
        sampleProduct.setId(1L);

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
    @DisplayName("TC-141: Lấy danh sách sản phẩm phân trang, tìm kiếm theo từ khóa SKU hoặc Tên")
    void getProducts_WithKeyword_ReturnsPagedProducts() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleProductDto);

        Page<ProductDto> result = productService.getProducts(0, 10, "whey", null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("WHEY-GOLD-5LBS", result.getContent().get(0).getSku());
        verify(productRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-142: Lấy danh sách sản phẩm lọc theo danh mục (categoryId) và trạng thái (status)")
    void getProducts_WithCategoryAndStatus_ReturnsFilteredProducts() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleProductDto);

        Page<ProductDto> result = productService.getProducts(0, 10, null, 1L, "ACTIVE", null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ACTIVE", result.getContent().get(0).getStatus());
        verify(productRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-143: Lấy danh sách sản phẩm lọc theo cảnh báo tồn kho thấp (lowStock = true)")
    void getProducts_WithLowStock_ReturnsLowStockProducts() {
        sampleProduct.setStock(5);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleProductDto);

        Page<ProductDto> result = productService.getProducts(0, 10, null, null, null, true);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TC-144: Lấy chi tiết sản phẩm theo ID thành công khi ID tồn tại")
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleProductDto);

        ProductDto result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("WHEY-GOLD-5LBS", result.getSku());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("TC-145: Báo lỗi ResourceNotFoundException khi lấy chi tiết sản phẩm theo ID không tồn tại")
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                productService.getProductById(999L)
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy sản phẩm với ID: 999"));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("TC-146: Lấy chi tiết sản phẩm theo mã SKU thành công khi SKU tồn tại")
    void getProductBySku_Success() {
        when(productRepository.findBySku("WHEY-GOLD-5LBS")).thenReturn(Optional.of(sampleProduct));
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleProductDto);

        ProductDto result = productService.getProductBySku("whey-gold-5lbs");

        assertNotNull(result);
        assertEquals("WHEY-GOLD-5LBS", result.getSku());
        verify(productRepository, times(1)).findBySku("WHEY-GOLD-5LBS");
    }

    @Test
    @DisplayName("TC-147: Báo lỗi ResourceNotFoundException khi lấy chi tiết sản phẩm theo SKU không tồn tại")
    void getProductBySku_NotFound_ThrowsException() {
        when(productRepository.findBySku("NON-EXIST-SKU")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                productService.getProductBySku("NON-EXIST-SKU")
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy sản phẩm với mã SKU: NON-EXIST-SKU"));
        verify(productRepository, times(1)).findBySku("NON-EXIST-SKU");
    }

    @Test
    @DisplayName("TC-148: Báo lỗi BadRequestException khi tạo sản phẩm có đơn giá, giá vốn hoặc số lượng âm")
    void createProduct_NegativeValues_ThrowsBadRequest() {
        // Giá bán âm
        ProductDto negativePrice = ProductDto.builder().price(new BigDecimal("-100")).build();
        BadRequestException exPrice = assertThrows(BadRequestException.class, () ->
                productService.createProduct(negativePrice)
        );
        assertTrue(exPrice.getMessage().contains("Đơn giá bán không được là số âm."));

        // Giá vốn âm
        ProductDto negativeCost = ProductDto.builder().price(BigDecimal.TEN).cost(new BigDecimal("-50")).build();
        BadRequestException exCost = assertThrows(BadRequestException.class, () ->
                productService.createProduct(negativeCost)
        );
        assertTrue(exCost.getMessage().contains("Giá vốn không được là số âm."));

        // Tồn kho âm
        ProductDto negativeStock = ProductDto.builder().price(BigDecimal.TEN).cost(BigDecimal.ONE).stock(-5).build();
        BadRequestException exStock = assertThrows(BadRequestException.class, () ->
                productService.createProduct(negativeStock)
        );
        assertTrue(exStock.getMessage().contains("Số lượng tồn kho không được là số âm."));

        // Định mức tồn kho tối thiểu âm
        ProductDto negativeMinStock = ProductDto.builder().price(BigDecimal.TEN).cost(BigDecimal.ONE).minStock(-1).build();
        BadRequestException exMinStock = assertThrows(BadRequestException.class, () ->
                productService.createProduct(negativeMinStock)
        );
        assertTrue(exMinStock.getMessage().contains("Định mức tồn kho tối thiểu (min_stock) không được là số âm."));
    }

    @Test
    @DisplayName("TC-149: Báo lỗi DuplicateResourceException khi tạo sản phẩm có mã SKU đã tồn tại")
    void createProduct_DuplicateSku_ThrowsDuplicate() {
        ProductDto request = ProductDto.builder()
                .sku("WHEY-GOLD-5LBS")
                .price(BigDecimal.valueOf(100))
                .cost(BigDecimal.valueOf(50))
                .build();

        when(productRepository.existsBySku("WHEY-GOLD-5LBS")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                productService.createProduct(request)
        );

        assertTrue(ex.getMessage().contains("Mã SKU 'WHEY-GOLD-5LBS' đã tồn tại trong hệ thống."));
        verify(productRepository, times(1)).existsBySku("WHEY-GOLD-5LBS");
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-150: Báo lỗi ResourceNotFoundException khi tạo sản phẩm với danh mục không tồn tại")
    void createProduct_CategoryNotFound_ThrowsException() {
        ProductDto request = ProductDto.builder()
                .sku("NEW-PRODUCT")
                .categoryId("999")
                .price(BigDecimal.valueOf(100))
                .cost(BigDecimal.valueOf(50))
                .build();

        when(productRepository.existsBySku("NEW-PRODUCT")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                productService.createProduct(request)
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy danh mục với ID: 999"));
        verify(categoryRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-151: Tạo mới sản phẩm thành công với dữ liệu hợp lệ và tự động sinh thông báo khi tồn kho chạm mức tối thiểu")
    void createProduct_Success_WithLowStockNotification() {
        ProductDto request = ProductDto.builder()
                .sku("CREATINE-300G")
                .name("Creatine Monohydrate 300g")
                .categoryId("1")
                .price(new BigDecimal("450000"))
                .cost(new BigDecimal("300000"))
                .stock(5)
                .minStock(10)
                .build();

        Product createdEntity = Product.builder()
                .sku("CREATINE-300G")
                .name("Creatine Monohydrate 300g")
                .category(sampleCategory)
                .price(new BigDecimal("450000"))
                .cost(new BigDecimal("300000"))
                .stock(5)
                .minStock(10)
                .status(CommonStatus.ACTIVE)
                .build();
        createdEntity.setId(2L);

        ProductDto createdDto = ProductDto.builder()
                .id("2")
                .sku("CREATINE-300G")
                .name("Creatine Monohydrate 300g")
                .stock(5)
                .minStock(10)
                .build();

        when(productRepository.existsBySku("CREATINE-300G")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(productMapper.toEntity(request, sampleCategory)).thenReturn(createdEntity);
        when(productRepository.save(createdEntity)).thenReturn(createdEntity);
        when(productMapper.toDto(createdEntity)).thenReturn(createdDto);

        ProductDto result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("2", result.getId());
        assertEquals("CREATINE-300G", result.getSku());
        // Do stock (5) <= minStock (10), hệ thống phải tạo 1 thông báo Notification cảnh báo tồn kho
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(productRepository, times(1)).save(createdEntity);
    }

    @Test
    @DisplayName("TC-152: Cập nhật sản phẩm thành công khi dữ liệu hợp lệ")
    void updateProduct_Success() {
        ProductDto updateRequest = ProductDto.builder()
                .sku("WHEY-GOLD-5LBS")
                .name("Sữa bột Whey Gold Standard 5lbs - Tên Mới")
                .price(new BigDecimal("1700000"))
                .cost(new BigDecimal("1250000"))
                .stock(60)
                .minStock(15)
                .categoryId("1")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.existsBySkuAndIdNot("WHEY-GOLD-5LBS", 1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        doAnswer(invocation -> {
            sampleProduct.setName("Sữa bột Whey Gold Standard 5lbs - Tên Mới");
            sampleProduct.setPrice(new BigDecimal("1700000"));
            sampleProduct.setStock(60);
            return null;
        }).when(productMapper).updateEntityFromDto(updateRequest, sampleProduct, sampleCategory);
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        ProductDto updatedDto = ProductDto.builder()
                .id("1")
                .sku("WHEY-GOLD-5LBS")
                .name("Sữa bột Whey Gold Standard 5lbs - Tên Mới")
                .price(new BigDecimal("1700000"))
                .stock(60)
                .build();
        when(productMapper.toDto(sampleProduct)).thenReturn(updatedDto);

        ProductDto result = productService.updateProduct(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Sữa bột Whey Gold Standard 5lbs - Tên Mới", result.getName());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("TC-153: Báo lỗi ResourceNotFoundException khi cập nhật sản phẩm có ID không tồn tại")
    void updateProduct_NotFound_ThrowsException() {
        ProductDto updateRequest = ProductDto.builder().name("Cập nhật").build();
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                productService.updateProduct(999L, updateRequest)
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy sản phẩm với ID: 999"));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-154: Báo lỗi DuplicateResourceException khi cập nhật sản phẩm với SKU trùng sản phẩm khác")
    void updateProduct_DuplicateSku_ThrowsDuplicate() {
        ProductDto updateRequest = ProductDto.builder()
                .sku("EXISTING-SKU-OTHER")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.existsBySkuAndIdNot("EXISTING-SKU-OTHER", 1L)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                productService.updateProduct(1L, updateRequest)
        );

        assertTrue(ex.getMessage().contains("Mã SKU 'EXISTING-SKU-OTHER' đã được sử dụng bởi sản phẩm khác."));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-155: Xóa sản phẩm thành công khi chưa phát sinh lịch sử giao dịch bán hàng")
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(transactionDetailRepository.existsByProductId(1L)).thenReturn(false);
        doNothing().when(productRepository).delete(sampleProduct);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository, times(1)).findById(1L);
        verify(transactionDetailRepository, times(1)).existsByProductId(1L);
        verify(productRepository, times(1)).delete(sampleProduct);
    }

    @Test
    @DisplayName("TC-156: Báo lỗi ResourceNotFoundException khi xóa sản phẩm có ID không tồn tại")
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                productService.deleteProduct(999L)
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy sản phẩm với ID: 999"));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("TC-157: Báo lỗi BadRequestException khi xóa sản phẩm đã phát sinh giao dịch bán hàng")
    void deleteProduct_HasTransactions_ThrowsBadRequest() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(transactionDetailRepository.existsByProductId(1L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                productService.deleteProduct(1L)
        );

        assertTrue(ex.getMessage().contains("Sản phẩm đã phát sinh lịch sử giao dịch bán hàng, không thể xóa khỏi hệ thống."));
        verify(productRepository, times(1)).findById(1L);
        verify(transactionDetailRepository, times(1)).existsByProductId(1L);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("TC-158: Nhập kho (importStock) thành công, cập nhật tồn kho mới và đơn giá vốn")
    void importStock_Success() {
        StockImportDto importDto = StockImportDto.builder()
                .quantity(20)
                .cost(new BigDecimal("1250000"))
                .note("Nhập lô hàng bổ sung")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        ProductDto updatedDto = ProductDto.builder()
                .id("1")
                .sku("WHEY-GOLD-5LBS")
                .stock(70) // 50 + 20
                .cost(new BigDecimal("1250000"))
                .build();
        when(productMapper.toDto(sampleProduct)).thenReturn(updatedDto);

        ProductDto result = productService.importStock(1L, importDto);

        assertNotNull(result);
        assertEquals(70, result.getStock());
        assertEquals(new BigDecimal("1250000"), result.getCost());
        assertEquals(70, sampleProduct.getStock());
        assertEquals(new BigDecimal("1250000"), sampleProduct.getCost());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("TC-159: Báo lỗi BadRequestException khi nhập kho với số lượng nhỏ hơn hoặc bằng 0")
    void importStock_InvalidQuantity_ThrowsBadRequest() {
        StockImportDto zeroQty = StockImportDto.builder().quantity(0).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                productService.importStock(1L, zeroQty)
        );

        assertTrue(ex.getMessage().contains("Số lượng nhập kho phải lớn hơn 0."));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-160: Báo lỗi BadRequestException khi nhập kho với đơn giá vốn âm")
    void importStock_NegativeCost_ThrowsBadRequest() {
        StockImportDto negativeCost = StockImportDto.builder()
                .quantity(10)
                .cost(new BigDecimal("-50000"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                productService.importStock(1L, negativeCost)
        );

        assertTrue(ex.getMessage().contains("Giá vốn nhập mới không được là số âm."));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-161: Báo lỗi ResourceNotFoundException khi nhập kho sản phẩm có ID không tồn tại")
    void importStock_NotFound_ThrowsException() {
        StockImportDto dto = StockImportDto.builder().quantity(10).build();
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                productService.importStock(999L, dto)
        );

        assertTrue(ex.getMessage().contains("Không tìm thấy sản phẩm với ID: 999"));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("TC-162: Điều chỉnh tồn kho (adjustStock) theo số lượng kiểm kê thực tế (actualStock) thành công")
    void adjustStock_WithActualStock_Success() {
        StockAdjustmentDto adjustDto = StockAdjustmentDto.builder()
                .actualStock(45)
                .reason("Kiểm kê định kỳ tháng 10")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        ProductDto updatedDto = ProductDto.builder()
                .id("1")
                .sku("WHEY-GOLD-5LBS")
                .stock(45)
                .build();
        when(productMapper.toDto(sampleProduct)).thenReturn(updatedDto);

        ProductDto result = productService.adjustStock(1L, adjustDto);

        assertNotNull(result);
        assertEquals(45, result.getStock());
        assertEquals(45, sampleProduct.getStock());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("TC-163: Điều chỉnh tồn kho (adjustStock) theo số lượng thay đổi (quantityChange) thành công")
    void adjustStock_WithQuantityChange_Success() {
        StockAdjustmentDto adjustDto = StockAdjustmentDto.builder()
                .quantityChange(-5)
                .reason("Hao hụt rách bao bì")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        ProductDto updatedDto = ProductDto.builder()
                .id("1")
                .sku("WHEY-GOLD-5LBS")
                .stock(45) // 50 - 5
                .build();
        when(productMapper.toDto(sampleProduct)).thenReturn(updatedDto);

        ProductDto result = productService.adjustStock(1L, adjustDto);

        assertNotNull(result);
        assertEquals(45, result.getStock());
        assertEquals(45, sampleProduct.getStock());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("TC-164: Báo lỗi BadRequestException khi điều chỉnh tồn kho làm số lượng tồn kho âm")
    void adjustStock_QuantityChangeNegativeResult_ThrowsBadRequest() {
        sampleProduct.setStock(5);
        StockAdjustmentDto adjustDto = StockAdjustmentDto.builder()
                .quantityChange(-10) // 5 - 10 = -5 < 0
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                productService.adjustStock(1L, adjustDto)
        );

        assertTrue(ex.getMessage().contains("Số lượng tồn kho sau điều chỉnh không thể nhỏ hơn 0"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-165: Báo lỗi BadRequestException khi điều chỉnh kiểm kê thực tế có số lượng âm")
    void adjustStock_NegativeActualStock_ThrowsBadRequest() {
        StockAdjustmentDto adjustDto = StockAdjustmentDto.builder()
                .actualStock(-1)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                productService.adjustStock(1L, adjustDto)
        );

        assertTrue(ex.getMessage().contains("Số lượng tồn kho thực tế không được là số âm."));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-166: Báo lỗi BadRequestException khi điều chỉnh tồn kho nhưng không cung cấp actualStock lẫn quantityChange")
    void adjustStock_NeitherProvided_ThrowsBadRequest() {
        StockAdjustmentDto emptyDto = StockAdjustmentDto.builder().reason("Không có số liệu").build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                productService.adjustStock(1L, emptyDto)
        );

        assertTrue(ex.getMessage().contains("Vui lòng cung cấp số lượng tồn kho thực tế (actualStock) hoặc số lượng thay đổi (quantityChange)."));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-167: Lấy danh sách sản phẩm có tồn kho thấp dưới định mức tối thiểu thành công")
    void getLowStockProducts_Success() {
        sampleProduct.setStock(4);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findLowStockProducts(any(Pageable.class))).thenReturn(page);
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleProductDto);

        Page<ProductDto> result = productService.getLowStockProducts(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findLowStockProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("TC-168: Lấy thống kê tổng quan kho hàng (getInventorySummary) thành công")
    void getInventorySummary_Success() {
        when(productRepository.count()).thenReturn(20L);
        when(productRepository.sumTotalStock()).thenReturn(500L);
        when(productRepository.countLowStockProducts()).thenReturn(3L);
        when(productRepository.sumTotalInventoryCost()).thenReturn(new BigDecimal("150000000"));

        Map<String, Object> summary = productService.getInventorySummary();

        assertNotNull(summary);
        assertEquals(20L, summary.get("totalProducts"));
        assertEquals(500L, summary.get("totalStock"));
        assertEquals(3L, summary.get("lowStockCount"));
        assertEquals(new BigDecimal("150000000"), summary.get("totalInventoryCost"));
        verify(productRepository, times(1)).count();
        verify(productRepository, times(1)).sumTotalStock();
        verify(productRepository, times(1)).countLowStockProducts();
        verify(productRepository, times(1)).sumTotalInventoryCost();
    }
}
