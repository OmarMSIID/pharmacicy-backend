package com.pharmacy.ordonnance.dto;
import java.util.List;

import lombok.Builder;
import lombok.Data;



@Data @Builder
public class OrdonnanceRequest {
    private String nomPrescripteur;
    private List<Long> medicamentIds;
}