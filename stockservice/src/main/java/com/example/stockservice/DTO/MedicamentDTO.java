package com.example.stockservice.DTO;

import java.math.BigDecimal;

public class MedicamentDTO {
private Long id;
    private String nomCommercial;
    private String dci;
    private String dosage;
    private String forme;
    private BigDecimal prixUnitaire;

    public MedicamentDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomCommercial() { return nomCommercial; }
    public void setNomCommercial(String nomCommercial) { this.nomCommercial = nomCommercial; }
    public String getDci() { return dci; }
    public void setDci(String dci) { this.dci = dci; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getForme() { return forme; }
    public void setForme(String forme) { this.forme = forme; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
}