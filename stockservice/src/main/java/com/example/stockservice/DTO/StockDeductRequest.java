package com.example.stockservice.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StockDeductRequest {
    @NotNull
    private Long medicamentId;

    @NotNull
    @Positive
    private Integer quantite;

    public StockDeductRequest() {}
    public StockDeductRequest(Long medicamentId, Integer quantite) {
        this.medicamentId = medicamentId;
        this.quantite = quantite;
    }

    public Long getMedicamentId() { return medicamentId; }
    public void setMedicamentId(Long medicamentId) { this.medicamentId = medicamentId; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
}
