package com.example.GYM_management_api.entities;

import com.example.GYM_management_api.entities.enums.CommonStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "stock", nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "min_stock", nullable = false)
    @Builder.Default
    private Integer minStock = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CommonStatus status = CommonStatus.ACTIVE;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public String getImage() {
        return this.imageUrl != null ? this.imageUrl : this.image;
    }

    public void setImage(String image) {
        this.image = image;
        this.imageUrl = image;
    }

    public String getImageUrl() {
        return this.imageUrl != null ? this.imageUrl : this.image;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.image = imageUrl;
    }

    public Integer getStockQuantity() {
        return this.stock;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stock = stockQuantity;
    }
}
