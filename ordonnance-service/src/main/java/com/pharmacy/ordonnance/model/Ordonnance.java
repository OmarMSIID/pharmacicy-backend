
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "ordonnances")
public class Ordonnance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomPrescripteur; // Peut être null si aucun médoc ne l'exige
    
    private LocalDateTime datePrescription;
    
    @Enumerated(EnumType.STRING)
    private StatutOrdonnance statut;
    
    @ElementCollection
    @CollectionTable(name = "ordonnance_medicaments", joinColumns = @JoinColumn(name = "ordonnance_id"))
    @Column(name = "medicament_id")
    private List<Long> medicamentIds;
}