package com.pharmacy.vente.client;

import com.pharmacy.vente.dto.StockDeductionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "stock-service")
public interface StockClient {

    @GetMapping("/api/stocks/disponible/{medicamentId}")
    Integer getAvailableStock(@PathVariable("medicamentId") Long medicamentId);

    @PutMapping("/api/stocks/deduire-multiple")
    void deductStock(@RequestBody StockDeductionRequest request);
}
