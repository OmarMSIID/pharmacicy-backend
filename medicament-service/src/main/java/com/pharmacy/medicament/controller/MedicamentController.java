package com.pharmacy.medicament.controller;

import com.pharmacy.medicament.dto.MedicamentRequest;
import com.pharmacy.medicament.dto.MedicamentResponse;
import com.pharmacy.medicament.service.MedicamentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicaments")
@Tag(name = "Medicaments", description = "Create, retrieve, update, and delete medicaments")
public class MedicamentController {

    private final MedicamentService service;

    public MedicamentController(MedicamentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List medicaments", description = "Returns all medicaments.")
    @ApiResponse(responseCode = "200", description = "Medicaments returned successfully")
    public List<MedicamentResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a medicament", description = "Returns a medicament by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Medicament found"),
        @ApiResponse(responseCode = "404", description = "Medicament not found")
    })
    public MedicamentResponse findById(
            @Parameter(description = "ID of the medicament", example = "1")
            @PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a medicament", description = "Creates a medicament and returns its generated ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Medicament created"),
        @ApiResponse(responseCode = "400", description = "Request validation failed")
    })
    public ResponseEntity<MedicamentResponse> create(@Valid @RequestBody MedicamentRequest request) {
        MedicamentResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/medicaments/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a medicament", description = "Replaces the fields of an existing medicament.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Medicament updated"),
        @ApiResponse(responseCode = "400", description = "Request validation failed"),
        @ApiResponse(responseCode = "404", description = "Medicament not found")
    })
    public MedicamentResponse update(
            @Parameter(description = "ID of the medicament", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MedicamentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a medicament", description = "Deletes a medicament by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Medicament deleted"),
        @ApiResponse(responseCode = "404", description = "Medicament not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the medicament", example = "1")
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
