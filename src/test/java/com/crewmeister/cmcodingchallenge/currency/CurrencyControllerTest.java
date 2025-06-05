package com.crewmeister.cmcodingchallenge.currency;

import com.crewmeister.cmcodingchallenge.dto.CurrencyDto;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDto;
import com.crewmeister.cmcodingchallenge.exception.ExchangeRateNotFoundException;
import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerGetCurrenciesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    void getCurrencies_ShouldReturnCurrencyList_WhenCurrenciesExist() throws Exception {
        List<CurrencyDto> expectedCurrencies = Arrays.asList(
                new CurrencyDto("USD", "US Dollar"),
                new CurrencyDto("EUR", "Euro"),
                new CurrencyDto("GBP", "British Pound"),
                new CurrencyDto("JPY", "Japanese Yen")
        );

        when(currencyService.getAllCurrencies()).thenReturn(expectedCurrencies);

        mockMvc.perform(get("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].code", is("USD")))
                .andExpect(jsonPath("$[0].name", is("US Dollar")))
                .andExpect(jsonPath("$[1].code", is("EUR")))
                .andExpect(jsonPath("$[1].name", is("Euro")))
                .andExpect(jsonPath("$[2].code", is("GBP")))
                .andExpect(jsonPath("$[2].name", is("British Pound")))
                .andExpect(jsonPath("$[3].code", is("JPY")))
                .andExpect(jsonPath("$[3].name", is("Japanese Yen")));

        verify(currencyService, times(1)).getAllCurrencies();
    }

    @Test
    void getCurrencies_ShouldReturnCorrectJsonStructure() throws Exception {
        List<CurrencyDto> currencies = Arrays.asList(
                new CurrencyDto("USD", "US Dollar"),
                new CurrencyDto("EUR", "Euro")
        );

        when(currencyService.getAllCurrencies()).thenReturn(currencies);

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("USD", "EUR")))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("US Dollar", "Euro")))
                .andExpect(jsonPath("$[*]", everyItem(hasKey("code"))))
                .andExpect(jsonPath("$[*]", everyItem(hasKey("name"))));

        verify(currencyService, times(1)).getAllCurrencies();
    }

    @Test
    void getAllExchangeRates_ShouldReturnExchangeRateList_WhenRatesExist() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<ExchangeRateDto> expectedRates = Arrays.asList(
                new ExchangeRateDto("USD", "US Dollar", today, new BigDecimal("1.0500")),
                new ExchangeRateDto("GBP", "British Pound", today, new BigDecimal("0.8500")),
                new ExchangeRateDto("JPY", "Japanese Yen", today, new BigDecimal("130.2500")),
                new ExchangeRateDto("USD", "US Dollar", yesterday, new BigDecimal("1.0480")),
                new ExchangeRateDto("GBP", "British Pound", yesterday, new BigDecimal("0.8520"))
        );

        when(exchangeRateService.getAllExchangeRates()).thenReturn(expectedRates);

        mockMvc.perform(get("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))

                .andExpect(jsonPath("$[0].currency_code", is("USD")))
                .andExpect(jsonPath("$[0].currency_name", is("US Dollar")))
                .andExpect(jsonPath("$[0].date", is(today.toString())))
                .andExpect(jsonPath("$[0].rate", is(1.0500)))

                .andExpect(jsonPath("$[1].currency_code", is("GBP")))
                .andExpect(jsonPath("$[1].currency_name", is("British Pound")))
                .andExpect(jsonPath("$[1].date", is(today.toString())))
                .andExpect(jsonPath("$[1].rate", is(0.8500)))

                .andExpect(jsonPath("$[2].currency_code", is("JPY")))
                .andExpect(jsonPath("$[2].currency_name", is("Japanese Yen")))
                .andExpect(jsonPath("$[2].date", is(today.toString())))
                .andExpect(jsonPath("$[2].rate", is(130.2500)))

                .andExpect(jsonPath("$[3].currency_code", is("USD")))
                .andExpect(jsonPath("$[3].date", is(yesterday.toString())))
                .andExpect(jsonPath("$[3].rate", is(1.0480)));

        verify(exchangeRateService, times(1)).getAllExchangeRates();
    }

    @Test
    void getAllExchangeRates_ShouldFormatDatesCorrectly() throws Exception {
        LocalDate date1 = LocalDate.of(2023, 12, 15);
        LocalDate date2 = LocalDate.of(2023, 1, 5);

        List<ExchangeRateDto> rates = Arrays.asList(
                new ExchangeRateDto("USD", "US Dollar", date1, new BigDecimal("1.0500")),
                new ExchangeRateDto("GBP", "British Pound", date2, new BigDecimal("0.8500"))
        );

        when(exchangeRateService.getAllExchangeRates()).thenReturn(rates);

        mockMvc.perform(get("/api/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date", is("2023-12-15")))
                .andExpect(jsonPath("$[1].date", is("2023-01-05")));

        verify(exchangeRateService, times(1)).getAllExchangeRates();
    }

    @Test
    void getExchangeRate_ShouldHandleDifferentCurrencies() throws Exception {
        // Given
        String currencyCode = "GBP";
        LocalDate date = LocalDate.of(2023, 12, 15);
        ExchangeRateDto expectedRate = new ExchangeRateDto(
                "GBP", "British Pound", date, new BigDecimal("0.8500"));

        when(exchangeRateService.getExchangeRate(currencyCode, date)).thenReturn(expectedRate);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/{currency}/{date}", currencyCode, date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency_code", is("GBP")))
                .andExpect(jsonPath("$.currency_name", is("British Pound")))
                .andExpect(jsonPath("$.rate", is(0.8500)));

        verify(exchangeRateService, times(1)).getExchangeRate(currencyCode, date);
    }

    @Test
    void getExchangeRate_ShouldReturn404_WhenExchangeRateNotFound() throws Exception {
        // Given
        String currencyCode = "USD";
        LocalDate date = LocalDate.of(1999, 1, 1);

        when(exchangeRateService.getExchangeRate(currencyCode, date))
                .thenThrow(new ExchangeRateNotFoundException(currencyCode, date));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/{currency}/{date}", currencyCode, date))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Exchange Rate Not Found")))
                .andExpect(jsonPath("$.message", containsString("USD")))
                .andExpect(jsonPath("$.message", containsString("1999-01-01")));

        verify(exchangeRateService, times(1)).getExchangeRate(currencyCode, date);
    }

    @Test
    void convertCurrency_ShouldReturnConversion_WhenValidParameters() throws Exception {
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        LocalDate date = LocalDate.of(2023, 12, 15);

        CurrencyConversionRates expectedConversion = new CurrencyConversionRates(
                amount, "USD", new BigDecimal("95.238095"), "EUR",
                new BigDecimal("1.050000"), date
        );

        when(exchangeRateService.convertCurrency(amount, currency, date))
                .thenReturn(expectedConversion);

        mockMvc.perform(get("/api/convert/{amount}/{currency}/{date}", amount, currency, date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.original_amount", is(100.00)))
                .andExpect(jsonPath("$.original_currency", is("USD")))
                .andExpect(jsonPath("$.converted_amount", is(95.238095)))
                .andExpect(jsonPath("$.target_currency", is("EUR")))
                .andExpect(jsonPath("$.exchange_rate", is(1.050000)))
                .andExpect(jsonPath("$.conversion_date", is("2023-12-15")));

        verify(exchangeRateService, times(1)).convertCurrency(amount, currency, date);
    }

    @Test
    void convertCurrency_ShouldReturn422_WhenArithmeticException() throws Exception {
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        LocalDate date = LocalDate.of(2023, 12, 15);

        when(exchangeRateService.convertCurrency(amount, currency, date))
                .thenThrow(new ArithmeticException("Non-terminating decimal expansion"));

        mockMvc.perform(get("/api/convert/{amount}/{currency}/{date}", amount, currency, date))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.error", is("Currency Conversion Error")))
                .andExpect(jsonPath("$.message", containsString("arithmetic error")))
                .andExpect(jsonPath("$.message", containsString("Non-terminating decimal expansion")));

        verify(exchangeRateService, times(1)).convertCurrency(amount, currency, date);
    }
}