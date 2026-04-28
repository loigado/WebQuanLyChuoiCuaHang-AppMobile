package com.branchmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.dto.StocktakeRequestDto;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.service.StocktakeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/manager/stocktake")
public class StocktakeController {

    private final StocktakeService stocktakeService;

    public StocktakeController(StocktakeService stocktakeService) {
        this.stocktakeService = stocktakeService;
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<Void>> completeStocktake(
            @Valid @RequestBody StocktakeService.StocktakeRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            stocktakeService.createAndComplete(request, userDetails != null ? userDetails.getUserId() : null);
            return ResponseEntity.ok(ApiResponse.ok("Cân bằng kho thành công", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }
}
