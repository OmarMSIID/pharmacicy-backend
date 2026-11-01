package com.example.stockservice.Repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.stockservice.DTO.StockProjection;
import com.example.stockservice.model.StockLot;


@Repository 
public interface StockLotRepository extends JpaRepository<StockLot, Long> {
    List<StockLot> findByMedicamentIdAndQuantiteGreaterThanOrderByDatePeremptionAsc(Long medicamentId,
            Integer minQuantity);

    // Fetch batches expiring on or before a given date with available units
    List<StockLot> findByDatePeremptionLessThanEqualAndQuantiteGreaterThan(LocalDate thresholdDate,
            Integer minQuantity);

    // Sum all quantities available for a specific medicine
    @Query("SELECT COALESCE(SUM(s.quantite), 0) FROM StockLot s WHERE s.medicamentId = :medId AND s.datePeremption > :today")
    Integer getTotalAvailableQuantity(@Param("medId") Long medicamentId, @Param("today") LocalDate today);

    @Query(value = """
            SELECT s.id AS id, s.numero_lot AS numeroLot, s.quantite AS quantite,
                   s.date_peremption AS datePeremption, m.nom_commercial AS nomCommercial,
                   m.dci AS dci, m.prix_unitaire AS prixUnitaire
            FROM stocks_lots s
            INNER JOIN medicaments m ON s.medicament_id = m.id
            """, nativeQuery = true)
    List<StockProjection> findAllWithMedicamentInfo();

}
