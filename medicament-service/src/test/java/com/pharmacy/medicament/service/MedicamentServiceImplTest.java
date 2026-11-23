package com.pharmacy.medicament.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pharmacy.medicament.dto.MedicamentRequest;
import com.pharmacy.medicament.dto.MedicamentResponse;
import com.pharmacy.medicament.entity.Medicament;
import com.pharmacy.medicament.exception.ResourceNotFoundException;
import com.pharmacy.medicament.repository.MedicamentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Vérifie le comportement du service sans démarrer Spring ni se connecter à la base de données.
 */
class MedicamentServiceImplTest {

    // Le repository simulé permet de contrôler les réponses de la couche de persistance.
    //  (3zma Simulation dyal Database without actually connecting to it)
    private final MedicamentRepository repository = mock(MedicamentRepository.class);
    private MedicamentServiceImpl service;

    @BeforeEach
    void setUp() {
        // Chaque test reçoit un service neuf utilisant le même repository simulé.
        service = new MedicamentServiceImpl(repository);
    }

    @Test
    // Vérifie que la liste d'entités renvoyée par le repository est convertie en DTO de réponse.
    void findAllMapsMedicamentsToResponses() {
        when(repository.findAll()).thenReturn(List.of(
                medicament("Paracetamol", "Paracetamol", "500 mg", "Comprimé", "4.25"),
                medicament("Ibuprofène", "Ibuprofène", "200 mg", "Gélule", "8.50")));

        List<MedicamentResponse> responses = service.findAll();

        assertThat(responses).containsExactly(
                response("Paracetamol", "Paracetamol", "500 mg", "Comprimé", "4.25"),
                response("Ibuprofène", "Ibuprofène", "200 mg", "Gélule", "8.50"));
        verify(repository).findAll();
    }

    @Test
    // Vérifie la recherche par identifiant et la conversion de l'entité trouvée.
    void findByIdReturnsTheMatchingMedicament() {
        Medicament medicament = medicament("Paracetamol", "Paracetamol", "500 mg", "Comprimé", "4.25");
        when(repository.findById(1L)).thenReturn(Optional.of(medicament));

        assertThat(service.findById(1L))
                .isEqualTo(response("Paracetamol", "Paracetamol", "500 mg", "Comprimé", "4.25"));
    }

    @Test
    // Vérifie qu'une recherche avec un identifiant inconnu signale une ressource absente.
    void findByIdThrowsWhenMedicamentDoesNotExist() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Medicament not found with id: 42");
    }

    @Test
    // Vérifie que les données de la requête sont enregistrées et renvoyées dans la réponse.
    void createSavesAndReturnsTheRequestedMedicament() {
        MedicamentRequest request = request("Paracetamol", "Paracetamol", "500 mg", "Comprimé", "4.25", true);
        when(repository.save(any(Medicament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicamentResponse response = service.create(request);

        assertThat(response).isEqualTo(response("Paracetamol", "Paracetamol", "500 mg", "Comprimé", "4.25", true));
        verify(repository).save(argThat(saved ->
                saved.getNomCommercial().equals("Paracetamol")
                        && saved.getDci().equals("Paracetamol")
                        && saved.getDosage().equals("500 mg")
                        && saved.getForme().equals("Comprimé")
                        && saved.getPrixUnitaire().compareTo(new BigDecimal("4.25")) == 0
                        && saved.isSurOrdonnance()));
    }

    @Test
    // Vérifie que la mise à jour remplace chacun des champs du médicament existant.
    void updateChangesAndSavesAllMedicamentFields() {
        Medicament existing = medicament("Ancien nom", "Ancienne DCI", "100 mg", "Sirop", "2.00");
        MedicamentRequest request = request("Nouveau nom", "Nouvelle DCI", "250 mg", "Comprimé", "6.75", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        MedicamentResponse response = service.update(1L, request);

        assertThat(response).isEqualTo(response("Nouveau nom", "Nouvelle DCI", "250 mg", "Comprimé", "6.75", true));
        assertThat(existing.getNomCommercial()).isEqualTo("Nouveau nom");
        assertThat(existing.getDci()).isEqualTo("Nouvelle DCI");
        assertThat(existing.getDosage()).isEqualTo("250 mg");
        assertThat(existing.getForme()).isEqualTo("Comprimé");
        assertThat(existing.getPrixUnitaire()).isEqualByComparingTo("6.75");
        assertThat(existing.isSurOrdonnance()).isTrue();
        verify(repository).save(existing);
    }

    @Test
    // Vérifie qu'une mise à jour d'un identifiant absent échoue sans enregistrer de données.
    void updateThrowsWithoutSavingWhenMedicamentDoesNotExist() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(
                42L, request("Paracetamol", "Paracetamol", "500 mg", "Comprimé", "4.25")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Medicament not found with id: 42");

        verify(repository, never()).save(any(Medicament.class));
    }

    @Test
    // Vérifie que la suppression cible bien le médicament récupéré par son identifiant.
    void deleteRemovesTheMatchingMedicament() {
        Medicament medicament = medicament("Paracetamol", "Paracetamol", "500 mg", "Comprimé", "4.25");
        when(repository.findById(1L)).thenReturn(Optional.of(medicament));

        service.delete(1L);

        verify(repository).delete(medicament);
    }

    @Test
    // Vérifie qu'aucune suppression n'est tentée si l'identifiant ne correspond à rien.
    void deleteThrowsWhenMedicamentDoesNotExist() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Medicament not found with id: 42");

        verify(repository, never()).delete(any(Medicament.class));
    }

    // Construit une entité de test avec un prix décimal exact, sans dépendre de la base.
    private static Medicament medicament(
            String nomCommercial, String dci, String dosage, String forme, String prixUnitaire) {
        return new Medicament(nomCommercial, dci, dosage, forme, new BigDecimal(prixUnitaire), false);
    }

    // Construit le DTO d'entrée utilisé par les scénarios de création et de mise à jour.
    private static MedicamentRequest request(
            String nomCommercial, String dci, String dosage, String forme, String prixUnitaire) {
        return request(nomCommercial, dci, dosage, forme, prixUnitaire, false);
    }

    private static MedicamentRequest request(
            String nomCommercial, String dci, String dosage, String forme, String prixUnitaire, boolean surOrdonnance) {
        return new MedicamentRequest(
                nomCommercial, dci, dosage, forme, new BigDecimal(prixUnitaire), surOrdonnance);
    }

    // Construit le DTO attendu dans les assertions des tests.
    private static MedicamentResponse response(
            String nomCommercial, String dci, String dosage, String forme, String prixUnitaire) {
        return response(nomCommercial, dci, dosage, forme, prixUnitaire, false);
    }

    private static MedicamentResponse response(
            String nomCommercial, String dci, String dosage, String forme, String prixUnitaire, boolean surOrdonnance) {
        return new MedicamentResponse(
                null, nomCommercial, dci, dosage, forme, new BigDecimal(prixUnitaire), surOrdonnance);
    }
}
