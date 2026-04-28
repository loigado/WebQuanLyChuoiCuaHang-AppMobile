package com.branchmanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "[ProductRecipe]")
public class ProductRecipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // Thành phẩm (ví dụ: Ly Cafe)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Product ingredient; // Nguyên liệu (ví dụ: Bột cafe)

    @Column(name = "quantity", precision = 12, scale = 4)
    private BigDecimal quantity; // Lượng tiêu hao cho 1 đơn vị thành phẩm

    // --- GETTERS & SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Product getIngredient() { return ingredient; }
    public void setIngredient(Product ingredient) { this.ingredient = ingredient; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
