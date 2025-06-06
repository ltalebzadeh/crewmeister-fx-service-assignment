package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.client.BundesbankApiClient;
import com.crewmeister.cmcodingchallenge.currency.CurrencyConversionRates;
import com.crewmeister.cmcodingchallenge.dto.BundesbankResponse;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDto;
import com.crewmeister.cmcodingchallenge.entity.Currency;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.exception.ExchangeRateNotFoundException;
import com.crewmeister.cmcodingchallenge.repository.CurrencyRepository;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyService currencyService;
    private final CurrencyRepository currencyRepository;
    private final BundesbankApiClient bundesbankClient;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, CurrencyService currencyService, CurrencyRepository currencyRepository, BundesbankApiClient bundesbankClient) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyService = currencyService;
        this.currencyRepository = currencyRepository;
        this.bundesbankClient = bundesbankClient;
    }

    public List<ExchangeRateDto> getAllExchangeRates() {
        List<ExchangeRate> rates = exchangeRateRepository.findAllWithCurrency();
        return rates.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ExchangeRateDto getExchangeRate(String currencyCode, LocalDate date) {
        Optional<ExchangeRate> rate = exchangeRateRepository.findByCurrencyCodeAndRateDate(currencyCode.toUpperCase(), date);
        if (rate.isPresent()) {
            return toDto(rate.get());
        }

        throw new ExchangeRateNotFoundException(currencyCode, date);
    }

    public CurrencyConversionRates convertCurrency(BigDecimal amount, String fromCurrency, LocalDate date) {
        if ("EUR".equalsIgnoreCase(fromCurrency)) {
            return new CurrencyConversionRates(
                    amount,
                    "EUR",
                    amount,
                    "EUR",
                    BigDecimal.ONE,
                    date
            );
        }

        ExchangeRateDto exchangeRate = getExchangeRate(fromCurrency, date);
        BigDecimal convertedAmount = amount.divide(exchangeRate.getRate(), 6, RoundingMode.HALF_UP);

        return new CurrencyConversionRates(
                amount,
                fromCurrency.toUpperCase(),
                convertedAmount,
                "EUR",
                exchangeRate.getRate(),
                date
        );
    }

    private ExchangeRateDto toDto(ExchangeRate exchangeRate) {
        return new ExchangeRateDto(
                exchangeRate.getCurrency().getCode(),
                exchangeRate.getCurrency().getName(),
                exchangeRate.getRateDate(),
                exchangeRate.getRate()
        );
    }

    @Transactional
    public void fetchAndStoreExchangeRates() {
        bundesbankClient.getExchangeRates()
                .subscribe(response -> {
                    if (response.getDate() != null && !response.getRates().isEmpty()) {
                        storeExchangeRatesAndCurrencies(response);
                    }
                }, error -> logger.error("Failed to fetch exchange rates", error));
    }

    private void storeExchangeRatesAndCurrencies(BundesbankResponse response) {
        if (!response.isSuccessful()) {
            logger.error("Cannot store exchange rates: {}", response.getErrorMessage());
            return;
        }

        if (!response.hasData()) {
            logger.warn("Cannot store exchange rates: no data available");
            return;
        }

        logger.info("Starting batch storage");

        try {
            batchStoreCurrencies(response);

            batchStoreExchangeRates(response);

            logger.info("Successfully completed batch storage for {} currencies on {}",
                    response.getCurrencyCount(), response.getDate());

        } catch (Exception e) {
            logger.error("Batch storage failed for {}", response.getDate(), e);
            throw new RuntimeException("Failed to store exchange rates", e);
        }
    }

    private void batchStoreCurrencies(BundesbankResponse response) {
        List<Currency> currenciesToSave = new ArrayList<>();

        for (Map.Entry<String, String> entry : response.getCurrencyNames().entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue();

            Optional<Currency> existing = currencyService.findByCode(code);
            if (existing.isPresent()) {
                Currency currency = existing.get();
                currency.setName(name);
                currenciesToSave.add(currency);
            } else {
                currenciesToSave.add(new Currency(code, name));
            }
        }

        if (!currenciesToSave.isEmpty()) {
            currencyRepository.saveAll(currenciesToSave);
            logger.debug("Batch saved {} currencies", currenciesToSave.size());
        }
    }

    private void batchStoreExchangeRates(BundesbankResponse response) {
        LocalDate date = response.getDate();
        List<ExchangeRate> ratesToSave = new ArrayList<>();

        Map<String, ExchangeRate> existingRates = exchangeRateRepository
                .findByRateDate(date)
                .stream()
                .collect(Collectors.toMap(
                        rate -> rate.getCurrency().getCode(),
                        Function.identity()
                ));

        for (Map.Entry<String, BigDecimal> entry : response.getRates().entrySet()) {
            String currencyCode = entry.getKey();
            BigDecimal rate = entry.getValue();

            Currency currency = currencyService.findByCode(currencyCode)
                    .orElseThrow(() -> new RuntimeException("Currency not found: " + currencyCode));

            ExchangeRate exchangeRate = existingRates.get(currencyCode);
            if (exchangeRate != null) {
                exchangeRate.setRate(rate);
            } else {
                exchangeRate = new ExchangeRate(currency, date, rate);
            }

            ratesToSave.add(exchangeRate);
        }

        if (!ratesToSave.isEmpty()) {
            exchangeRateRepository.saveAll(ratesToSave);
            logger.debug("Batch saved {} exchange rates for {}", ratesToSave.size(), date);
        }
    }
}
