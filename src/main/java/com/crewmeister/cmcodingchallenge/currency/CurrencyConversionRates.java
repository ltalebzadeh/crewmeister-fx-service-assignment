package com.crewmeister.cmcodingchallenge.currency;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CurrencyConversionRates {
    @JsonProperty("original_amount")
    private BigDecimal originalAmount;

    @JsonProperty("original_currency")
    private String originalCurrency;

    @JsonProperty("converted_amount")
    private BigDecimal convertedAmount;

    @JsonProperty("target_currency")
    private String targetCurrency;

    @JsonProperty("exchange_rate")
    private BigDecimal exchangeRate;

    @JsonProperty("conversion_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
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
