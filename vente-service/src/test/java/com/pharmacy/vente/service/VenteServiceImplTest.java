package com.pharmacy.vente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.pharmacy.vente.client.MedicamentClient;
import com.pharmacy.vente.client.OrdonnanceClient;
import com.pharmacy.vente.client.StockClient;
import com.pharmacy.vente.dto.MedicamentDto;
import com.pharmacy.vente.dto.OrdonnanceDto;
import com.pharmacy.vente.dto.StockDeductionRequest;
import com.pharmacy.vente.dto.VenteRequest;
import com.pharmacy.vente.dto.VenteResponse;
import com.pharmacy.vente.entity.LigneVente;
import com.pharmacy.vente.entity.Vente;
import com.pharmacy.vente.exception.ConflictException;
import com.pharmacy.vente.exception.ResourceNotFoundException;
import com.pharmacy.vente.repository.VenteRepository;
import feign.FeignException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

class VenteServiceImplTest {

    private final VenteRepository repository = mock(VenteRepository.class);
    private final MedicamentClient medicamentClient = mock(MedicamentClient.class);
    private final StockClient stockClient = mock(StockClient.class);
    private final OrdonnanceClient ordonnanceClient = mock(OrdonnanceClient.class);
    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
    private final SimpleTransactionStatus transaction = new SimpleTransactionStatus();

