package com.branchmanagement.component;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.branchmanagement.entity.Stock;
import com.branchmanagement.repository.StockRepository;
import com.branchmanagement.service.NotificationService;

@Component
public class StockAlertTask {

    private static final Logger logger = LoggerFactory.getLogger(StockAlertTask.class);
    
    private final StockRepository stockRepository;
    private final NotificationService notificationService;

    public StockAlertTask(StockRepository stockRepository, NotificationService notificationService) {
        this.stockRepository = stockRepository;
        this.notificationService = notificationService;
    }

    /**
     * Chạy tự động mỗi giờ (Cron: Phút 0, Giây 0 của mỗi giờ)
     * Quét kiểm tra Tồn kho thấp hoặc Tồn kho âm
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkLowStockAlerts() {
        logger.info("Bắt đầu quét tự động cảnh báo tồn kho...");

        // Giả sử: Tồn kho < 10 là mức thấp (Low)
        BigDecimal threshold = new BigDecimal("10");
        List<Stock> lowStocks = stockRepository.findStocksBelowThreshold(threshold);

        for (Stock stock : lowStocks) {
            String type = stock.getQuantity().compareTo(BigDecimal.ZERO) < 0 ? "stock_negative" : "stock_low";
            String priority = "stock_negative".equals(type) ? "Urgent" : "High";
            String title = "stock_negative".equals(type) ? "CẢNH BÁO: TỒN KHO ÂM!" : "Cảnh báo: Tồn kho sắp hết";
            
            // Lấy Manager của chi nhánh (Giả sử Branch có liên kết với User là manager, 
            // nếu không có, bạn có thể truyền thẳng ID của Admin tổng)
            // Tạm thời truyền null nếu chưa map Manager vào Branch, hãy thay bằng User hợp lệ nhé!
            
            /* Uncomment và sửa lại khi bạn xác định được logic lấy Manager:
            notificationService.sendAlert(
                stock.getBranch().getManager(), 
                type, 
                title, 
                "Sản phẩm '" + stock.getProduct().getProductName() + "' tại chi nhánh '" + stock.getBranch().getBranchName() + "' hiện chỉ còn " + stock.getQuantity(), 
                priority, 
                "/manager/stock/inventory"
            );
            */
        }
    }
}