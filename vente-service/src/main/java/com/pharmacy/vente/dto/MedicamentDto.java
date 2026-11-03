package com.pharmacy.vente.dto;

import java.math.BigDecimal;

public record MedicamentDto(Long id, String nomCommercial, BigDecimal prixUnitaire) {
}
