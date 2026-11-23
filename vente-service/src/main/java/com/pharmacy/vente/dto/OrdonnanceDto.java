package com.pharmacy.vente.dto;

import java.util.List;

public record OrdonnanceDto(Long id, String statut, List<Long> medicamentIds) {
}
