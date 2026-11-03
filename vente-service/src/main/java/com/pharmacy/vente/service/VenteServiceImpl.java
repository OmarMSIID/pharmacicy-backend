package com.pharmacy.vente.service;

import com.pharmacy.vente.client.MedicamentClient;
import com.pharmacy.vente.client.OrdonnanceClient;
import com.pharmacy.vente.client.StockClient;
import com.pharmacy.vente.dto.MedicamentDto;
import com.pharmacy.vente.dto.OrdonnanceDto;
import com.pharmacy.vente.dto.StockDeductionRequest;
import com.pharmacy.vente.dto.VenteRequest;
import com.pharmacy.vente.dto.VenteResponse;
import com.pharmacy.vente.entity.LigneVente;
import com.pharmacy.vente.entity.Vente;
import com.pharmacy.vente.exception.ConflictException;
import com.pharmacy.vente.exception.ResourceNotFoundException;
import com.pharmacy.vente.repository.VenteRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class VenteServiceImpl implements VenteService {

    private static final Logger log = LoggerFactory.getLogger(VenteServiceImpl.class);
    private static final String STATUT_EN_ATTENTE = "EN_ATTENTE";

    private final VenteRepository repository;
    private final MedicamentClient medicamentClient;
    private final StockClient stockClient;
    private final OrdonnanceClient ordonnanceClient;
    private final TransactionTemplate transactionTemplate;

    public VenteServiceImpl(
            VenteRepository repository,
            MedicamentClient medicamentClient,
            StockClient stockClient,
            OrdonnanceClient ordonnanceClient,
            TransactionTemplate transactionTemplate) {
        this.repository = repository;
        this.medicamentClient = medicamentClient;
        this.stockClient = stockClient;
        this.ordonnanceClient = ordonnanceClient;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public VenteResponse create(VenteRequest request) {
        Long ordonnanceId = request.ordonnanceId();
        if (ordonnanceId != null) {
            checkOrdonnanceIsPending(ordonnanceId);
        }

        Map<Long, Integer> quantites = mergeLignes(request.lignes());
        Map<Long, String> nomsCommerciaux = new HashMap<>();
        Vente vente = new Vente(ordonnanceId);
        quantites.forEach((medicamentId, quantite) -> {
            MedicamentDto medicament = getMedicament(medicamentId);
            checkStockIsAvailable(medicament, quantite);
            nomsCommerciaux.put(medicamentId, medicament.nomCommercial());
            vente.addLigne(new LigneVente(
                    medicamentId,
                    quantite,
                    medicament.prixUnitaire().multiply(BigDecimal.valueOf(quantite))));
        });

        // The sale is written first, so a failed deduction rolls it back and a failed write deducts nothing
        Vente saved = transactionTemplate.execute(status -> {
            Vente persisted = repository.saveAndFlush(vente);
            deductStock(quantites);
            return persisted;
        });

        String avertissement = ordonnanceId != null ? serveOrdonnance(saved.getId(), ordonnanceId) : null;
        return VenteResponse.from(saved, nomsCommerciaux, avertissement);
    }

    @Override
    public VenteResponse findById(Long id) {
        Vente vente = repository.findWithLignesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vente not found with id: " + id));

        Map<Long, String> nomsCommerciaux = new HashMap<>();
        for (LigneVente ligne : vente.getLignes()) {
            nomsCommerciaux.computeIfAbsent(ligne.getMedicamentId(), this::findNomCommercial);
        }
        return VenteResponse.from(vente, nomsCommerciaux, null);
    }

    private void checkOrdonnanceIsPending(Long ordonnanceId) {
        OrdonnanceDto ordonnance;
        try {
            ordonnance = ordonnanceClient.findById(ordonnanceId);
        } catch (FeignException.NotFound exception) {
            throw new ResourceNotFoundException("Ordonnance not found with id: " + ordonnanceId);
        }
        if (!STATUT_EN_ATTENTE.equals(ordonnance.statut())) {
            throw new ConflictException(
                    "Ordonnance " + ordonnanceId + " cannot be served, its status is: " + ordonnance.statut());
        }
    }

    private Map<Long, Integer> mergeLignes(List<VenteRequest.Ligne> lignes) {
        Map<Long, Integer> quantites = new LinkedHashMap<>();
        for (VenteRequest.Ligne ligne : lignes) {
            quantites.merge(ligne.medicamentId(), ligne.quantite(), Integer::sum);
        }
        return quantites;
    }

    private MedicamentDto getMedicament(Long medicamentId) {
        try {
            return medicamentClient.findById(medicamentId);
        } catch (FeignException.NotFound exception) {
            throw new ResourceNotFoundException("Medicament not found with id: " + medicamentId);
        }
    }

    private void checkStockIsAvailable(MedicamentDto medicament, int quantite) {
        int disponible = stockClient.getAvailableStock(medicament.id());
        if (disponible < quantite) {
            throw new ConflictException("Insufficient stock for medicament " + medicament.id()
                    + ": requested " + quantite + ", available " + disponible);
        }
    }

    private void deductStock(Map<Long, Integer> quantites) {
        List<StockDeductionRequest.Ligne> lignes = quantites.entrySet().stream()
                .map(entry -> new StockDeductionRequest.Ligne(entry.getKey(), entry.getValue()))
                .toList();
        try {
            stockClient.deductStock(new StockDeductionRequest(lignes));
        } catch (FeignException.BadRequest exception) {
            // Another sale took the stock after our availability check
            throw new ConflictException("Insufficient stock: " + exception.contentUTF8());
        }
    }

    private String serveOrdonnance(Long venteId, Long ordonnanceId) {
        try {
            ordonnanceClient.serve(ordonnanceId);
            return null;
        } catch (FeignException exception) {
            log.error("Vente {} was saved but ordonnance {} could not be marked as served: {}",
                    venteId, ordonnanceId, exception.getMessage());
            return "Sale recorded, but ordonnance " + ordonnanceId + " could not be marked as served";
        }
    }

    private String findNomCommercial(Long medicamentId) {
        try {
            return medicamentClient.findById(medicamentId).nomCommercial();
        } catch (FeignException exception) {
            log.warn("Could not fetch medicament {}: {}", medicamentId, exception.getMessage());
            return null;
        }
    }
}
