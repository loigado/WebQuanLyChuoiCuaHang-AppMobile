package com.branchmanagement.repository;

import com.branchmanagement.entity.Stocktake;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StocktakeRepository extends JpaRepository<Stocktake, Integer> {
    List<Stocktake> findByBranch_BranchIdOrderByCreatedAtDesc(Integer branchId);

    @Query("SELECT d.product.productName, d.systemQuantity, d.actualQuantity, " +
    	       "(d.actualQuantity - d.systemQuantity) " +
    	       "FROM StocktakeDetail d WHERE d.stocktake.stocktakeId = :stocktakeId")
    	List<Object[]> findDiscrepancyReport(@Param("stocktakeId") Integer stocktakeId);
}