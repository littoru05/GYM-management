package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.stock <= p.minStock")
    Page<Product> findLowStockProducts(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock <= p.minStock")
    long countLowStockProducts();

    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM Product p")
    Long sumTotalStock();

    @Query("SELECT COALESCE(SUM(p.stock * p.cost), 0) FROM Product p")
    BigDecimal sumTotalInventoryCost();
}
