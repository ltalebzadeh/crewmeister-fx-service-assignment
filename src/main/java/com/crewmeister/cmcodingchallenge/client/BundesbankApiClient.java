package com.crewmeister.cmcodingchallenge.client;

import com.crewmeister.cmcodingchallenge.dto.BundesbankResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BundesbankApiClient {

    private static final Logger logger = LoggerFactory.getLogger(BundesbankApiClient.class);

    @Value("${ecb.api.daily-rates-url}")
    private String ECB_DAILY_RATES_URL;

    @Value("${ecb.api.timeout:10000}")
    private int timeoutMs;

    @Value("${ecb.api.retry-attempts:3}")
    private int retryAttempts;

    private final WebClient webClient;
    private final EcbXmlParser xmlParser;

    public BundesbankApiClient(EcbXmlParser xmlParser) {
        this.xmlParser = xmlParser;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    public Mono<BundesbankResponse> getExchangeRates() {
        String url = ECB_DAILY_RATES_URL;
        logger.debug("Fetching exchange rates from ECB: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .filter(content -> content != null && content.contains("Envelope"))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid XML response")))
                .map(xmlParser::parseEcbXml)
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1)))
                .onErrorReturn(BundesbankResponse.failure("ECB API call failed"));
    }
}