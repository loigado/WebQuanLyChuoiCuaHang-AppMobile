package com.branchmanagement.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.branchmanagement.dto.InventoryValueDto;
import com.branchmanagement.dto.StockMovementDto;
import com.branchmanagement.dto.WorkflowStatsDto;
import com.branchmanagement.entity.Product;
import com.branchmanagement.repository.ProductRepository;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.repository.StockTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
public class InventoryReportServiceImpl {

    private final StockRepository stockRepository;
    private final StockTransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    // Constructor Injection (Đã thêm ProductRepository)
    public InventoryReportServiceImpl(StockRepository stockRepository, 
                                     StockTransactionRepository transactionRepository,
                                     ProductRepository productRepository) {
        this.stockRepository = stockRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
    }

    /**
     * Wrapper for legacy export call
     */
    public ByteArrayInputStream exportCurrentInventoryExcel() throws IOException {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);
        return exportComprehensiveReport(start, end);
    }

    /**
     * LOGIC 1: LẤY THỐNG KÊ TRẠNG THÁI PHIÊU (WORKFLOW)
     */
    public WorkflowStatsDto getWorkflowStats() {
        WorkflowStatsDto stats = new WorkflowStatsDto();
        stats.setTotalDraft(transactionRepository.countByStatus("draft"));
        stats.setTotalPending(transactionRepository.countByStatus("pending"));
        stats.setTotalApproved(transactionRepository.countByStatus("approved"));
        stats.setTotalRejected(transactionRepository.countByStatus("rejected"));
        
        long totalProcessed = stats.getTotalApproved() + stats.getTotalRejected();
        if (totalProcessed > 0) {
            stats.setApprovalRate((double) stats.getTotalApproved() / totalProcessed * 100);
        }
        
        Double avgTime = transactionRepository.getAverageProcessingTimeInHours();
        stats.setAvgProcessingHours(avgTime != null ? avgTime : 0.0);
        
        return stats;
    }

    /**
     * LOGIC 2: XUẤT EXCEL BÁO CÁO TỔNG HỢP (ĐA SHEET)
     * Sheet 1: Giá trị tồn kho theo danh mục
     * Sheet 2: Biến động kho trong kỳ
     */
    public ByteArrayInputStream exportComprehensiveReport(LocalDateTime start, LocalDateTime end) throws IOException {
        List<InventoryValueDto> inventoryData = stockRepository.getInventoryValueByCategory();
        List<StockMovementDto> movementData = getStockMovementReport(start, end);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // --- SHEET 1: GIÁ TRỊ TỒN KHO ---
            Sheet sheet1 = workbook.createSheet("Giá trị tồn kho");
            Row headerRow1 = sheet1.createRow(0);
            String[] columns1 = {"Danh mục", "Tổng số lượng", "Tổng giá trị (VND)"};
            
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < columns1.length; i++) {
                Cell cell = headerRow1.createCell(i);
                cell.setCellValue(columns1[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (InventoryValueDto item : inventoryData) {
                Row row = sheet1.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getCategoryName());
                row.createCell(1).setCellValue(item.getTotalQuantity().doubleValue());
                row.createCell(2).setCellValue(item.getTotalValue().doubleValue());
            }
            for (int i = 0; i < columns1.length; i++) sheet1.autoSizeColumn(i);

            // --- SHEET 2: BIẾN ĐỘNG KHO ---
            Sheet sheet2 = workbook.createSheet("Biến động kho");
            Row headerRow2 = sheet2.createRow(0);
            String[] columns2 = {"Sản phẩm", "Tồn đầu kỳ", "Tổng nhập", "Tổng xuất", "Điều chuyển", "Tồn cuối kỳ"};
            
            for (int i = 0; i < columns2.length; i++) {
                Cell cell = headerRow2.createCell(i);
                cell.setCellValue(columns2[i]);
                cell.setCellStyle(headerStyle);
            }

            rowIdx = 1;
            for (StockMovementDto item : movementData) {
                Row row = sheet2.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getProductName());
                row.createCell(1).setCellValue(item.getOpeningStock().doubleValue());
                row.createCell(2).setCellValue(item.getTotalImport().doubleValue());
                row.createCell(3).setCellValue(item.getTotalExport().doubleValue());
                row.createCell(4).setCellValue(item.getTotalTransfer().doubleValue());
                row.createCell(5).setCellValue(item.getClosingStock().doubleValue());
            }
            for (int i = 0; i < columns2.length; i++) sheet2.autoSizeColumn(i);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * LOGIC 3: BÁO CÁO BIẾN ĐỘNG KHO (SỐ DƯ ĐẦU - NHẬP - XUẤT - SỐ DƯ CUỐI)
     */
    public List<StockMovementDto> getStockMovementReport(LocalDateTime start, LocalDateTime end) {
        List<Product> products = productRepository.findAll();
        
        return products.stream().map(product -> {
            Integer pId = product.getProductId();
            
            // 1. Tổng biến động TRONG KỲ [start -> end]
            BigDecimal imports = nullToZero(transactionRepository.sumQuantityByTypeAndDate(pId, "import", start, end));
            BigDecimal exports = nullToZero(transactionRepository.sumQuantityByTypeAndDate(pId, "export", start, end));
            BigDecimal adj = nullToZero(transactionRepository.sumQuantityByTypeAndDate(pId, "adjustment", start, end));
            BigDecimal transfers = nullToZero(transactionRepository.sumQuantityByTypeAndDate(pId, "transfer", start, end));

            // 2. Tính TỒN CUỐI KỲ (Tại thời điểm 'end')
            // Công thức: Tồn hiện tại - (Biến động sau ngày 'end' đến nay)
            BigDecimal currentStock = nullToZero(stockRepository.getTotalQuantityByProduct(pId));
            BigDecimal netAfter = nullToZero(transactionRepository.sumNetChangeAfter(pId, end));
            BigDecimal closingStock = currentStock.subtract(netAfter);
            
            // 3. Tính TỒN ĐẦU KỲ (Tại thời điểm 'start')
            // Công thức: Tồn cuối kỳ - (Biến động trong kỳ)
            // Biến động thuần trong kỳ = Nhập - Xuất + Điều chỉnh
            BigDecimal netInPeriod = imports.subtract(exports).add(adj);
            BigDecimal openingStock = closingStock.subtract(netInPeriod);

            // Gán vào DTO
            StockMovementDto dto = new StockMovementDto();
            dto.setProductName(product.getProductName());
            dto.setOpeningStock(openingStock);
            dto.setTotalImport(imports);
            dto.setTotalExport(exports);
            dto.setTotalTransfer(transfers);
            dto.setClosingStock(closingStock);
            
            return dto;
        }).collect(Collectors.toList());
    }

    // Helper xử lý null
    private BigDecimal nullToZero(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}