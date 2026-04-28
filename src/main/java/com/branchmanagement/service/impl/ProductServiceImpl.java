package com.branchmanagement.service.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.branchmanagement.dto.ProductRequest;
import com.branchmanagement.dto.ProductResponse;
import com.branchmanagement.entity.Category;
import com.branchmanagement.entity.Product;
import com.branchmanagement.repository.CategoryRepository;
import com.branchmanagement.repository.ProductRepository;
import com.branchmanagement.service.ProductService;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of Product Service.
 * Handles product lifecycle including creation, updates, and bulk import from Excel.
 * 
 * @author Antigravity AI
 */
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_INACTIVE = "inactive";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<ProductResponse> getActiveProducts(int page, int size) {
        return productRepository.findByStatus(STATUS_ACTIVE, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateUniqueFields(request, null);

        Category category = findCategoryById(request.getCategoryId());
        Product product = new Product();
        mapRequestToEntity(request, product, category);
        
        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        validateUniqueFields(request, id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        Category category = findCategoryById(request.getCategoryId());
        mapRequestToEntity(request, product, category);
        
        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void stopSellingProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        product.setStatus(STATUS_INACTIVE);
        productRepository.save(product);
    }

    /**
     * Imports products from an Excel file.
     * Expects columns: Code, Name, CategoryID, Unit, Price.
     */
    @Override
    @Transactional
    public void importFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File Excel không hợp lệ hoặc trống");
        }

        List<Product> productsToSave = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Product product = parseProductFromRow(row, i + 1);
                productsToSave.add(product);
            }
            productRepository.saveAll(productsToSave);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý file Excel: " + e.getMessage());
        }
    }

    // --- Private Helper Methods ---

    private Category findCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
    }

    private Product parseProductFromRow(Row row, int rowNum) {
        String code = row.getCell(0).getStringCellValue();
        String name = row.getCell(1).getStringCellValue();
        Integer categoryId = (int) row.getCell(2).getNumericCellValue();
        String unit = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : null;
        BigDecimal price = row.getCell(4) != null ? BigDecimal.valueOf(row.getCell(4).getNumericCellValue()) : BigDecimal.ZERO;

        if (productRepository.existsByProductCode(code)) {
            throw new RuntimeException("Mã sản phẩm " + code + " đã tồn tại ở dòng " + rowNum);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục ID " + categoryId + " không tồn tại ở dòng " + rowNum));

        Product product = new Product();
        product.setProductCode(code);
        product.setProductName(name);
        product.setCategory(category);
        product.setUnit(unit);
        product.setPrice(price);
        product.setStatus(STATUS_ACTIVE);
        return product;
    }

    private void validateUniqueFields(ProductRequest request, Integer currentProductId) {
        if (currentProductId == null) {
            if (productRepository.existsByProductCode(request.getProductCode())) {
                throw new RuntimeException("Mã sản phẩm đã tồn tại");
            }
        }
    }

    private void mapRequestToEntity(ProductRequest req, Product entity, Category cat) {
        entity.setCategory(cat);
        entity.setProductCode(req.getProductCode());
        entity.setProductName(req.getProductName());
        entity.setUnit(req.getUnit());
        entity.setPrice(req.getPrice());
        entity.setBarcode(req.getBarcode());
        entity.setSku(req.getSku());
        entity.setImageUrl(req.getImageUrl());
        entity.setMinStock(req.getMinStock() != null ? req.getMinStock() : 0);
        entity.setMaxStock(req.getMaxStock() != null ? req.getMaxStock() : 0);
        entity.setStatus(STATUS_ACTIVE);
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse res = new ProductResponse();
        res.setProductId(p.getProductId());
        res.setProductCode(p.getProductCode());
        res.setProductName(p.getProductName());
        res.setUnit(p.getUnit());
        res.setPrice(p.getPrice());
        res.setBarcode(p.getBarcode());
        res.setSku(p.getSku());
        res.setImageUrl(p.getImageUrl());
        res.setStatus(p.getStatus());
        res.setMinStock(p.getMinStock());
        res.setMaxStock(p.getMaxStock());
        
        if (p.getCategory() != null) {
            res.setCategoryId(p.getCategory().getCategoryId());
            res.setCategoryName(p.getCategory().getCategoryName());
        }
        return res;
    }
}