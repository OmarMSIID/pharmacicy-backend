package com.pharmacy.ordonnance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.ordonnance.dto.OrdonnanceRequest;
import com.pharmacy.ordonnance.model.Ordonnance;
import com.pharmacy.ordonnance.service.OrdonnanceServiceImpl;

@RestController
@RequestMapping("/api/ordonnances")
public class OrdonnanceController {

    private final OrdonnanceServiceImpl ordonnanceService;

    public OrdonnanceController(OrdonnanceServiceImpl ordonnanceService) {
        this.ordonnanceService = ordonnanceService;
    }

    @PostMapping
    public ResponseEntity<Ordonnance> create(@Valid @RequestBody OrdonnanceRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ordonnanceService.createOrdonnance(request));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping
    public ResponseEntity<List<Ordonnance>> findAll() {
        return ResponseEntity.ok(ordonnanceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ordonnance> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ordonnanceService.findById(id));
    }

    @PutMapping("/{id}/servir")
    public ResponseEntity<Ordonnance> servir(@PathVariable Long id) {
        return ResponseEntity.ok(ordonnanceService.marquerCommeServie(id));
    }
}