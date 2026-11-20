package com.pharmacy.ordonnance.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ordonnances")
public class Ordonnance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_prescripteur", length = 100)
    private String nomPrescripteur;

    @Column(name = "date_prescription", nullable = false)
    private LocalDateTime datePrescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOrdonnance statut;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ordonnance_medicaments", joinColumns = @JoinColumn(name = "ordonnance_id"))
    @Column(name = "medicament_id")
    private List<Long> medicamentIds;

    public Ordonnance() {
    }

    public Ordonnance(
            Long id,
            String nomPrescripteur,
            LocalDateTime datePrescription,
            StatutOrdonnance statut,
            List<Long> medicamentIds) {
        this.id = id;
        this.nomPrescripteur = nomPrescripteur;
        this.datePrescription = datePrescription;
        this.statut = statut;
        this.medicamentIds = medicamentIds;
    }

    public Long getId() {
        return id;
    }

    public String getNomPrescripteur() {
        return nomPrescripteur;
    }

    public void setNomPrescripteur(String nomPrescripteur) {
        this.nomPrescripteur = nomPrescripteur;
    }

    public LocalDateTime getDatePrescription() {
        return datePrescription;
    }

    public void setDatePrescription(LocalDateTime datePrescription) {
        this.datePrescription = datePrescription;
    }

    public StatutOrdonnance getStatut() {
        return statut;
    }

    public void setStatut(StatutOrdonnance statut) {
        this.statut = statut;
    }

    public List<Long> getMedicamentIds() {
        return medicamentIds;
    }

    public void setMedicamentIds(List<Long> medicamentIds) {
        this.medicamentIds = medicamentIds;
    }
}