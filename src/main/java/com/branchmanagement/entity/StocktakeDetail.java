package com.branchmanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "[StocktakeDetail]")
public class StocktakeDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stocktake_id")
    private Stocktake stocktake;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "system_quantity")
    private BigDecimal systemQuantity; 

    @Column(name = "actual_quantity")
    private BigDecimal actualQuantity; 

    // --- GETTERS & SETTERS ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Stocktake getStocktake() { return stocktake; }
    public void setStocktake(Stocktake stocktake) { this.stocktake = stocktake; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getSystemQuantity() { return systemQuantity; }
    public void setSystemQuantity(BigDecimal systemQuantity) { this.systemQuantity = systemQuantity; }

    public BigDecimal getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(BigDecimal actualQuantity) { this.actualQuantity = actualQuantity; }
}