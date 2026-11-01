package com.example.stockservice.DTO;

import java.time.LocalDate;

public class StockWithMedicamentDTO {
    private Long id;
    private String numeroLot;
    private Integer quantite;
    private LocalDate datePeremption;
    private Integer seuilAlerte;
    private MedicamentDTO medicament;

    public StockWithMedicamentDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
    public LocalDate getDatePeremption() { return datePeremption; }
    public void setDatePeremption(LocalDate datePeremption) { this.datePeremption = datePeremption; }
    public Integer getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(Integer seuilAlerte) { this.seuilAlerte = seuilAlerte; }
    public MedicamentDTO getMedicament() { return medicament; }
    public void setMedicament(MedicamentDTO medicament) { this.medicament = medicament; }
}
