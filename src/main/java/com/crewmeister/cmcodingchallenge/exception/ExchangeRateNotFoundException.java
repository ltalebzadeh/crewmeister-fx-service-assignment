package com.crewmeister.cmcodingchallenge.exception;

import java.time.LocalDate;

public class ExchangeRateNotFoundException extends RuntimeException {

    public ExchangeRateNotFoundException(String currencyCode, LocalDate date) {
        super(String.format("Exchange rate for currency '%s' on date '%s' not found",
                currencyCode, date));
    }
}
