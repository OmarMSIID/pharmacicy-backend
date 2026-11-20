package com.pharmacy.ordonnance.client;

import com.pharmacy.ordonnance.dto.MedicamentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medicament-service")
public interface MedicamentClient {
    @GetMapping("/api/medicaments/{id}")
    MedicamentDTO getMedicamentById(@PathVariable("id") Long id);
}