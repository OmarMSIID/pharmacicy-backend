package com.pharmacy.medicament.repository;

import com.pharmacy.medicament.entity.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentRepository extends JpaRepository<Medicament, Long> {
}
