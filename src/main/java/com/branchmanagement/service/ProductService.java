package com.branchmanagement.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import com.branchmanagement.dto.ProductRequest;
import com.branchmanagement.dto.ProductResponse;

public interface ProductService {
    Page<ProductResponse> getActiveProducts(int page, int size);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Integer id, ProductRequest request);
    void stopSellingProduct(Integer id);
    void importFromExcel(MultipartFile file);

}