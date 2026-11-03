package com.pharmacy.vente.dto;

import java.util.List;

public record StockDeductionRequest(List<Ligne> lignes) {

    public record Ligne(Long medicamentId, Integer quantite) {
    }
}
