package com.branchmanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.branchmanagement.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByProductCode(String productCode);
    boolean existsByBarcode(String barcode);
    boolean existsBySku(String sku);
    
    // ✅ Hỗ trợ scan barcode
    java.util.Optional<Product> findByBarcode(String barcode);

    Page<Product> findByStatus(String status, Pageable pageable);
    
    // ✅ Đã sửa: Đếm sản phẩm active có tồn kho thấp trên toàn hệ thống
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'active' AND p.quantity <= p.minStock")
    long countLowStock();

    // Lưu ý: Xóa hàm getAggregatedInventoryData cũ tại đây vì Product không có branch
}