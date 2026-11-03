package com.pharmacy.vente.service;

import com.pharmacy.vente.dto.VenteRequest;
import com.pharmacy.vente.dto.VenteResponse;

public interface VenteService {

    VenteResponse create(VenteRequest request);

    VenteResponse findById(Long id);
}
