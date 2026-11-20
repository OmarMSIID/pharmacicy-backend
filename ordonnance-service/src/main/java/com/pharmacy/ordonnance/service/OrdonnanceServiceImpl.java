package com.pharmacy.ordonnance.service;

import com.pharmacy.ordonnance.client.MedicamentClient;
import com.pharmacy.ordonnance.dto.MedicamentDTO;
import com.pharmacy.ordonnance.dto.OrdonnanceRequest;
import com.pharmacy.ordonnance.model.Ordonnance;
import com.pharmacy.ordonnance.model.StatutOrdonnance;
import com.pharmacy.ordonnance.repository.OrdonnanceRepository;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrdonnanceServiceImpl {

    private final OrdonnanceRepository repository;
    private final MedicamentClient medicamentClient;

    public OrdonnanceServiceImpl(OrdonnanceRepository repository, MedicamentClient medicamentClient) {
        this.repository = repository;
        this.medicamentClient = medicamentClient;
    }

    @Transactional
    public Ordonnance createOrdonnance(OrdonnanceRequest request) {
        boolean necessitePrescripteur = false;

        for (Long medId : request.medicamentIds()) {
            MedicamentDTO med = medicamentClient.getMedicamentById(medId);
            if (med != null && med.surOrdonnance()) {
                necessitePrescripteur = true;
                break;
            }
        }

        if (necessitePrescripteur
                && (request.nomPrescripteur() == null || request.nomPrescripteur().trim().isEmpty())) {
            throw new IllegalArgumentException(
                    "Nom du prescripteur obligatoire : au moins un médicament nécessite une ordonnance.");
        }

        Ordonnance ordonnance = new Ordonnance(
                null,
                request.nomPrescripteur(),
                LocalDateTime.now(),
                StatutOrdonnance.EN_ATTENTE,
                request.medicamentIds());

        return repository.save(ordonnance);
    }

    @Transactional(readOnly = true)
    public Ordonnance findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ordonnance introuvable : " + id));
    }

    @Transactional
    public Ordonnance marquerCommeServie(Long id) {
        Ordonnance ord = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ordonnance introuvable : " + id));
        ord.setStatut(StatutOrdonnance.SERVIE);
        return repository.save(ord);
    }
}