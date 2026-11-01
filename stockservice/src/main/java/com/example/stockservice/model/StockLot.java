package com.example.stockservice.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "stocks_lots")
public class StockLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "medicament_id", nullable = false)
    private Long medicamentId;

    @NotBlank
    @Column(name = "numero_lot", nullable = false, length = 50)
    private String numeroLot;

    @NotNull
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @NotNull
    @Column(name = "date_peremption", nullable = false)
    private LocalDate datePeremption;

    @Column(name = "seuil_alerte")
    private Integer seuilAlerte = 10;

    public StockLot() {}

    public StockLot(Long medicamentId, String numeroLot, Integer quantite, LocalDate datePeremption, Integer seuilAlerte) {
        this.medicamentId = medicamentId;
        this.numeroLot = numeroLot;
        this.quantite = quantite;
        this.datePeremption = datePeremption;
        this.seuilAlerte = (seuilAlerte != null) ? seuilAlerte : 10;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMedicamentId() { return medicamentId; }
    public void setMedicamentId(Long medicamentId) { this.medicamentId = medicamentId; }
    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
    public LocalDate getDatePeremption() { return datePeremption; }
    public void setDatePeremption(LocalDate datePeremption) { this.datePeremption = datePeremption; }
    public Integer getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(Integer seuilAlerte) { this.seuilAlerte = seuilAlerte; }
}