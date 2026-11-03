package com.pharmacy.vente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "ligne_ventes")
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    @Column(name = "medicament_id", nullable = false)
    private Long medicamentId;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixTotal;

    protected LigneVente() {
    }

    public LigneVente(Long medicamentId, Integer quantite, BigDecimal prixTotal) {
        this.medicamentId = medicamentId;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
    }

    void setVente(Vente vente) {
        this.vente = vente;
    }

    public Long getId() {
        return id;
    }

    public Long getMedicamentId() {
        return medicamentId;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public BigDecimal getPrixTotal() {
        return prixTotal;
    }
}
