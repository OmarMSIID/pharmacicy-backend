package com.pharmacy.vente.client;

import com.pharmacy.vente.dto.OrdonnanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "ordonnance-service")
public interface OrdonnanceClient {

    @GetMapping("/api/ordonnances/{id}")
    OrdonnanceDto findById(@PathVariable("id") Long id);

    @PutMapping("/api/ordonnances/{id}/servir")
    void serve(@PathVariable("id") Long id);
}
