package com.crewmeister.cmcodingchallenge.currency;

import com.crewmeister.cmcodingchallenge.dto.CurrencyDto;
import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
}