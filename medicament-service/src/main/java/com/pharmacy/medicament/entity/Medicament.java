package com.pharmacy.medicament.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "medicaments")
public class Medicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_commercial", nullable = false, length = 150)
    private String nomCommercial;

    @Column(length = 150)
    private String dci;

    @Column(length = 50)
    private String dosage;

    @Column(length = 50)
    private String forme;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "sur_ordonnance", nullable = false)
    private boolean surOrdonnance;

    protected Medicament() {
    }

    public Medicament(
            String nomCommercial,
            String dci,
            String dosage,
            String forme,
            BigDecimal prixUnitaire,
            boolean surOrdonnance) {
        this.nomCommercial = nomCommercial;
        this.dci = dci;
        this.dosage = dosage;
        this.forme = forme;
        this.prixUnitaire = prixUnitaire;
        this.surOrdonnance = surOrdonnance;
    }

    public Long getId() {
        return id;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
    }

    public String getDci() {
        return dci;
    }

    public void setDci(String dci) {
        this.dci = dci;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getForme() {
        return forme;
    }

    public void setForme(String forme) {
        this.forme = forme;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public boolean isSurOrdonnance() {
        return surOrdonnance;
    }

    public void setSurOrdonnance(boolean surOrdonnance) {
        this.surOrdonnance = surOrdonnance;
    }
}