    private VenteServiceImpl service;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(transaction);
        when(repository.saveAndFlush(any(Vente.class))).thenAnswer(call -> call.getArgument(0));
        when(medicamentClient.findById(1L)).thenReturn(new MedicamentDto(1L, "Paracetamol", new BigDecimal("4.25")));
        when(medicamentClient.findById(2L)).thenReturn(new MedicamentDto(2L, "Ibuprofene", new BigDecimal("10.00")));
        when(stockClient.getAvailableStock(any())).thenReturn(100);
        service = new VenteServiceImpl(repository, medicamentClient, stockClient, ordonnanceClient,
                new TransactionTemplate(transactionManager));
    }

    @Test
    void createPricesLinesAndDeductsStock() {
        VenteResponse response = service.create(request(null, ligne(1L, 2), ligne(2L, 1)));

        assertThat(response.total()).isEqualByComparingTo("18.50");
        assertThat(response.lignes()).hasSize(2);
        assertThat(response.lignes().get(0).prixTotal()).isEqualByComparingTo("8.50");
        assertThat(response.lignes().get(0).nomCommercial()).isEqualTo("Paracetamol");
        assertThat(response.avertissement()).isNull();
        verify(stockClient).deductStock(new StockDeductionRequest(List.of(
                new StockDeductionRequest.Ligne(1L, 2),
                new StockDeductionRequest.Ligne(2L, 1))));
        verify(transactionManager).commit(transaction);
        verifyNoInteractions(ordonnanceClient);
    }

    @Test
    void createMergesLinesOfTheSameMedicamentBeforeCheckingStock() {
        when(stockClient.getAvailableStock(1L)).thenReturn(5);

        assertThatThrownBy(() -> service.create(request(null, ligne(1L, 3), ligne(1L, 3))))
                .isInstanceOf(ConflictException.class);

        VenteResponse response = service.create(request(null, ligne(1L, 2), ligne(1L, 3)));
        assertThat(response.lignes()).hasSize(1);
        assertThat(response.lignes().get(0).quantite()).isEqualTo(5);
    }

    @Test
    void createRejectsUnknownMedicament() {
        when(medicamentClient.findById(9L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> service.create(request(null, ligne(9L, 1))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).saveAndFlush(any());
        verify(stockClient, never()).deductStock(any());
    }

    @Test
    void createTouchesNothingWhenStockIsInsufficient() {
        when(stockClient.getAvailableStock(2L)).thenReturn(0);

        assertThatThrownBy(() -> service.create(request(null, ligne(1L, 1), ligne(2L, 1))))
                .isInstanceOf(ConflictException.class);

        verify(repository, never()).saveAndFlush(any());
        verify(stockClient, never()).deductStock(any());
    }

    @Test
    void createRollsBackTheSaleWhenStockDeductionIsRefused() {
        FeignException.BadRequest refused = mock(FeignException.BadRequest.class);
        when(refused.contentUTF8()).thenReturn("Quantité insuffisante");
        doThrow(refused).when(stockClient).deductStock(any());
        when(ordonnanceClient.findById(5L)).thenReturn(new OrdonnanceDto(5L, "EN_ATTENTE"));

        assertThatThrownBy(() -> service.create(request(5L, ligne(1L, 1))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Quantité insuffisante");

        verify(transactionManager).rollback(transaction);
        verify(transactionManager, never()).commit(any());
        verify(ordonnanceClient, never()).serve(any());
    }

    @Test
    void createRollsBackTheSaleWhenStockServiceIsDown() {
        FeignException.ServiceUnavailable down = mock(FeignException.ServiceUnavailable.class);
        doThrow(down).when(stockClient).deductStock(any());

        assertThatThrownBy(() -> service.create(request(null, ligne(1L, 1)))).isSameAs(down);

        verify(transactionManager).rollback(transaction);
    }

    @Test
    void createServesPendingOrdonnance() {
        when(ordonnanceClient.findById(5L)).thenReturn(new OrdonnanceDto(5L, "EN_ATTENTE"));

        VenteResponse response = service.create(request(5L, ligne(1L, 1)));

        assertThat(response.ordonnanceId()).isEqualTo(5L);
        assertThat(response.avertissement()).isNull();
        verify(ordonnanceClient).serve(5L);
    }

    @Test
    void createRejectsOrdonnanceThatWasAlreadyServed() {
        when(ordonnanceClient.findById(5L)).thenReturn(new OrdonnanceDto(5L, "SERVIE"));

        assertThatThrownBy(() -> service.create(request(5L, ligne(1L, 1))))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(medicamentClient, stockClient);
    }

    @Test
    void createRejectsUnknownOrdonnance() {
        when(ordonnanceClient.findById(5L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> service.create(request(5L, ligne(1L, 1))))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(medicamentClient, stockClient);
    }

    @Test
    void createDoesNotTouchStockWhenOrdonnanceServiceIsDown() {
        FeignException.ServiceUnavailable down = mock(FeignException.ServiceUnavailable.class);
        when(ordonnanceClient.findById(5L)).thenThrow(down);

        assertThatThrownBy(() -> service.create(request(5L, ligne(1L, 1)))).isSameAs(down);

        verifyNoInteractions(medicamentClient, stockClient);
    }

    @Test
    void createKeepsTheSaleAndWarnsWhenServingOrdonnanceFails() {
        when(ordonnanceClient.findById(5L)).thenReturn(new OrdonnanceDto(5L, "EN_ATTENTE"));
        doThrow(mock(FeignException.ServiceUnavailable.class)).when(ordonnanceClient).serve(5L);

        VenteResponse response = service.create(request(5L, ligne(1L, 1)));

        assertThat(response.avertissement()).contains("ordonnance 5");
        verify(transactionManager).commit(transaction);
    }

    @Test
    void findByIdReturnsLinesAndLeavesNameEmptyWhenMedicamentServiceIsDown() {
        Vente vente = new Vente(null);
        vente.addLigne(new LigneVente(1L, 2, new BigDecimal("8.50")));
        vente.addLigne(new LigneVente(3L, 1, new BigDecimal("2.00")));
        when(repository.findWithLignesById(7L)).thenReturn(Optional.of(vente));
        when(medicamentClient.findById(3L)).thenThrow(mock(FeignException.ServiceUnavailable.class));

        VenteResponse response = service.findById(7L);

        assertThat(response.total()).isEqualByComparingTo("10.50");
        assertThat(response.lignes().get(0).nomCommercial()).isEqualTo("Paracetamol");
        assertThat(response.lignes().get(1).nomCommercial()).isNull();
    }

    @Test
    void findByIdRejectsUnknownVente() {
        when(repository.findWithLignesById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(7L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private VenteRequest request(Long ordonnanceId, VenteRequest.Ligne... lignes) {
        return new VenteRequest(ordonnanceId, List.of(lignes));
    }

    private VenteRequest.Ligne ligne(Long medicamentId, int quantite) {
        return new VenteRequest.Ligne(medicamentId, quantite);
    }
}
