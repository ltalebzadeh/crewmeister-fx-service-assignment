package com.crewmeister.cmcodingchallenge.initilization;

import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final ExchangeRateService exchangeRateService;

    public DataInitializer(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing application data...");

        exchangeRateService.fetchAndStoreExchangeRates();

        logger.info("Data initialization completed");
    }
}
