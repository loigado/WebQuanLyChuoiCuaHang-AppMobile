package com.branchmanagement.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.ProductRequest;
import com.branchmanagement.dto.ProductResponse;
import com.branchmanagement.service.ProductService;

import jakarta.validation.Valid;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/manager/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ProductResponse> products = productService.getActiveProducts(page, size);
            return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách sản phẩm thành công", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }



    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse created = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Tạo sản phẩm thành công", created));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Integer id,
                                                                      @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse updated = productService.updateProduct(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật sản phẩm thành công", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> stopSellingProduct(@PathVariable Integer id) {
        try {
            productService.stopSellingProduct(id);
            return ResponseEntity.ok(ApiResponse.ok("Đã ngừng kinh doanh sản phẩm", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Void>> importProducts(@RequestParam("file") MultipartFile file) {
        try {
            productService.importFromExcel(file);
            return ResponseEntity.ok(ApiResponse.ok("Import sản phẩm từ Excel thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        }
    }
}