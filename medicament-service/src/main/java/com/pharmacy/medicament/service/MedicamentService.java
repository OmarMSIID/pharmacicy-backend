package com.pharmacy.medicament.service;

import com.pharmacy.medicament.dto.MedicamentRequest;
import com.pharmacy.medicament.dto.MedicamentResponse;
import java.util.List;

public interface MedicamentService {

    List<MedicamentResponse> findAll();

    MedicamentResponse findById(Long id);

    MedicamentResponse create(MedicamentRequest request);

    MedicamentResponse update(Long id, MedicamentRequest request);

    void delete(Long id);
}
