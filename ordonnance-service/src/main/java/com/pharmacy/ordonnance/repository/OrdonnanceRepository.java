package com.pharmacy.ordonnance.repository;

import com.pharmacy.ordonnance.model.Ordonnance;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {

    List<Ordonnance> findAllByOrderByDatePrescriptionDescIdDesc();
}
