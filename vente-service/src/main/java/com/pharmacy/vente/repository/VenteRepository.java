package com.pharmacy.vente.repository;

import com.pharmacy.vente.entity.Vente;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    @EntityGraph(attributePaths = "lignes")
    Optional<Vente> findWithLignesById(Long id);
}
