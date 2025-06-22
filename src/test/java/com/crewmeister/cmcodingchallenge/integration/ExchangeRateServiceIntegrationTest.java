package com.crewmeister.cmcodingchallenge.integration;

import com.crewmeister.cmcodingchallenge.currency.CurrencyConversionRates;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDto;
import com.crewmeister.cmcodingchallenge.entity.Currency;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.exception.ExchangeRateNotFoundException;
import com.crewmeister.cmcodingchallenge.repository.CurrencyRepository;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ExchangeRateServiceIntegrationTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CacheManager cacheManager;

    private Currency usdCurrency;
    private Currency gbpCurrency;
    private Currency jpyCurrency;
    private Currency chfCurrency;

    private LocalDate today;
    private LocalDate yesterday;
    private LocalDate dayBeforeYesterday;

    @BeforeEach
    void setUp() {
        tearDown();

        today = LocalDate.of(2025, 6, 4);
        yesterday = today.minusDays(1);
        dayBeforeYesterday = today.minusDays(2);

        usdCurrency = currencyRepository.save(new Currency("USD", "US Dollar"));
        gbpCurrency = currencyRepository.save(new Currency("GBP", "British Pound"));
        jpyCurrency = currencyRepository.save(new Currency("JPY", "Japanese Yen"));
        chfCurrency = currencyRepository.save(new Currency("CHF", "Swiss Franc"));

        createTestExchangeRates();
    }

    @AfterEach
    void tearDown() {
        exchangeRateRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    private void createTestExchangeRates() {
        // Today's rates
        exchangeRateRepository.save(new ExchangeRate(usdCurrency, today, new BigDecimal("1.1384")));
        exchangeRateRepository.save(new ExchangeRate(gbpCurrency, today, new BigDecimal("0.84210")));
        exchangeRateRepository.save(new ExchangeRate(jpyCurrency, today, new BigDecimal("164.15")));
        exchangeRateRepository.save(new ExchangeRate(chfCurrency, today, new BigDecimal("0.9370")));

        // Yesterday's rates
        exchangeRateRepository.save(new ExchangeRate(usdCurrency, yesterday, new BigDecimal("1.1345")));
        exchangeRateRepository.save(new ExchangeRate(gbpCurrency, yesterday, new BigDecimal("0.85200")));
        exchangeRateRepository.save(new ExchangeRate(jpyCurrency, yesterday, new BigDecimal("164.1478")));

        // Day before yesterday's rates
        exchangeRateRepository.save(new ExchangeRate(usdCurrency, dayBeforeYesterday, new BigDecimal("1.1289")));
        exchangeRateRepository.save(new ExchangeRate(gbpCurrency, dayBeforeYesterday, new BigDecimal("0.84800")));
    }

    @Test
    void getAllExchangeRates_ShouldReturnAllRatesFromDatabase() {
        List<ExchangeRateDto> result = exchangeRateService.getAllExchangeRates();

        assertNotNull(result);
        assertEquals(9, result.size());

        long usdCount = result.stream().filter(r -> "USD".equals(r.getCurrencyCode())).count();
        long gbpCount = result.stream().filter(r -> "GBP".equals(r.getCurrencyCode())).count();
        long jpyCount = result.stream().filter(r -> "JPY".equals(r.getCurrencyCode())).count();
        long chfCount = result.stream().filter(r -> "CHF".equals(r.getCurrencyCode())).count();

        assertEquals(3, usdCount);
        assertEquals(3, gbpCount);
        assertEquals(2, jpyCount);
        assertEquals(1, chfCount);

        Optional<ExchangeRateDto> usdRate = result.stream()
                .filter(r -> "USD".equals(r.getCurrencyCode()))
                .findFirst();

        assertTrue(usdRate.isPresent());
        assertEquals("US Dollar", usdRate.get().getCurrencyName());
    }

    @Test
    void getAllExchangeRates_ShouldReturnEmptyList_WhenNoDatabaseData() {
        cacheManager.getCacheNames().forEach(cacheName ->
                Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
        exchangeRateRepository.deleteAll();
        List<ExchangeRateDto> result = exchangeRateService.getAllExchangeRates();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getExchangeRate_ShouldReturnDifferentRatesForDifferentDates() {
        ExchangeRateDto todayRate = exchangeRateService.getExchangeRate("USD", today);
        ExchangeRateDto yesterdayRate = exchangeRateService.getExchangeRate("USD", yesterday);
        ExchangeRateDto oldRate = exchangeRateService.getExchangeRate("USD", dayBeforeYesterday);

        assertEquals(new BigDecimal("1.138400"), todayRate.getRate());
        assertEquals(new BigDecimal("1.134500"), yesterdayRate.getRate());
        assertEquals(new BigDecimal("1.128900"), oldRate.getRate());

        assertEquals(today, todayRate.getDate());
        assertEquals(yesterday, yesterdayRate.getDate());
        assertEquals(dayBeforeYesterday, oldRate.getDate());
    }

    @Test
    void convertCurrency_ShouldHandleDifferentDates_WithDifferentRates() {
        BigDecimal amount = new BigDecimal("100.00");

        CurrencyConversionRates todayResult = exchangeRateService.convertCurrency(amount, "USD", today);
        CurrencyConversionRates yesterdayResult = exchangeRateService.convertCurrency(amount, "USD", yesterday);
        CurrencyConversionRates oldResult = exchangeRateService.convertCurrency(amount, "USD", dayBeforeYesterday);

        assertEquals(new BigDecimal("1.138400"), todayResult.getExchangeRate());
        assertEquals(new BigDecimal("1.134500"), yesterdayResult.getExchangeRate());
        assertEquals(new BigDecimal("1.128900"), oldResult.getExchangeRate());

        assertNotEquals(todayResult.getConvertedAmount(), yesterdayResult.getConvertedAmount());
        assertNotEquals(yesterdayResult.getConvertedAmount(), oldResult.getConvertedAmount());

        assertEquals(
                amount.divide(new BigDecimal("1.138400"), 6, RoundingMode.HALF_UP),
                todayResult.getConvertedAmount()
        );
        assertEquals(
                amount.divide(new BigDecimal("1.134500"), 6, RoundingMode.HALF_UP),
                yesterdayResult.getConvertedAmount()
        );
        assertEquals(
                amount.divide(new BigDecimal("1.128900"), 6, RoundingMode.HALF_UP),
                oldResult.getConvertedAmount()
        );
    }

    @Test
    void convertCurrency_ShouldThrowException_WhenRateNotInDatabase() {
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate futureDate = today.plusDays(10);

        ExchangeRateNotFoundException exception = assertThrows(
                ExchangeRateNotFoundException.class,
                () -> exchangeRateService.convertCurrency(amount, "SOMETHING", futureDate)
        );

        assertTrue(exception.getMessage().contains("SOMETHING"));
        assertTrue(exception.getMessage().contains(futureDate.toString()));
    }
}
