package com.crewmeister.cmcodingchallenge.currency;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CurrencyConversionRates {
    private BigDecimal originalAmount;
    private String originalCurrency;
    private BigDecimal convertedAmount;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private LocalDate date;

    public CurrencyConversionRates(double conversionRate) {
        this.exchangeRate = BigDecimal.valueOf(conversionRate);
    }

    public double getConversionRate() {
        return exchangeRate != null ? exchangeRate.doubleValue() : 0.0;
    }

    public void setConversionRate(double conversionRate) {
        this.exchangeRate = BigDecimal.valueOf(conversionRate);
    }
}
