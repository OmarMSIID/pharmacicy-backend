package com.pharmacy.medicament.dto;

import com.pharmacy.medicament.entity.Medicament;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Medicament returned by the service")
public record MedicamentResponse(
        @Schema(description = "Database-generated medicament ID", example = "1")
        Long id,
        @Schema(description = "Commercial name", example = "Paracetamol")
        String nomCommercial,
        @Schema(description = "International nonproprietary name", example = "Paracetamol")
        String dci,
        @Schema(description = "Strength or dosage", example = "500 mg")
        String dosage,
        @Schema(description = "Pharmaceutical form", example = "Tablet")
        String forme,
        @Schema(description = "Unit price", example = "4.25")
        BigDecimal prixUnitaire,
        @Schema(description = "Whether this medicament requires a prescription", example = "true")
        boolean surOrdonnance) {

    public static MedicamentResponse from(Medicament medicament) {
        return new MedicamentResponse(
                medicament.getId(),
                medicament.getNomCommercial(),
                medicament.getDci(),
                medicament.getDosage(),
                medicament.getForme(),
                medicament.getPrixUnitaire(),
                medicament.isSurOrdonnance());
    }
}
