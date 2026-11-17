package com.pharmacy.medicament.service;

import com.pharmacy.medicament.dto.MedicamentRequest;
import com.pharmacy.medicament.dto.MedicamentResponse;
import com.pharmacy.medicament.entity.Medicament;
import com.pharmacy.medicament.exception.ResourceNotFoundException;
import com.pharmacy.medicament.repository.MedicamentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicamentServiceImpl implements MedicamentService {

    private final MedicamentRepository repository;

    public MedicamentServiceImpl(MedicamentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicamentResponse> findAll() {
        return repository.findAll().stream().map(MedicamentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicamentResponse findById(Long id) {
        return MedicamentResponse.from(getMedicament(id));
    }

    @Override
    public MedicamentResponse create(MedicamentRequest request) {
        Medicament medicament = new Medicament(
                request.nomCommercial(),
                request.dci(),
                request.dosage(),
                request.forme(),
                request.prixUnitaire(),
                request.surOrdonnance());
        return MedicamentResponse.from(repository.save(medicament));
    }

    @Override
    public MedicamentResponse update(Long id, MedicamentRequest request) {
        Medicament medicament = getMedicament(id);
        medicament.setNomCommercial(request.nomCommercial());
        medicament.setDci(request.dci());
        medicament.setDosage(request.dosage());
        medicament.setForme(request.forme());
        medicament.setPrixUnitaire(request.prixUnitaire());
        medicament.setSurOrdonnance(request.surOrdonnance());
        return MedicamentResponse.from(repository.save(medicament));
    }

    @Override
    public void delete(Long id) {
        repository.delete(getMedicament(id));
    }

    private Medicament getMedicament(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicament not found with id: " + id));
    }
}
