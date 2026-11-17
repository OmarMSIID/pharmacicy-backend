@Service
@RequiredArgsConstructor
public class OrdonnanceServiceImpl {

    private final OrdonnanceRepository repository;
    private final MedicamentClient medicamentClient;

    public Ordonnance createOrdonnance(OrdonnanceRequest request) {
        boolean necessitePrescripteur = false;

        // 1. Vérifier si un des médicaments exige une ordonnance
        for (Long medId : request.getMedicamentIds()) {
            MedicamentDTO med = medicamentClient.getMedicamentById(medId);
            if (med != null && med.isSurOrdonnance()) {
                necessitePrescripteur = true;
                break;
            }
        }

        // 2. Règle métier : Si ordonnance requise, le prescripteur est OBLIGATOIRE
        if (necessitePrescripteur && (request.getNomPrescripteur() == null || request.getNomPrescripteur().trim().isEmpty())) {
            throw new IllegalArgumentException("Nom du prescripteur obligatoire : au moins un médicament nécessite une ordonnance.");
        }

        // 3. Sauvegarder
        Ordonnance ordonnance = Ordonnance.builder()
                .nomPrescripteur(request.getNomPrescripteur())
                .medicamentIds(request.getMedicamentIds())
                .datePrescription(LocalDateTime.now())
                .statut(StatutOrdonnance.EN_ATTENTE)
                .build();

        return repository.save(ordonnance);
    }

    // Méthode appelée par vente-service (d'après ton document)
    public Ordonnance marquerCommeServie(Long id) {
        Ordonnance ord = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordonnance introuvable"));
        ord.setStatut(StatutOrdonnance.SERVIE);
        return repository.save(ord);
    }
}