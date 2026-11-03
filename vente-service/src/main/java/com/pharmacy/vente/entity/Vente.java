package com.pharmacy.vente.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventes")
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_vente")
    private LocalDateTime dateVente;

    @Column(name = "ordonnance_id")
    private Long ordonnanceId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneVente> lignes = new ArrayList<>();

    protected Vente() {
    }

    public Vente(Long ordonnanceId) {
        this.ordonnanceId = ordonnanceId;
    }

    @PrePersist
    void onCreate() {
        dateVente = LocalDateTime.now();
    }

    public void addLigne(LigneVente ligne) {
        ligne.setVente(this);
        lignes.add(ligne);
        total = total.add(ligne.getPrixTotal());
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public Long getOrdonnanceId() {
        return ordonnanceId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<LigneVente> getLignes() {
        return lignes;
    }
}
