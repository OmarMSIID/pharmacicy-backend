package com.pharmacy.medicament.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pharmacy.medicament.dto.MedicamentRequest;
import com.pharmacy.medicament.dto.MedicamentResponse;
import com.pharmacy.medicament.service.MedicamentService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MedicamentControllerTest {

    private MedicamentService service;
    private MedicamentController controller;

    @BeforeEach
    void setUp() {
        service = mock(MedicamentService.class);
        controller = new MedicamentController(service);
    }

    @Test
    void findAllReturnsAllMedicaments() {
        List<MedicamentResponse> medicaments = List.of(response(1L, "Paracetamol"));
        when(service.findAll()).thenReturn(medicaments);

        assertThat(controller.findAll()).isEqualTo(medicaments);
        verify(service).findAll();
    }

    @Test
    void findByIdReturnsTheMatchingMedicament() {
        MedicamentResponse medicament = response(1L, "Paracetamol");
        when(service.findById(1L)).thenReturn(medicament);

        assertThat(controller.findById(1L)).isEqualTo(medicament);
        verify(service).findById(1L);
    }

    @Test
    void createReturnsCreatedStatusAndLocation() {
        MedicamentRequest request = request("Paracetamol");
        MedicamentResponse medicament = response(1L, "Paracetamol");
        when(service.create(request)).thenReturn(medicament);

        ResponseEntity<MedicamentResponse> result = controller.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getHeaders().getLocation()).hasToString("/api/medicaments/1");
        assertThat(result.getBody()).isEqualTo(medicament);
        verify(service).create(request);
    }

    @Test
    void updateReturnsTheUpdatedMedicament() {
        MedicamentRequest request = request("Doliprane");
        MedicamentResponse medicament = response(1L, "Doliprane");
        when(service.update(1L, request)).thenReturn(medicament);

        assertThat(controller.update(1L, request)).isEqualTo(medicament);
        verify(service).update(1L, request);
    }

    @Test
    void deleteReturnsNoContentStatus() {
        ResponseEntity<Void> result = controller.delete(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        verify(service).delete(1L);
    }

    private static MedicamentRequest request(String name) {
        return new MedicamentRequest(name, name, "500 mg", "Comprimé", new BigDecimal("4.25"), false);
    }

    private static MedicamentResponse response(Long id, String name) {
        return new MedicamentResponse(
                id, name, name, "500 mg", "Comprimé", new BigDecimal("4.25"), false);
    }
}
