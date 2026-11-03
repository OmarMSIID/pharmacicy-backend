package com.example.stockservice.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockservice.Client.MedicamentClient;
import com.example.stockservice.DTO.StockDeductRequest;
import com.example.stockservice.Repo.StockLotRepository;
import com.example.stockservice.model.StockLot;

// Real transactions on H2: the test's own transaction is disabled so that rollback can be observed
@DataJpaTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
@Import(StockService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StockServiceBatchDeductTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockLotRepository repository;

    @MockBean
    private MedicamentClient medicamentClient;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void deductsEveryLineWhenStockIsSufficient() {
        StockLot first = saveLot(1L, 10);
        StockLot second = saveLot(2L, 5);

        stockService.deductStockBatch(List.of(
                new StockDeductRequest(1L, 4),
                new StockDeductRequest(2L, 5)));

        assertThat(quantityOf(first)).isEqualTo(6);
        assertThat(quantityOf(second)).isZero();
    }

    @Test
    void rollsBackEarlierLinesWhenALaterLineFails() {
        StockLot first = saveLot(1L, 10);
        StockLot second = saveLot(2L, 1);

        assertThatThrownBy(() -> stockService.deductStockBatch(List.of(
                new StockDeductRequest(1L, 4),
                new StockDeductRequest(2L, 3))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(quantityOf(first)).isEqualTo(10);
        assertThat(quantityOf(second)).isEqualTo(1);
    }

    private StockLot saveLot(Long medicamentId, int quantite) {
        return repository.save(new StockLot(medicamentId, "LOT-" + medicamentId, quantite,
                LocalDate.now().plusYears(1), 10));
    }

    private int quantityOf(StockLot lot) {
        return repository.findById(lot.getId()).orElseThrow().getQuantite();
    }
}
