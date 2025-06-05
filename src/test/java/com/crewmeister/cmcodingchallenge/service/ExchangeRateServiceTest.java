package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.currency.CurrencyConversionRates;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDto;
import com.crewmeister.cmcodingchallenge.entity.Currency;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import com.crewmeister.cmcodingchallenge.exception.ExchangeRateNotFoundException;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private Currency usdCurrency;
    private Currency gbpCurrency;
    private Currency jpyCurrency;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        usdCurrency = new Currency("USD", "US Dollar");
        gbpCurrency = new Currency("GBP", "British Pound");
        jpyCurrency = new Currency("JPY", "Japanese Yen");
        testDate = LocalDate.of(2025, 6, 4);
    }

    @Test
    void getAllExchangeRates_ShouldReturnAllRates() {
        List<ExchangeRate> mockRates = Arrays.asList(
                new ExchangeRate(usdCurrency, testDate, new BigDecimal("1.1384")),
                new ExchangeRate(gbpCurrency, testDate, new BigDecimal("0.84210")),
                new ExchangeRate(jpyCurrency, testDate, new BigDecimal("164.15")),
                new ExchangeRate(usdCurrency, testDate.minusDays(1), new BigDecimal("1.1345"))
        );
        when(exchangeRateRepository.findAllWithCurrency()).thenReturn(mockRates);

        List<ExchangeRateDto> result = exchangeRateService.getAllExchangeRates();

        assertNotNull(result);
        assertEquals(4, result.size());

        ExchangeRateDto usdRate = result.get(0);
        assertEquals("USD", usdRate.getCurrencyCode());
        assertEquals("US Dollar", usdRate.getCurrencyName());
        assertEquals(testDate, usdRate.getDate());
        assertEquals(new BigDecimal("1.1384"), usdRate.getRate());

        ExchangeRateDto gbpRate = result.get(1);
        assertEquals("GBP", gbpRate.getCurrencyCode());
        assertEquals("British Pound", gbpRate.getCurrencyName());
        assertEquals(new BigDecimal("0.84210"), gbpRate.getRate());

        ExchangeRateDto jpyRate = result.get(2);
        assertEquals("JPY", jpyRate.getCurrencyCode());
        assertEquals("Japanese Yen", jpyRate.getCurrencyName());
        assertEquals(new BigDecimal("164.15"), jpyRate.getRate());

        ExchangeRateDto historicalRate = result.get(3);
        assertEquals("USD", historicalRate.getCurrencyCode());
        assertEquals(testDate.minusDays(1), historicalRate.getDate());
        assertEquals(new BigDecimal("1.1345"), historicalRate.getRate());

        verify(exchangeRateRepository, times(1)).findAllWithCurrency();
    }

    @Test
    void getExchangeRate_ShouldReturnRate_WhenFoundInDatabase() {
        LocalDate yesterday = testDate.minusDays(1);
        String gbpCode = "GBP";
        ExchangeRate gbpRate = new ExchangeRate(gbpCurrency, testDate, new BigDecimal("0.8500"));
        String jpyCode = "JPY";
        ExchangeRate jpyRate = new ExchangeRate(jpyCurrency, yesterday, new BigDecimal("130.2500"));
        when(exchangeRateRepository.findByCurrencyCodeAndDate(gbpCode, testDate))
                .thenReturn(Optional.of(gbpRate));
        when(exchangeRateRepository.findByCurrencyCodeAndDate(jpyCode, yesterday))
                .thenReturn(Optional.of(jpyRate));

        ExchangeRateDto gbpResult = exchangeRateService.getExchangeRate(gbpCode, testDate);
        ExchangeRateDto jpyResult = exchangeRateService.getExchangeRate(jpyCode, yesterday);

        assertEquals("GBP", gbpResult.getCurrencyCode());
        assertEquals("British Pound", gbpResult.getCurrencyName());
        assertEquals(new BigDecimal("0.8500"), gbpResult.getRate());

        assertEquals("JPY", jpyResult.getCurrencyCode());
        assertEquals("Japanese Yen", jpyResult.getCurrencyName());
        assertEquals(new BigDecimal("130.2500"), jpyResult.getRate());

        verify(exchangeRateRepository, times(1)).findByCurrencyCodeAndDate(gbpCode, testDate);
        verify(exchangeRateRepository, times(1)).findByCurrencyCodeAndDate(jpyCode, yesterday);
    }

    @Test
    void convertCurrency_ShouldHandleEurToEurConversion() {
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "EUR";

        CurrencyConversionRates result = exchangeRateService.convertCurrency(amount, currency, testDate);

        assertNotNull(result);
        assertEquals(amount, result.getOriginalAmount());
        assertEquals("EUR", result.getOriginalCurrency());
        assertEquals(amount, result.getConvertedAmount());
        assertEquals("EUR", result.getTargetCurrency());
        assertEquals(BigDecimal.ONE, result.getExchangeRate());
        assertEquals(testDate, result.getDate());

        verifyNoInteractions(exchangeRateRepository);
    }

    @Test
    void convertCurrency_ShouldConvertUsdToEur_Correctly() {
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        ExchangeRate exchangeRate = new ExchangeRate(usdCurrency, testDate, new BigDecimal("1.1384"));
        when(exchangeRateRepository.findByCurrencyCodeAndDate("USD", testDate))
                .thenReturn(Optional.of(exchangeRate));

        CurrencyConversionRates result = exchangeRateService.convertCurrency(amount, currency, testDate);

        assertNotNull(result);
        assertEquals(amount, result.getOriginalAmount());
        assertEquals("USD", result.getOriginalCurrency());
        assertEquals("EUR", result.getTargetCurrency());
        assertEquals(new BigDecimal("1.1384"), result.getExchangeRate());
        assertEquals(testDate, result.getDate());

        // calculation: 100 / 1.1384 = 0.072254
        BigDecimal expectedConverted = amount.divide(new BigDecimal("1.1384"), 6, RoundingMode.HALF_UP);
        assertEquals(expectedConverted, result.getConvertedAmount());

        verify(exchangeRateRepository, times(1)).findByCurrencyCodeAndDate("USD", testDate);
    }

    @Test
    void convertCurrency_ShouldConvertJpyToEur_WithLargeRate() {
        BigDecimal amount = new BigDecimal("16415.00");
        String currency = "JPY";
        ExchangeRate exchangeRate = new ExchangeRate(jpyCurrency, testDate, new BigDecimal("164.15"));
        when(exchangeRateRepository.findByCurrencyCodeAndDate("JPY", testDate))
                .thenReturn(Optional.of(exchangeRate));

        CurrencyConversionRates result = exchangeRateService.convertCurrency(amount, currency, testDate);

        assertNotNull(result);
        assertEquals(amount, result.getOriginalAmount());
        assertEquals("JPY", result.getOriginalCurrency());
        assertEquals("EUR", result.getTargetCurrency());
        assertEquals(new BigDecimal("164.15"), result.getExchangeRate());

        // calculation: 16415 / 164.15 = 100.000000
        BigDecimal expectedConverted = amount.divide(new BigDecimal("164.15"), 6, RoundingMode.HALF_UP);
        assertEquals(expectedConverted, result.getConvertedAmount());

        verify(exchangeRateRepository, times(1)).findByCurrencyCodeAndDate("JPY", testDate);
    }

    @Test
    void convertCurrency_ShouldThrowException_WhenExchangeRateNotFound() {
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "SOMETHING";
        when(exchangeRateRepository.findByCurrencyCodeAndDate("SOMETHING", testDate))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        ExchangeRateNotFoundException exception = assertThrows(
                ExchangeRateNotFoundException.class,
                () -> exchangeRateService.convertCurrency(amount, currency, testDate)
        );

        assertTrue(exception.getMessage().contains("SOMETHING"));
        assertTrue(exception.getMessage().contains(testDate.toString()));

        verify(exchangeRateRepository, times(1)).findByCurrencyCodeAndDate("SOMETHING", testDate);
    }
}