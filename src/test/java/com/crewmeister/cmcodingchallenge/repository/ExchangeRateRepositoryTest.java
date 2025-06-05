package com.crewmeister.cmcodingchallenge.repository;

import com.crewmeister.cmcodingchallenge.entity.Currency;
import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
class ExchangeRateRepositoryTest {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2025, 6, 4);
        LocalDate yesterday = today.minusDays(1);
        LocalDate dayBeforeYesterday = today.minusDays(2);

        Currency usdCurrency = currencyRepository.save(new Currency("USD", "US Dollar"));
        Currency eurCurrency = currencyRepository.save(new Currency("EUR", "Euro"));
        Currency gbpCurrency = currencyRepository.save(new Currency("GBP", "British Pound"));

        exchangeRateRepository.save(new ExchangeRate(usdCurrency, today, new BigDecimal("1.1384")));
        exchangeRateRepository.save(new ExchangeRate(usdCurrency, yesterday, new BigDecimal("1.1345")));
        exchangeRateRepository.save(new ExchangeRate(usdCurrency, dayBeforeYesterday, new BigDecimal("1.1289")));

        exchangeRateRepository.save(new ExchangeRate(gbpCurrency, today, new BigDecimal("0.85200")));
        exchangeRateRepository.save(new ExchangeRate(gbpCurrency, yesterday, new BigDecimal("0.84800")));

        exchangeRateRepository.save(new ExchangeRate(eurCurrency, today, new BigDecimal("1.0000")));
    }

    @Test
    void findAllWithCurrency_ShouldUseJoinFetch() {
        List<ExchangeRate> results = exchangeRateRepository.findAllWithCurrency();

        assertNotNull(results);
        assertEquals(6, results.size());

        for (ExchangeRate rate : results) {
            assertNotNull(rate.getCurrency());
            assertNotNull(rate.getCurrency().getCode());
            assertNotNull(rate.getCurrency().getName());
        }
    }

    @Test
    void findByCurrencyCodeAndDate_ShouldFindExactMatch() {
        Optional<ExchangeRate> result = exchangeRateRepository.findByCurrencyCodeAndDate("USD", today);

        assertTrue(result.isPresent());
        ExchangeRate rate = result.get();
        assertEquals("USD", rate.getCurrency().getCode());
        assertEquals(today, rate.getRateDate());
        assertEquals(new BigDecimal("1.1384"), rate.getRate());
    }

    @Test
    void findByCurrencyCodeAndDate_ShouldReturnEmpty_ForNonExistent() {
        Optional<ExchangeRate> result = exchangeRateRepository.findByCurrencyCodeAndDate("USD", today.plusDays(10));

        assertFalse(result.isPresent());
    }
}