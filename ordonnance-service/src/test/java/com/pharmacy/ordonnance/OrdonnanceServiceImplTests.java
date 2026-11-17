import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        medSansOrdonnance = new MedicamentDTO();
        medSansOrdonnance.setId(1L);
        medSansOrdonnance.setSurOrdonnance(false);

        medAvecOrdonnance = new MedicamentDTO();
        medAvecOrdonnance.setId(2L);
        medAvecOrdonnance.setSurOrdonnance(true);
    }

    // UT-ORD-01
    @Test
    void testCreateOrdonnance_Reussie_AvecPrescripteur() {
        // Arrange
        OrdonnanceRequest request = OrdonnanceRequest.builder()
                .nomPrescripteur("Dr. Chraibi")
                .medicamentIds(List.of(2L))
                .build();

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
        OrdonnanceRequest request = OrdonnanceRequest.builder()
                .nomPrescripteur(null) // Omission du médecin !
                .medicamentIds(List.of(2L))
                .build();

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
        OrdonnanceRequest request = OrdonnanceRequest.builder()
                .nomPrescripteur(null) // Pas de médecin
                .medicamentIds(List.of(1L)) // Médicament libre (Doliprane par ex)
                .build();

        when(medicamentClient.getMedicamentById(1L)).thenReturn(medSansOrdonnance);
        when(repository.save(any(Ordonnance.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Ordonnance result = ordonnanceService.createOrdonnance(request);

        // Assert
        assertNotNull(result);
        assertNull(result.getNomPrescripteur());
        verify(repository, times(1)).save(any(Ordonnance.class));
    }
}