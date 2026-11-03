package com.example.stockservice.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.stockservice.DTO.StockDeductBatchRequest;
import com.example.stockservice.DTO.StockDeductRequest;
import com.example.stockservice.DTO.StockWithMedicamentDTO;
import com.example.stockservice.Service.StockService;
import com.example.stockservice.model.StockLot;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/entree")
    public ResponseEntity<StockLot> ajouterLot(@Valid @RequestBody StockLot lot) {
        return new ResponseEntity<>(stockService.saveEntry(lot), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StockLot>> getAll() {
        return ResponseEntity.ok(stockService.getAllStock());
    }

    @GetMapping("/disponible/{medicamentId}")
    public ResponseEntity<Integer> getAvailableStock(@PathVariable Long medicamentId) {
        return ResponseEntity.ok(stockService.getAvailableStockForMedicament(medicamentId));
    }

    @GetMapping("/alertes-peremption")
    public ResponseEntity<List<StockLot>> getAlertes(@RequestParam(defaultValue = "30") int jours) {
        return ResponseEntity.ok(stockService.getExpiringBatches(jours));
    }

    @PutMapping("/deduire")
    public ResponseEntity<String> deduireStock(@Valid @RequestBody StockDeductRequest request) {
        try {
            stockService.deductStock(request.getMedicamentId(), request.getQuantite());
            return ResponseEntity.ok("Stock décrémenté avec succès.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/deduire-multiple")
    public ResponseEntity<String> deduireStockMultiple(@Valid @RequestBody StockDeductBatchRequest request) {
        try {
            stockService.deductStockBatch(request.getLignes());
            return ResponseEntity.ok("Stock décrémenté avec succès.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/details")
    public ResponseEntity<List<StockWithMedicamentDTO>> getAllWithMedicamentDetails() {
        return ResponseEntity.ok(stockService.getAllStockWithDetails());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<StockWithMedicamentDTO> getByIdWithDetails(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getStockByIdWithDetails(id));
    }

}
