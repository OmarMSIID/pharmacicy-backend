package com.pharmacy.ordonnance;

import com.pharmacy.ordonnance.client.MedicamentClient;
import com.pharmacy.ordonnance.dto.MedicamentDTO;
import com.pharmacy.ordonnance.dto.OrdonnanceRequest;
import com.pharmacy.ordonnance.model.Ordonnance;
import com.pharmacy.ordonnance.model.StatutOrdonnance;
import com.pharmacy.ordonnance.repository.OrdonnanceRepository;
import com.pharmacy.ordonnance.service.OrdonnanceServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OrdonnanceServiceImplTest {

    @Mock 
    private OrdonnanceRepository repository;

    @Mock
    private MedicamentClient medicamentClient;

    @InjectMocks
    private OrdonnanceServiceImpl ordonnanceService;

    private MedicamentDTO medSansOrdonnance;
    private MedicamentDTO medAvecOrdonnance;

    @BeforeEach
    void setUp() {
        medSansOrdonnance = new MedicamentDTO(1L, "Sans ordonnance", false);
        medAvecOrdonnance = new MedicamentDTO(2L, "Avec ordonnance", true);
    }

    // UT-ORD-01
    @Test
    void testCreateOrdonnance_Reussie_AvecPrescripteur() {
        // Arrange
        OrdonnanceRequest request = new OrdonnanceRequest("Dr. Chraibi", List.of(2L));

        when(medicamentClient.getMedicamentById(2L)).thenReturn(medAvecOrdonnance);
        when(repository.save(any(Ordonnance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Ordonnance result = ordonnanceService.createOrdonnance(request);

        // Assert
        assertNotNull(result);
        assertEquals("Dr. Chraibi", result.getNomPrescripteur());
        assertEquals(StatutOrdonnance.EN_ATTENTE, result.getStatut());
        verify(repository, times(1)).save(any(Ordonnance.class));
    }

    // UT-ORD-02
    @Test
    void testCreateOrdonnance_Echec_SansPrescripteur_PourMedocStrict() {
        // Arrange
        OrdonnanceRequest request = new OrdonnanceRequest(null, List.of(2L));

        when(medicamentClient.getMedicamentById(2L)).thenReturn(medAvecOrdonnance);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ordonnanceService.createOrdonnance(request);
        });

        assertTrue(exception.getMessage().contains("Nom du prescripteur obligatoire"));
        verify(repository, never()).save(any(Ordonnance.class)); // Vérifie qu'on n'a rien sauvegardé
    }

    // UT-ORD-03
    @Test
    void testCreateOrdonnance_Reussie_SansPrescripteur_MedocLibre() {
        // Arrange
        OrdonnanceRequest request = new OrdonnanceRequest(null, List.of(1L));

        when(medicamentClient.getMedicamentById(1L)).thenReturn(medSansOrdonnance);
        when(repository.save(any(Ordonnance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Ordonnance result = ordonnanceService.createOrdonnance(request);

        // Assert
        assertNotNull(result);
        assertNull(result.getNomPrescripteur());
        verify(repository, times(1)).save(any(Ordonnance.class));
    }

    @Test
    void findByIdReturnsTheOrdonnanceUsedByVenteService() {
        Ordonnance ordonnance = new Ordonnance(
                10L, null, null, StatutOrdonnance.EN_ATTENTE, List.of());
        when(repository.findById(10L)).thenReturn(Optional.of(ordonnance));

        assertEquals(ordonnance, ordonnanceService.findById(10L));
    }

    @Test
    void findAllReturnsTheNewestOrdonnancesFirst() {
        Ordonnance recente = new Ordonnance(2L, null, null, StatutOrdonnance.EN_ATTENTE, List.of());
        Ordonnance ancienne = new Ordonnance(1L, null, null, StatutOrdonnance.SERVIE, List.of());
        when(repository.findAllByOrderByDatePrescriptionDescIdDesc()).thenReturn(List.of(recente, ancienne));

        assertEquals(List.of(recente, ancienne), ordonnanceService.findAll());
    }

    @Test
    void findByIdReturnsNotFoundWhenOrdonnanceDoesNotExist() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> ordonnanceService.findById(10L));
    }
}