package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.dto.CurrencyDto;
import com.crewmeister.cmcodingchallenge.entity.Currency;
import com.crewmeister.cmcodingchallenge.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public List<CurrencyDto> getAllCurrencies() {
        logger.info("Getting all available currencies");
        List<Currency> currencies = currencyRepository.findAll();
        return currencies.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<Currency> findByCode(String code) {
        return currencyRepository.findById(code.toUpperCase());
    }

    private CurrencyDto toDto(Currency currency) {
        return new CurrencyDto(currency.getCode(), currency.getName());
    }
}
