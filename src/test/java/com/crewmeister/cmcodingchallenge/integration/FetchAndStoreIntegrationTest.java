package com.crewmeister.cmcodingchallenge.integration;

import com.crewmeister.cmcodingchallenge.client.BundesbankApiClient;
import com.crewmeister.cmcodingchallenge.dto.BundesbankResponse;
import com.crewmeister.cmcodingchallenge.entity.Currency;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.repository.CurrencyRepository;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class FetchAndStoreIntegrationTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @MockBean
    private BundesbankApiClient bundesbankApiClient;

    private LocalDate today;

    private BundesbankResponse successfulResponse;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2023, 12, 15);

        exchangeRateRepository.deleteAll();
        currencyRepository.deleteAll();

        successfulResponse = BundesbankResponse.success(today, "ECB");
        successfulResponse.addCurrency("USD", "US Dollar", new BigDecimal("1.1411"));
        successfulResponse.addCurrency("GBP", "British Pound Sterling", new BigDecimal("0.8426"));
        successfulResponse.addCurrency("JPY", "Japanese Yen", new BigDecimal("164.62"));
    }

    @AfterEach
    void tearDown() {
        exchangeRateRepository.deleteAll();
        currencyRepository.deleteAll();
    }

    @Test
    void fetchAndStoreExchangeRates_ShouldStoreNewData() throws InterruptedException {
        when(bundesbankApiClient.getExchangeRates()).thenReturn(Mono.just(successfulResponse));

        exchangeRateService.fetchAndStoreExchangeRates();
        Thread.sleep(500);

        List<Currency> currencies = currencyRepository.findAll();
        assertEquals(3, currencies.size());

        Currency usdCurrency = currencyRepository.findByCode("USD").orElse(null);
        assertNotNull(usdCurrency);
        assertEquals("USD", usdCurrency.getCode());
        assertEquals("US Dollar", usdCurrency.getName());

        Currency gbpCurrency = currencyRepository.findByCode("GBP").orElse(null);
        assertNotNull(gbpCurrency);
        assertEquals("GBP", gbpCurrency.getCode());
        assertEquals("British Pound Sterling", gbpCurrency.getName());

        Currency jpyCurrency = currencyRepository.findByCode("JPY").orElse(null);
        assertNotNull(jpyCurrency);
        assertEquals("JPY", jpyCurrency.getCode());
        assertEquals("Japanese Yen", jpyCurrency.getName());

        List<ExchangeRate> exchangeRates = exchangeRateRepository.findAll();
        assertEquals(3, exchangeRates.size());

        Optional<ExchangeRate> usdRate = exchangeRateRepository.findByCurrencyCodeAndRateDate("USD", today);
        assertTrue(usdRate.isPresent());
        assertEquals(new BigDecimal("1.141100"), usdRate.get().getRate());
        assertEquals(today, usdRate.get().getRateDate());

        Optional<ExchangeRate> gbpRate = exchangeRateRepository.findByCurrencyCodeAndRateDate("GBP", today);
        assertTrue(gbpRate.isPresent());
        assertEquals(new BigDecimal("0.842600"), gbpRate.get().getRate());

        Optional<ExchangeRate> jpyRate = exchangeRateRepository.findByCurrencyCodeAndRateDate("JPY", today);
        assertTrue(jpyRate.isPresent());
        assertEquals(new BigDecimal("164.620000"), jpyRate.get().getRate());

        verify(bundesbankApiClient).getExchangeRates();
    }

    @Test
    void fetchAndStoreExchangeRates_ShouldUpdateExistingData() throws InterruptedException {
        Currency existingUsd = new Currency("USD", "Old US Dollar");
        Currency existingGbp = new Currency("GBP", "Old British Pound");
        currencyRepository.saveAll(List.of(existingUsd, existingGbp));

        ExchangeRate existingUsdRate = new ExchangeRate(existingUsd, today, new BigDecimal("1.0000"));
        ExchangeRate existingGbpRate = new ExchangeRate(existingGbp, today, new BigDecimal("0.8000"));
        exchangeRateRepository.saveAll(List.of(existingUsdRate, existingGbpRate));

        when(bundesbankApiClient.getExchangeRates()).thenReturn(Mono.just(successfulResponse));

        exchangeRateService.fetchAndStoreExchangeRates();
        Thread.sleep(500);

        Currency updatedUsd = currencyRepository.findByCode("USD").orElse(null);
        assertNotNull(updatedUsd);
        assertEquals("US Dollar", updatedUsd.getName());

        Currency updatedGbp = currencyRepository.findByCode("GBP").orElse(null);
        assertNotNull(updatedGbp);
        assertEquals("British Pound Sterling", updatedGbp.getName());

        Currency newJpy = currencyRepository.findByCode("JPY").orElse(null);
        assertNotNull(newJpy);
        assertEquals("Japanese Yen", newJpy.getName());

        Optional<ExchangeRate> updatedUsdRate = exchangeRateRepository.findByCurrencyCodeAndRateDate("USD", today);
        assertTrue(updatedUsdRate.isPresent());
        assertEquals(new BigDecimal("1.141100"), updatedUsdRate.get().getRate());

        Optional<ExchangeRate> updatedGbpRate = exchangeRateRepository.findByCurrencyCodeAndRateDate("GBP", today);
        assertTrue(updatedGbpRate.isPresent());
        assertEquals(new BigDecimal("0.842600"), updatedGbpRate.get().getRate());

        Optional<ExchangeRate> newJpyRate = exchangeRateRepository.findByCurrencyCodeAndRateDate("JPY", today);
        assertTrue(newJpyRate.isPresent());
        assertEquals(new BigDecimal("164.620000"), newJpyRate.get().getRate());

        assertEquals(3, currencyRepository.count());
        assertEquals(3, exchangeRateRepository.count());
    }

    @Test
    void fetchAndStoreExchangeRates_ShouldHandleMultipleDates() throws InterruptedException {
        LocalDate firstDate = LocalDate.of(2025, 6, 4);
        BundesbankResponse firstResponse = BundesbankResponse.success(firstDate, "ECB");
        firstResponse.addCurrency("USD", "US Dollar", new BigDecimal("1.1411"));

        LocalDate secondDate = LocalDate.of(2025, 5, 14);
        BundesbankResponse secondResponse = BundesbankResponse.success(secondDate, "ECB");
        secondResponse.addCurrency("USD", "US Dollar", new BigDecimal("1.1313"));

        when(bundesbankApiClient.getExchangeRates()).thenReturn(Mono.just(firstResponse));
        exchangeRateService.fetchAndStoreExchangeRates();
        Thread.sleep(500);

        when(bundesbankApiClient.getExchangeRates()).thenReturn(Mono.just(secondResponse));
        exchangeRateService.fetchAndStoreExchangeRates();
        Thread.sleep(500);

        assertEquals(1, currencyRepository.count());
        assertEquals(2, exchangeRateRepository.count());

        Optional<ExchangeRate> firstDateRate = exchangeRateRepository.findByCurrencyCodeAndRateDate("USD", firstDate);
        assertTrue(firstDateRate.isPresent());
        assertEquals(new BigDecimal("1.141100"), firstDateRate.get().getRate());

        Optional<ExchangeRate> secondDateRate = exchangeRateRepository.findByCurrencyCodeAndRateDate("USD", secondDate);
        assertTrue(secondDateRate.isPresent());
        assertEquals(new BigDecimal("1.131300"), secondDateRate.get().getRate());
    }

    @Test
    void fetchAndStoreExchangeRates_ShouldNotStoreOnFailure() throws InterruptedException {
        BundesbankResponse failedResponse = BundesbankResponse.failure("API timeout");
        when(bundesbankApiClient.getExchangeRates()).thenReturn(Mono.just(failedResponse));

        exchangeRateService.fetchAndStoreExchangeRates();
        Thread.sleep(500);

        assertEquals(0, currencyRepository.count());
        assertEquals(0, exchangeRateRepository.count());

        verify(bundesbankApiClient).getExchangeRates();
    }
}
