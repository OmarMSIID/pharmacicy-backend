package com.example.stockservice.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface  StockProjection {
    Long getId();
    String getNumeroLot();
    Integer getQuantite();
    LocalDate getDatePeremption();
    String getNomCommercial();
    String getDci();
    BigDecimal getPrixUnitaire();
}
