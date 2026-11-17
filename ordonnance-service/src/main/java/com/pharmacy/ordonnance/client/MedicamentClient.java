@FeignClient(name = "medicament-service")
public interface MedicamentClient {
    @GetMapping("/api/medicaments/{id}")
    MedicamentDTO getMedicamentById(@PathVariable("id") Long id);
}