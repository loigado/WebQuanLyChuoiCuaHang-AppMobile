package com.branchmanagement.repository;

import com.branchmanagement.entity.ProductRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRecipeRepository extends JpaRepository<ProductRecipe, Integer> {
    List<ProductRecipe> findByProduct_ProductId(Integer productId);
    void deleteByProduct_ProductId(Integer productId);
}
