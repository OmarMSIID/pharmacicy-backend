package com.pharmacy.vente.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "Sale to record")
public record VenteRequest(
        @Schema(description = "Prescription served by this sale, if any", example = "5", nullable = true)
        Long ordonnanceId,
        @Schema(description = "Cart lines")
        @NotEmpty @Valid
        List<Ligne> lignes) {

    @Schema(description = "One medicament in the cart")
    public record Ligne(
            @Schema(description = "ID of the medicament", example = "1")
            @NotNull
            Long medicamentId,
            @Schema(description = "Quantity sold", example = "2")
            @NotNull @Positive
            Integer quantite) {
    }
}
