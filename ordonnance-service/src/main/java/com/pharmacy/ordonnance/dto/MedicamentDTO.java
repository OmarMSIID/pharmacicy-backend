package com.pharmacy.ordonnance.dto;
import lombok.Data;

@Data
public class MedicamentDTO {
    private Long id;
    private String nomCommercial;
    private boolean surOrdonnance; // L'attribut clé venant de medicament-service
}