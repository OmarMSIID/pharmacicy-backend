package com.pharmacy.vente.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.pharmacy.vente.entity.LigneVente;
import com.pharmacy.vente.entity.Vente;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class VenteRepositoryTest {

    @Autowired
    private VenteRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesLinesWithTheSaleAndLoadsThemBack() {
        Vente vente = new Vente(5L);
        vente.addLigne(new LigneVente(1L, 2, new BigDecimal("8.50")));
        vente.addLigne(new LigneVente(2L, 1, new BigDecimal("10.00")));

        Long id = repository.saveAndFlush(vente).getId();
        entityManager.clear();

        Vente loaded = repository.findWithLignesById(id).orElseThrow();
        assertThat(loaded.getDateVente()).isNotNull();
        assertThat(loaded.getOrdonnanceId()).isEqualTo(5L);
        assertThat(loaded.getTotal()).isEqualByComparingTo("18.50");
        assertThat(loaded.getLignes()).hasSize(2);
        assertThat(loaded.getLignes()).allSatisfy(ligne -> assertThat(ligne.getId()).isNotNull());
    }
}
