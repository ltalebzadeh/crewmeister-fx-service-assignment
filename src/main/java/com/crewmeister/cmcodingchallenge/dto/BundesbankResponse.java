package com.crewmeister.cmcodingchallenge.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BundesbankResponse {

    private LocalDate date;
    private Map<String, BigDecimal> rates = new HashMap<>();
    private Map<String, String> currencyNames = new HashMap<>();
    private String dataSource;
    private boolean successful = true;
    private String errorMessage;

    public static BundesbankResponse success(LocalDate date, String dataSource) {
        BundesbankResponse response = new BundesbankResponse();
        response.setDate(date);
        response.setDataSource(dataSource);
        response.setSuccessful(true);
        return response;
    }

    public static BundesbankResponse failure(String errorMessage) {
        BundesbankResponse response = new BundesbankResponse();
        response.setSuccessful(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public void addCurrency(String code, String name, BigDecimal rate) {
        this.currencyNames.put(code, name);
        this.rates.put(code, rate);
    }

    public boolean hasData() {
        return successful && !rates.isEmpty();
    }

    public int getCurrencyCount() {
        return rates.size();
    }

    public Optional<BigDecimal> getRate(String currencyCode) {
        return Optional.ofNullable(rates.get(currencyCode));
    }

    public Optional<String> getCurrencyName(String currencyCode) {
        if (currencyCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(currencyNames.get(currencyCode.toUpperCase()));
    }
}
