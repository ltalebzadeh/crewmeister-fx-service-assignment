package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.currency.CurrencyConversionRates;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDto;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.exception.ExchangeRateNotFoundException;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Cacheable("exchangeRates")
    @Transactional(readOnly = true)
    public List<ExchangeRateDto> getAllExchangeRates() {
        List<ExchangeRate> rates = exchangeRateRepository.findAllWithCurrency();
        return rates.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ExchangeRateDto getExchangeRate(String currencyCode, LocalDate date) {
        Optional<ExchangeRate> rate = exchangeRateRepository.findByCurrencyCodeAndDate(currencyCode.toUpperCase(), date);
        if (rate.isPresent()) {
            return toDto(rate.get());
        }

        // TODO: fetch from external API if not found

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
}
