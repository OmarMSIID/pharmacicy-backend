package com.pharmacy.vente.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pharmacy.vente.dto.VenteResponse;
import com.pharmacy.vente.exception.ConflictException;
import com.pharmacy.vente.exception.GlobalExceptionHandler;
import com.pharmacy.vente.exception.ResourceNotFoundException;
import com.pharmacy.vente.service.VenteService;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class VenteControllerTest {

    private static final String VALID_BODY = """
            { "lignes": [ { "medicamentId": 1, "quantite": 2 } ] }
            """;

    private final VenteService service = mock(VenteService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new VenteController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(postVente(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ventes/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.total").value(8.5))
                .andExpect(jsonPath("$.lignes[0].medicamentId").value(1))
                .andExpect(jsonPath("$.avertissement").doesNotExist());
    }

    @Test
    void createRejectsEmptyCart() throws Exception {
        mockMvc.perform(postVente("{ \"lignes\": [] }")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void createRejectsNonPositiveQuantity() throws Exception {
        mockMvc.perform(postVente("{ \"lignes\": [ { \"medicamentId\": 1, \"quantite\": 0 } ] }"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void createMapsConflictTo409() throws Exception {
        when(service.create(any())).thenThrow(new ConflictException("Insufficient stock"));

        mockMvc.perform(postVente(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient stock"));
    }

    @Test
    void createMapsRemoteFailureTo503() throws Exception {
        when(service.create(any())).thenThrow(mock(FeignException.ServiceUnavailable.class));

        mockMvc.perform(postVente(VALID_BODY)).andExpect(status().isServiceUnavailable());
    }

    @Test
    void findByIdReturnsSale() throws Exception {
        when(service.findById(7L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/ventes/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lignes[0].nomCommercial").value("Paracetamol"));
    }

    @Test
    void findByIdMapsMissingSaleTo404() throws Exception {
        when(service.findById(7L)).thenThrow(new ResourceNotFoundException("Vente not found with id: 7"));

        mockMvc.perform(get("/api/ventes/7")).andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postVente(String body) {
        return post("/api/ventes").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private VenteResponse sampleResponse() {
        return new VenteResponse(7L, LocalDateTime.of(2026, 11, 3, 12, 0), null, new BigDecimal("8.50"),
                List.of(new VenteResponse.Ligne(1L, 1L, "Paracetamol", 2, new BigDecimal("8.50"))), null);
    }
}
