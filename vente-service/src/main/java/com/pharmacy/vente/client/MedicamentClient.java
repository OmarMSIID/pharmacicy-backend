package com.pharmacy.vente.client;

import com.pharmacy.vente.dto.MedicamentDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medicament-service")
public interface MedicamentClient {

    @GetMapping("/api/medicaments")
    List<MedicamentDto> findAll();

    @GetMapping("/api/medicaments/{id}")
    MedicamentDto findById(@PathVariable("id") Long id);
}
