package com.example.GYM_management_api.services.impl;

import com.example.GYM_management_api.dtos.ProductDto;
import com.example.GYM_management_api.dtos.StockAdjustmentDto;
import com.example.GYM_management_api.dtos.StockImportDto;
import com.example.GYM_management_api.entities.Category;
import com.example.GYM_management_api.entities.Notification;
import com.example.GYM_management_api.entities.Product;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import com.example.GYM_management_api.entities.enums.NotificationType;
import com.example.GYM_management_api.exceptions.BadRequestException;
import com.example.GYM_management_api.exceptions.DuplicateResourceException;
import com.example.GYM_management_api.exceptions.ResourceNotFoundException;
import com.example.GYM_management_api.mappers.ProductMapper;
import com.example.GYM_management_api.repositories.CategoryRepository;
import com.example.GYM_management_api.repositories.NotificationRepository;
import com.example.GYM_management_api.repositories.ProductRepository;
import com.example.GYM_management_api.repositories.TransactionDetailRepository;
import com.example.GYM_management_api.services.IProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionDetailRepository transactionDetailRepository;
    private final NotificationRepository notificationRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(int page, int size, String keyword, Long categoryId, String status,
            Boolean lowStock) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "id"));

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String search = "%" + keyword.trim().toLowerCase() + "%";
                Predicate skuPredicate = cb.like(cb.lower(root.get("sku")), search);
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), search);
                predicates.add(cb.or(skuPredicate, namePredicate));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (status != null && !status.trim().isEmpty()) {
                try {
                    CommonStatus statusEnum = CommonStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (Boolean.TRUE.equals(lowStock)) {
                predicates.add(cb.le(root.get("stock"), root.get("minStock")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với mã SKU: " + sku));
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto createProduct(ProductDto dto) {
        validateNegativeNumbers(dto.getPrice(), dto.getCost(), dto.getStock(), dto.getMinStock());

        String sku = null;
        if (dto.getSku() != null && !dto.getSku().trim().isEmpty()) {
            sku = dto.getSku().trim().toUpperCase();
            if (productRepository.existsBySku(sku)) {
                throw new DuplicateResourceException("Mã SKU '" + sku + "' đã tồn tại trong hệ thống.");
            }
        }

        Category category = resolveCategory(dto.getCategoryId(), dto.getCategory());

        Product product = productMapper.toEntity(dto, category);
        if (sku != null) {
            product.setSku(sku);
        }

        Product saved = productRepository.save(product);
        log.info("Đã tạo sản phẩm mới: ID={}, SKU={}, Name={}, Stock={}, MinStock={}",
                saved.getId(), saved.getSku(), saved.getName(), saved.getStock(), saved.getMinStock());

        checkAndNotifyLowStock(saved);

        return productMapper.toDto(saved);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        validateNegativeNumbers(dto.getPrice(), dto.getCost(), dto.getStock(), dto.getMinStock());

        if (dto.getSku() != null && !dto.getSku().trim().isEmpty()) {
            String sku = dto.getSku().trim().toUpperCase();
            if (productRepository.existsBySkuAndIdNot(sku, id)) {
                throw new DuplicateResourceException("Mã SKU '" + sku + "' đã được sử dụng bởi sản phẩm khác.");
            }
        }

        Category category = null;
        if (dto.getCategoryId() != null || (dto.getCategory() != null && !dto.getCategory().trim().isEmpty())) {
            category = resolveCategory(dto.getCategoryId(), dto.getCategory());
        }

        productMapper.updateEntityFromDto(dto, product, category);
        Product updated = productRepository.save(product);
        log.info("Đã cập nhật sản phẩm: ID={}, SKU={}, Name={}, Stock={}",
                updated.getId(), updated.getSku(), updated.getName(), updated.getStock());

        checkAndNotifyLowStock(updated);

        return productMapper.toDto(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        if (transactionDetailRepository.existsByProductId(id)) {
            throw new BadRequestException(
                    "Sản phẩm đã phát sinh lịch sử giao dịch bán hàng, không thể xóa khỏi hệ thống. Vui lòng chuyển trạng thái sang INACTIVE.");
        }

        productRepository.delete(product);
        log.info("Đã xóa sản phẩm: ID={}, SKU={}", id, product.getSku());
    }

    @Override
    public ProductDto importStock(Long id, StockImportDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        Integer importQty = dto.getQuantity();
        if (importQty == null || importQty <= 0) {
            throw new BadRequestException("Số lượng nhập kho phải lớn hơn 0.");
        }

        if (dto.getCost() != null) {
            if (dto.getCost().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Giá vốn nhập mới không được là số âm.");
            }
            product.setCost(dto.getCost());
        }

        int previousStock = product.getStock() != null ? product.getStock() : 0;
        int newStock = previousStock + importQty;
        product.setStock(newStock);

        Product saved = productRepository.save(product);
        log.info("Nhập kho sản phẩm ID={}, SKU={}, Số lượng nhập={}, Tồn cũ={}, Tồn mới={}, Ghi chú={}",
                saved.getId(), saved.getSku(), importQty, previousStock, newStock, dto.getNote());

        return productMapper.toDto(saved);
    }

    @Override
    public ProductDto adjustStock(Long id, StockAdjustmentDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        int previousStock = product.getStock() != null ? product.getStock() : 0;
        int newStock;

        if (dto.getActualStock() != null) {
            if (dto.getActualStock() < 0) {
                throw new BadRequestException("Số lượng tồn kho thực tế không được là số âm.");
            }
            newStock = dto.getActualStock();
        } else if (dto.getQuantityChange() != null) {
            newStock = previousStock + dto.getQuantityChange();
            if (newStock < 0) {
                throw new BadRequestException("Số lượng tồn kho sau điều chỉnh không thể nhỏ hơn 0 (Tồn kho hiện tại: "
                        + previousStock + ", điều chỉnh: " + dto.getQuantityChange() + ").");
            }
        } else {
            throw new BadRequestException(
                    "Vui lòng cung cấp số lượng tồn kho thực tế (actualStock) hoặc số lượng thay đổi (quantityChange).");
        }

        product.setStock(newStock);
        Product saved = productRepository.save(product);
        log.info("Điều chỉnh kiểm kê kho sản phẩm ID={}, SKU={}, Tồn cũ={}, Tồn mới={}, Lý do={}",
                saved.getId(), saved.getSku(), previousStock, newStock, dto.getReason());

        checkAndNotifyLowStock(saved);

        return productMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getLowStockProducts(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.ASC, "stock"));
        return productRepository.findLowStockProducts(pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getInventorySummary() {
        long totalProducts = productRepository.count();
        Long totalStock = productRepository.sumTotalStock();
        long lowStockCount = productRepository.countLowStockProducts();
        BigDecimal totalCost = productRepository.sumTotalInventoryCost();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", totalProducts);
        summary.put("totalStock", totalStock != null ? totalStock : 0L);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalInventoryCost", totalCost != null ? totalCost : BigDecimal.ZERO);

        return summary;
    }

    private void validateNegativeNumbers(BigDecimal price, BigDecimal cost, Integer stock, Integer minStock) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Đơn giá bán không được là số âm.");
        }
        if (cost != null && cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Giá vốn không được là số âm.");
        }
        if (stock != null && stock < 0) {
            throw new BadRequestException("Số lượng tồn kho không được là số âm.");
        }
        if (minStock != null && minStock < 0) {
            throw new BadRequestException("Định mức tồn kho tối thiểu (min_stock) không được là số âm.");
        }
    }

    private Category resolveCategory(String categoryIdStr, String categoryNameOrCode) {
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
            try {
                Long catId = Long.parseLong(categoryIdStr.trim());
                return categoryRepository.findById(catId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + catId));
            } catch (NumberFormatException e) {
                throw new BadRequestException("ID danh mục không hợp lệ: " + categoryIdStr);
            }
        }

        if (categoryNameOrCode != null && !categoryNameOrCode.trim().isEmpty()) {
            return categoryRepository.findByCode(categoryNameOrCode.trim().toUpperCase())
                    .or(() -> categoryRepository.findByName(categoryNameOrCode.trim()))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục: " + categoryNameOrCode));
        }

        throw new BadRequestException("Sản phẩm phải thuộc về một danh mục hợp lệ.");
    }

    private void checkAndNotifyLowStock(Product product) {
        if (product.getStock() != null && product.getMinStock() != null
                && product.getStock() <= product.getMinStock()) {
            log.warn("CẢNH BÁO TỒN KHO THẤP: Sản phẩm '{}' (SKU: {}) chỉ còn {} sản phẩm (Ngưỡng tối thiểu: {})",
                    product.getName(), product.getSku(), product.getStock(), product.getMinStock());

            Notification notification = Notification.builder()
                    .title("Cảnh báo tồn kho: " + product.getName())
                    .message("Sản phẩm " + product.getName() + " (SKU: " + product.getSku() + ") chỉ còn "
                            + product.getStock() + " trong kho, chạm ngưỡng cảnh báo tối thiểu ("
                            + product.getMinStock() + "). Vui lòng nhập thêm hàng.")
                    .type(NotificationType.INVENTORY)
                    .targetRole("ADMIN")
                    .readStatus(false)
                    .actionUrl("/admin/products")
                    .build();

            notificationRepository.save(notification);
        }
    }
}
