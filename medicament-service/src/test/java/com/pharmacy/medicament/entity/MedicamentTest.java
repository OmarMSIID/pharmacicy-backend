package com.pharmacy.medicament.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MedicamentTest {

    @Test
    void constructorInitializesAllFields() {
        Medicament medicament = new Medicament(
                "Paracetamol",
                "Paracetamol",
                "500 mg",
                "Comprimé",
                new BigDecimal("4.25"),
                true);

        assertThat(medicament.getId()).isNull();
        assertThat(medicament.getNomCommercial()).isEqualTo("Paracetamol");
        assertThat(medicament.getDci()).isEqualTo("Paracetamol");
        assertThat(medicament.getDosage()).isEqualTo("500 mg");
        assertThat(medicament.getForme()).isEqualTo("Comprimé");
        assertThat(medicament.getPrixUnitaire()).isEqualByComparingTo("4.25");
        assertThat(medicament.isSurOrdonnance()).isTrue();
    }

    @Test
    void settersUpdateAllMutableFields() {
        Medicament medicament = new Medicament(
                "Ancien nom",
                "Ancienne DCI",
                "100 mg",
                "Sirop",
                new BigDecimal("2.00"),
                false);

        medicament.setNomCommercial("Nouveau nom");
        medicament.setDci("Nouvelle DCI");
        medicament.setDosage("250 mg");
        medicament.setForme("Comprimé");
        medicament.setPrixUnitaire(new BigDecimal("6.75"));
        medicament.setSurOrdonnance(true);

        assertThat(medicament.getNomCommercial()).isEqualTo("Nouveau nom");
        assertThat(medicament.getDci()).isEqualTo("Nouvelle DCI");
        assertThat(medicament.getDosage()).isEqualTo("250 mg");
        assertThat(medicament.getForme()).isEqualTo("Comprimé");
        assertThat(medicament.getPrixUnitaire()).isEqualByComparingTo("6.75");
        assertThat(medicament.isSurOrdonnance()).isTrue();
    }

    @Test
    void constructorSupportsNullOptionalFields() {
        Medicament medicament = new Medicament(
                "Aspirine", null, null, null, new BigDecimal("1.50"), false);

        assertThat(medicament.getNomCommercial()).isEqualTo("Aspirine");
        assertThat(medicament.getDci()).isNull();
        assertThat(medicament.getDosage()).isNull();
        assertThat(medicament.getForme()).isNull();
        assertThat(medicament.getPrixUnitaire()).isEqualByComparingTo("1.50");
        assertThat(medicament.isSurOrdonnance()).isFalse();
    }
}
