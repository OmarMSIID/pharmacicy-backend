package com.pharmacy.vente.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pharmacy.vente.entity.LigneVente;
import com.pharmacy.vente.entity.Vente;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Sale returned by the service")
public record VenteResponse(
        @Schema(description = "Database-generated sale ID", example = "1")
        Long id,
        @Schema(description = "Date and time of the sale")
        LocalDateTime dateVente,
        @Schema(description = "Prescription served by this sale, if any", example = "5", nullable = true)
        Long ordonnanceId,
        @Schema(description = "Sale total", example = "8.50")
        BigDecimal total,
        @Schema(description = "Billing lines")
        List<Ligne> lignes,
        @Schema(description = "Set when the sale succeeded but the prescription could not be marked as served")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String avertissement) {

    @Schema(description = "One billing line")
    public record Ligne(
            @Schema(description = "Database-generated line ID", example = "1")
            Long id,
            @Schema(description = "ID of the medicament", example = "1")
            Long medicamentId,
            @Schema(description = "Commercial name, empty if medicament-service is unavailable", example = "Paracetamol")
            String nomCommercial,
            @Schema(description = "Quantity sold", example = "2")
            Integer quantite,
            @Schema(description = "Line total", example = "8.50")
            BigDecimal prixTotal) {

        static Ligne from(LigneVente ligne, String nomCommercial) {
            return new Ligne(
                    ligne.getId(),
                    ligne.getMedicamentId(),
                    nomCommercial,
                    ligne.getQuantite(),
                    ligne.getPrixTotal());
        }
    }

    public static VenteResponse from(Vente vente, Map<Long, String> nomsCommerciaux, String avertissement) {
        return new VenteResponse(
                vente.getId(),
                vente.getDateVente(),
                vente.getOrdonnanceId(),
                vente.getTotal(),
                vente.getLignes().stream()
                        .map(ligne -> Ligne.from(ligne, nomsCommerciaux.get(ligne.getMedicamentId())))
                        .toList(),
                avertissement);
    }
}
