package com.pharmacy.medicament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fields required to create or replace a medicament")
public record MedicamentRequest(
        @Schema(description = "Commercial name", example = "Paracetamol")
        @NotBlank @Size(max = 150) String nomCommercial,
        @Schema(description = "International nonproprietary name", example = "Paracetamol")
        @Size(max = 150) String dci,
        @Schema(description = "Strength or dosage", example = "500 mg")
        @Size(max = 50) String dosage,
        @Schema(description = "Pharmaceutical form", example = "Tablet")
        @Size(max = 50) String forme,
        @Schema(description = "Unit price; must be zero or greater", example = "4.25")
        @NotNull @PositiveOrZero BigDecimal prixUnitaire,
        @Schema(description = "Whether this medicament requires a prescription", example = "true")
        boolean surOrdonnance) {
}
