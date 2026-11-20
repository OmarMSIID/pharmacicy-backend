package com.pharmacy.ordonnance.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record OrdonnanceRequest(
        String nomPrescripteur,
        @NotEmpty List<@NotNull @Positive Long> medicamentIds) {
}