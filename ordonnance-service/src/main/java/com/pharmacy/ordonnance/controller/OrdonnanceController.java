package com.pharmacy.ordonnance.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/ordonnances")
@RequiredArgsConstructor
public class OrdonnanceController {

    private final Ordonnance ordonnanceService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OrdonnanceRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ordonnanceService.createOrdonnance(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/servir")
    public ResponseEntity<?> servir(@PathVariable Long id) {
        return ResponseEntity.ok(ordonnanceService.marquerCommeServie(id));
    }
}