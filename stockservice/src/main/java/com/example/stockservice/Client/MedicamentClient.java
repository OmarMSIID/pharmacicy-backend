package com.example.stockservice.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.stockservice.DTO.MedicamentDTO;

@FeignClient(name = "medicament-service")
public interface MedicamentClient {

    @GetMapping("/api/medicaments/{id}")
    MedicamentDTO getMedicamentById(@PathVariable("id") Long id);
}
