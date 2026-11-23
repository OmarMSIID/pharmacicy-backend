package com.pharmacy.vente.controller;

import com.pharmacy.vente.dto.VenteRequest;
import com.pharmacy.vente.dto.VenteResponse;
import com.pharmacy.vente.service.VenteService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ventes")
@Tag(name = "Ventes", description = "Record and retrieve sales")
public class VenteController {

    private final VenteService service;

    public VenteController(VenteService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a sale",
            description = "Prices the cart, deducts the stock, saves the sale, and serves the prescription if one is given.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sale created"),
        @ApiResponse(responseCode = "400", description = "Request validation failed"),
        @ApiResponse(responseCode = "404", description = "Medicament or prescription not found"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock or prescription already served"),
        @ApiResponse(responseCode = "503", description = "A required service is unavailable")
    })
    public ResponseEntity<VenteResponse> create(@Valid @RequestBody VenteRequest request) {
        VenteResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/ventes/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List sales", description = "Returns all sales and their billing lines, newest first.")
    @ApiResponse(responseCode = "200", description = "Sales returned successfully")
    public List<VenteResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a sale", description = "Returns a sale and its billing lines.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sale found"),
        @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    public VenteResponse findById(
            @Parameter(description = "ID of the sale", example = "1")
            @PathVariable Long id) {
        return service.findById(id);
    }
}
