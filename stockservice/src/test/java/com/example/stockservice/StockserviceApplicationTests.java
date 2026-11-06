package com.example.stockservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.stockservice.Client.MedicamentClient;
import com.example.stockservice.DTO.StockWithMedicamentDTO;
import com.example.stockservice.Repo.StockLotRepository;
import com.example.stockservice.Service.StockService;
import com.example.stockservice.model.StockLot;

import feign.FeignException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class StockserviceApplicationTests {

	@Mock
    private StockLotRepository repository;

    @Mock
    private MedicamentClient medicamentClient;

    @InjectMocks
    private StockService stockService;

    private Long medId;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        medId = 1L;
        today = LocalDate.now();
    }

    // =========================================================================
    // UT-STK-01 : Enregistrer un nouveau lot valide
    // =========================================================================
    @Test
    @DisplayName("UT-STK-01 : Enregistrement réussi d'une entrée de lot en stock")
    void testSaveEntry_Success() {
        // Arrange
        StockLot lotInput = new StockLot(medId, "LOT-2026-X1", 100, today.plusYears(1), 10);
        StockLot lotSaved = new StockLot(medId, "LOT-2026-X1", 100, today.plusYears(1), 10);
        lotSaved.setId(10L);

        when(repository.save(any(StockLot.class))).thenReturn(lotSaved);

        // Act
        StockLot result = stockService.saveEntry(lotInput);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("LOT-2026-X1", result.getNumeroLot());
        verify(repository, times(1)).save(lotInput);
    }

    // =========================================================================
    // UT-STK-02 : Décrémentation selon l'algorithme FEFO (First Expired, First Out)
    // =========================================================================
    @Test
    @DisplayName("UT-STK-02 : Déduction FEFO priorisant le lot le plus proche de la péremption")
    void testDeductStock_FefoOrdering() {

        StockLot lotA = new StockLot(medId, "LOT-A", 10, today.plusDays(10), 5);
        lotA.setId(1L);

        StockLot lotB = new StockLot(medId, "LOT-B", 20, today.plusDays(30), 5);
        lotB.setId(2L);

        when(repository.findByMedicamentIdAndQuantiteGreaterThanOrderByDatePeremptionAsc(medId, 0))
                .thenReturn(new ArrayList<>(List.of(lotA, lotB)));

        stockService.deductStock(medId, 15);

        assertEquals(0, lotA.getQuantite(), "Le lot expirant le plus tôt doit être totalement épuisé.");
        assertEquals(15, lotB.getQuantite(), "Le lot suivant doit être amputé du reliquat (20 - 5 = 15).");

        ArgumentCaptor<StockLot> captor = ArgumentCaptor.forClass(StockLot.class);
        verify(repository, times(2)).save(captor.capture());

        List<StockLot> savedLots = captor.getAllValues();
        assertEquals(0, savedLots.get(0).getQuantite());
        assertEquals(15, savedLots.get(1).getQuantite());
    }

    // =========================================================================
    // UT-STK-03 : Refus de vente si stock total disponible insuffisant
    // =========================================================================
    @Test
    @DisplayName("UT-STK-03 : Exception levée si la quantité demandée dépasse le stock non périmé")
    void testDeductStock_InsufficientStock() {
        // Arrange : Seulement 5 unités disponibles
        StockLot lotUnique = new StockLot(medId, "LOT-SEUL", 5, today.plusMonths(6), 5);
        when(repository.findByMedicamentIdAndQuantiteGreaterThanOrderByDatePeremptionAsc(medId, 0))
                .thenReturn(new ArrayList<>(List.of(lotUnique)));

        // Act & Assert : Demander 10 unités doit échouer sans altérer la base
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            stockService.deductStock(medId, 10);
        });

        assertTrue(exception.getMessage().contains("Quantité insuffisante"));
        verify(repository, never()).save(any(StockLot.class));
    }

    // =========================================================================
    // UT-STK-04 : Exclusion absolue des lots déjà expirés
    // =========================================================================
    @Test
    @DisplayName("UT-STK-04 : Les lots déjà expirés sont ignorés et exclus du calcul de vente")
    void testDeductStock_IgnoreExpiredBatches() {
        // Arrange
        StockLot lotPerime = new StockLot(medId, "LOT-PERIME", 20, today.minusDays(2), 5);
        StockLot lotValide = new StockLot(medId, "LOT-VALIDE", 5, today.plusDays(20), 5);

        when(repository.findByMedicamentIdAndQuantiteGreaterThanOrderByDatePeremptionAsc(medId, 0))
                .thenReturn(new ArrayList<>(List.of(lotPerime, lotValide)));

        // Act & Assert : Demander 10 unités doit échouer car le lot périmé est ignoré et 5 < 10
        assertThrows(IllegalStateException.class, () -> {
            stockService.deductStock(medId, 10);
        });

        // Vérifier que la quantité du lot périmé n'a pas bougé
        assertEquals(20, lotPerime.getQuantite());
        verify(repository, never()).save(lotPerime);
    }

    // =========================================================================
    // UT-STK-05 : Alertes de péremption imminente
    // =========================================================================
    @Test
    @DisplayName("UT-STK-05 : Filtrage strict des lots périmés ou approchant du terme (<= 30 jours)")
    void testGetExpiringBatches() {
        // Arrange
        LocalDate seuil30Jours = today.plusDays(30);
        StockLot lotCritique = new StockLot(medId, "LOT-CRITIQUE", 10, today.plusDays(5), 5);

        when(repository.findByDatePeremptionLessThanEqualAndQuantiteGreaterThan(seuil30Jours, 0))
                .thenReturn(List.of(lotCritique));

        // Act
        List<StockLot> alertes = stockService.getExpiringBatches(30);

        // Assert
        assertNotNull(alertes);
        assertEquals(1, alertes.size());
        assertEquals("LOT-CRITIQUE", alertes.get(0).getNumeroLot());
        verify(repository, times(1)).findByDatePeremptionLessThanEqualAndQuantiteGreaterThan(seuil30Jours, 0);
    }

    // =========================================================================
    // UT-STK-06 : Résilience Feign et Fallback
    // =========================================================================
    @Test
    @DisplayName("UT-STK-06 : Fallback gracieux si medicament-service est injoignable")
    void testGetStockByIdWithDetails_FallbackOnFeignFailure() {
        // Arrange
        Long lotId = 1L;
        StockLot lot = new StockLot(99L, "LOT-ISOLE", 40, today.plusMonths(4), 10);
        lot.setId(lotId);

        when(repository.findById(lotId)).thenReturn(Optional.of(lot));
        // Simuler une coupure réseau ou indisponibilité du service médicament
        when(medicamentClient.getMedicamentById(99L)).thenThrow(mock(FeignException.class));

        // Act
        StockWithMedicamentDTO dto = stockService.getStockByIdWithDetails(lotId);

        // Assert : Doit retourner le DTO sans lever d'exception 500
        assertNotNull(dto);
        assertEquals(lotId, dto.getId());
        assertEquals("LOT-ISOLE", dto.getNumeroLot());
        assertNotNull(dto.getMedicament(), "L'objet médicament de fallback doit être instancié.");
        assertEquals("Indisponible", dto.getMedicament().getNomCommercial());
    }
}
