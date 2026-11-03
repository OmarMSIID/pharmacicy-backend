package com.example.stockservice.DTO;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class StockDeductBatchRequest {
    @NotEmpty
    @Valid
    private List<StockDeductRequest> lignes;

    public StockDeductBatchRequest() {}
    public StockDeductBatchRequest(List<StockDeductRequest> lignes) {
        this.lignes = lignes;
    }

    public List<StockDeductRequest> getLignes() { return lignes; }
    public void setLignes(List<StockDeductRequest> lignes) { this.lignes = lignes; }
}
