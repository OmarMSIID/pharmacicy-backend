package com.example.stockservice.Scheduler;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stockservice.Repo.StockLotRepository;
import com.example.stockservice.model.StockLot;

@Component 
public class ExporationAlertScheduler {
    private static final Logger log = LoggerFactory.getLogger(ExporationAlertScheduler.class);
    private final StockLotRepository repository;

    public ExporationAlertScheduler(StockLotRepository repository) {
        this.repository = repository;
    }

    // Runs every day at 8:00 AM (also runs 10 seconds after startup for initial check)
    @Scheduled(cron = "0 0 8 * * *")
    @Scheduled(initialDelay = 10000, fixedDelay = 86400000)
    public void checkExpirations() {
        LocalDate alertDate = LocalDate.now().plusDays(30);
        List<StockLot> expiringLots = repository.findByDatePeremptionLessThanEqualAndQuantiteGreaterThan(alertDate, 0);

        if (expiringLots.isEmpty()) {
            log.info("[ALERTE PÉREMPTION] Aucun lot n'arrive à expiration dans les 30 prochains jours.");
        } else {
            log.warn("[ALERTE PÉREMPTION] {} lot(s) périmé(s) ou expirant sous 30 jours détécté(s) !", expiringLots.size());
            for (StockLot lot : expiringLots) {
                log.warn(" -> Lot: {} | Médicament ID: {} | Quantité restante: {} | Date péremption: {}",
                        lot.getNumeroLot(), lot.getMedicamentId(), lot.getQuantite(), lot.getDatePeremption());
            }
        }
    }
}
