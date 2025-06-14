package com.crewmeister.cmcodingchallenge.currency;

import com.crewmeister.cmcodingchallenge.dto.CurrencyDto;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDto;
import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController()
@RequestMapping("/api")
@Validated
public class CurrencyController {

    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;

    public CurrencyController(CurrencyService currencyService, ExchangeRateService exchangeRateService) {
        this.currencyService = currencyService;
        this.exchangeRateService = exchangeRateService;
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
    public ResponseEntity<List<ExchangeRateDto>> getAllExchangeRates() {
        List<ExchangeRateDto> rates = exchangeRateService.getAllExchangeRates();
        return ResponseEntity.ok(rates);
    }

    /**
     * Get EUR-FX exchange rate for particular currency on particular day
     */
    @GetMapping("/exchange-rates/{currency}/{date}")
    public ResponseEntity<ExchangeRateDto> getExchangeRate(
            @PathVariable
            @NotBlank(message = "Currency code is required")
            @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
            String currency,

            @PathVariable
            @NotNull(message = "Date is required")
            @PastOrPresent(message = "Date cannot be in the future")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        ExchangeRateDto rate = exchangeRateService.getExchangeRate(currency, date);
        return ResponseEntity.ok(rate);
    }

    /**
     * Convert foreign exchange amount to EUR on particular day
     */
    @GetMapping("/convert/{amount}/{currency}/{date}")
    public ResponseEntity<CurrencyConversionRates> convertCurrency(
            @PathVariable
            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be positive")
            BigDecimal amount,

            @PathVariable
            @NotBlank(message = "Currency code is required")
            @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
            String currency,

            @PathVariable
            @NotNull(message = "Date is required")
            @PastOrPresent(message = "Date cannot be in the future")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        CurrencyConversionRates conversionResult = exchangeRateService.convertCurrency(amount, currency, date);
        return ResponseEntity.ok(conversionResult);
    }
}
