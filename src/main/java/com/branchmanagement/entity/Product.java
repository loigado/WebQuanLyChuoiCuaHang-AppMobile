package com.branchmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat; // ✅ Thêm import
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.*;

@Entity
@Table(name = "[Product]")
@SQLDelete(sql = "UPDATE [Product] SET is_deleted = 1 WHERE product_id = ?")
@SQLRestriction("is_deleted = 0")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, columnDefinition = "NVARCHAR(200)")
    private String productName;

    @Column(name = "unit", columnDefinition = "NVARCHAR(50)")
    private String unit;

    @Column(name = "price", precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // ✅ Sửa lỗi LocalDateTime
    private LocalDateTime createdAt;
    
    @Column(name = "quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantity = 0;
    
    @Column(name = "min_stock")
    private Integer minStock = 0;

    @Column(name = "max_stock")
    private Integer maxStock = 0;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Product() {}

    // Getters & Setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getQuantity() { return quantity != null ? quantity : 0; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }
    public Integer getMaxStock() { return maxStock; }
    public void setMaxStock(Integer maxStock) { this.maxStock = maxStock; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}