package com.crewmeister.cmcodingchallenge.currency;

import com.crewmeister.cmcodingchallenge.dto.CurrencyDto;
import com.crewmeister.cmcodingchallenge.entity.Currency;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/api")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    /**
     * Get all available currencies
     */
    @GetMapping("/currencies")
    public ResponseEntity<List<CurrencyDto>> getCurrencies() {
        List<CurrencyDto> currencies = currencyService.getAllCurrencies();
        return new ResponseEntity<>(currencies, HttpStatus.OK);
    }

    /**
     * Get all EUR-FX exchange rates at all available dates
     */
    @GetMapping("/exchange-rates")
    public ResponseEntity<List<ExchangeRate>> getAllExchangeRates() {
        // TODO: Replace with service methods
        List<ExchangeRate> exchangeRates = new ArrayList<>();

        return ResponseEntity.ok(exchangeRates);
    }

    /**
     * Get EUR-FX exchange rate for particular currency on particular day
     */
    @GetMapping("/exchange-rates/{currency}/{date}")
    public ResponseEntity<ExchangeRate> getExchangeRate(
            @PathVariable
            String currency,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        // TODO: Replace with service method
        Currency currencyEntity = new Currency("hard code", currency);
        ExchangeRate exchangeRate = new ExchangeRate(currencyEntity, date, new BigDecimal("1.0"));
        return ResponseEntity.ok(exchangeRate);
    }

    /**
     * Convert foreign exchange amount to EUR on particular day
     */
    @GetMapping("/convert/{amount}/{currency}/{date}")
    public ResponseEntity<CurrencyConversionRates> convertCurrency(
            @PathVariable
            BigDecimal amount,
            @PathVariable
            String currency,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        // TODO: Replace with service method
        BigDecimal convertedAmount = new BigDecimal("100.0");
        CurrencyConversionRates conversion =
                new CurrencyConversionRates(amount, currency, convertedAmount, "EUR", new BigDecimal("1.0"), date);
        return ResponseEntity.ok(conversion);
    }
}
