package com.branchmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "[Stocktake]")
public class Stocktake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stocktakeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "status")
    private String status = "pending"; 

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "stocktake", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StocktakeDetail> details = new ArrayList<>();

    // --- GETTERS & SETTERS ---
    public Integer getStocktakeId() { return stocktakeId; }
    public void setStocktakeId(Integer stocktakeId) { this.stocktakeId = stocktakeId; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<StocktakeDetail> getDetails() { return details; }
    public void setDetails(List<StocktakeDetail> details) { this.details = details; }
}