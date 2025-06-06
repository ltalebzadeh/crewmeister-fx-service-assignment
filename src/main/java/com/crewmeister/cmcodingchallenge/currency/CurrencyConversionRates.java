package com.crewmeister.cmcodingchallenge.currency;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyConversionRates {

    @NotNull(message = "Original amount is required")
    @Positive(message = "Original amount must be positive")
    @JsonProperty("original_amount")
    private BigDecimal originalAmount;

    @NotBlank(message = "Original currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @JsonProperty("original_currency")
    private String originalCurrency;

    @NotNull(message = "Converted amount is required")
    @Positive(message = "Converted amount must be positive")
    @JsonProperty("converted_amount")
    private BigDecimal convertedAmount;

    @NotBlank(message = "Target currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @JsonProperty("target_currency")
    private String targetCurrency;

    @NotNull(message = "Exchange rate is required")
    @Positive(message = "Exchange rate must be positive")
    @JsonProperty("exchange_rate")
    private BigDecimal exchangeRate;

    @NotNull(message = "Conversion date is required")
    @PastOrPresent(message = "Conversion date cannot be in the future")
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
