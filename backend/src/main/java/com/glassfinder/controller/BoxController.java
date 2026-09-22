package com.glassfinder.controller;

import com.glassfinder.dto.AddStockRequest;
import com.glassfinder.dto.BoxResponse;
import com.glassfinder.dto.CreateBoxRequest;
import com.glassfinder.dto.SellStockRequest;
import com.glassfinder.dto.StockHistoryResponse;
import com.glassfinder.dto.StockResponse;
import com.glassfinder.service.BoxService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boxes")
public class BoxController {

    private final BoxService boxService;

    public BoxController(BoxService boxService) {
        this.boxService = boxService;
    }

    // ==========================================
    // CREATE BOX
    // ==========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoxResponse createBox(
            @Valid @RequestBody CreateBoxRequest request
    ) {
        return boxService.createBox(request);
    }


    // ==========================================
    // GET ALL BOXES
    // ==========================================

    @GetMapping
    public List<BoxResponse> getAllBoxes() {
        return boxService.getAllBoxes();
    }


    // ==========================================
    // SEARCH
    // ==========================================

    @GetMapping("/search")
    public List<BoxResponse> searchByModel(
            @RequestParam String model
    ) {
        return boxService.searchByModel(model);
    }


    // ==========================================
    // SELL
    // ==========================================

    @PostMapping("/sell")
    public StockResponse sellStock(
            @Valid @RequestBody SellStockRequest request
    ) {
        return boxService.sellStock(request);
    }


    // ==========================================
    // ADD STOCK
    // ==========================================

    @PostMapping("/add")
    public StockResponse addStock(
            @Valid @RequestBody AddStockRequest request
    ) {
        return boxService.addStock(request);
    }


    // ==========================================
    // HISTORY
    // ==========================================

    @GetMapping("/{boxCode}/history")
    public List<StockHistoryResponse> getStockHistory(
            @PathVariable String boxCode
    ) {
        return boxService.getStockHistory(boxCode);
    }
}