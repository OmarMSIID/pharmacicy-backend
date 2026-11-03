package com.example.stockservice.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockservice.Client.MedicamentClient;
import com.example.stockservice.DTO.MedicamentDTO;
import com.example.stockservice.DTO.StockDeductRequest;
import com.example.stockservice.DTO.StockWithMedicamentDTO;
import com.example.stockservice.Repo.StockLotRepository;
import com.example.stockservice.model.StockLot;

@Service 
public class StockService {
    private final StockLotRepository repository;
    private final MedicamentClient medicamentClient;

    public StockService(StockLotRepository repository, MedicamentClient medicamentClient) {
        this.repository = repository;
        this.medicamentClient = medicamentClient;
    }

    

    public StockLot saveEntry(StockLot lot) {
        return repository.save(lot);
    }

    public List<StockLot> getAllStock() {
        return repository.findAll();
    }

    public List<StockLot> getExpiringBatches(int daysThreshold) {
        LocalDate limitDate = LocalDate.now().plusDays(daysThreshold);
        return repository.findByDatePeremptionLessThanEqualAndQuantiteGreaterThan(limitDate, 0);
    }

    public Integer getAvailableStockForMedicament(Long medicamentId) {
        return repository.getTotalAvailableQuantity(medicamentId, LocalDate.now());
    }

    @Transactional
    public void deductStock(Long medicamentId, int quantityToDeduct) {
        LocalDate today = LocalDate.now();
        List<StockLot> availableLots = repository.findByMedicamentIdAndQuantiteGreaterThanOrderByDatePeremptionAsc(medicamentId, 0);

        int totalAvailable = availableLots.stream()
                .filter(l -> l.getDatePeremption().isAfter(today))
                .mapToInt(StockLot::getQuantite)
                .sum();

        if (totalAvailable < quantityToDeduct) {
            throw new IllegalStateException("Quantité insuffisante en stock pour le médicament ID: " + medicamentId);
        }

        int remaining = quantityToDeduct;
        for (StockLot lot : availableLots) {
            if (lot.getDatePeremption().isBefore(today)) {
                continue; // Skip expired batches
            }

            if (lot.getQuantite() >= remaining) {
                lot.setQuantite(lot.getQuantite() - remaining);
                repository.save(lot);
                remaining = 0;
                break;
            } else {
                remaining -= lot.getQuantite();
                lot.setQuantite(0);
                repository.save(lot);
            }
        }
    }

    // All-or-nothing: if one line cannot be deducted, the whole transaction rolls back
    @Transactional
    public void deductStockBatch(List<StockDeductRequest> lignes) {
        for (StockDeductRequest ligne : lignes) {
            deductStock(ligne.getMedicamentId(), ligne.getQuantite());
        }
    }

    public List<StockWithMedicamentDTO> getAllStockWithDetails() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public StockWithMedicamentDTO getStockByIdWithDetails(Long id) {
        StockLot lot = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lot non trouvé: " + id));
        return mapToDTO(lot);
    }

    private StockWithMedicamentDTO mapToDTO(StockLot lot) {
        StockWithMedicamentDTO dto = new StockWithMedicamentDTO();
        dto.setId(lot.getId());
        dto.setNumeroLot(lot.getNumeroLot());
        dto.setQuantite(lot.getQuantite());
        dto.setDatePeremption(lot.getDatePeremption());
        dto.setSeuilAlerte(lot.getSeuilAlerte());

        try {
            MedicamentDTO med = medicamentClient.getMedicamentById(lot.getMedicamentId());
            dto.setMedicament(med);
        } catch (Exception e) {
            // Fallback if medicament-service is temporarily unavailable
            MedicamentDTO fallback = new MedicamentDTO();
            fallback.setId(lot.getMedicamentId());
            fallback.setNomCommercial("Indisponible");
            dto.setMedicament(fallback);
        }

        return dto;
    }
}
