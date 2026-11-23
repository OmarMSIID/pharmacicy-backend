package com.pharmacy.vente.service;

import com.pharmacy.vente.dto.VenteRequest;
import com.pharmacy.vente.dto.VenteResponse;
import java.util.List;

public interface VenteService {

    VenteResponse create(VenteRequest request);

    VenteResponse findById(Long id);

    List<VenteResponse> findAll();
}
