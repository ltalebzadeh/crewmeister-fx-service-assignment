package com.crewmeister.cmcodingchallenge.client;

import com.crewmeister.cmcodingchallenge.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class EcbXmlParser {

    private static final Logger logger = LoggerFactory.getLogger(EcbXmlParser.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final XmlMapper xmlMapper;

    public EcbXmlParser() {
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        this.xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    /**
     * Parse ECB XML response and convert to BundesbankResponse
     */
    public BundesbankResponse parseEcbXml(String xmlContent) {
        logger.debug("Starting ECB XML parsing");

        // Input validation
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            logger.warn("Empty XML content received");
            return BundesbankResponse.failure("Empty XML content");
        }

        // Basic ECB format validation
        String trimmedContent = xmlContent.trim();
        if (!trimmedContent.contains("Envelope") || !trimmedContent.contains("Cube")) {
            logger.warn("XML content doesn't appear to be ECB format");
            return BundesbankResponse.failure("Invalid ECB XML format");
        }

        // Additional validation for ECB-specific elements
        if (!trimmedContent.contains("European Central Bank") && !trimmedContent.contains("currency=")) {
            logger.warn("XML content missing ECB-specific elements");
            return BundesbankResponse.failure("Content doesn't match ECB exchange rate format");
        }

        try {
            logger.debug("Parsing XML with Jackson XmlMapper");

            // Parse XML using Jackson
            EcbResponse ecbResponse = xmlMapper.readValue(xmlContent, EcbResponse.class);

            if (ecbResponse == null) {
                logger.error("Jackson returned null EcbResponse");
                return BundesbankResponse.failure("Failed to parse XML: null response");
            }

            // Convert ECB response to internal format
            BundesbankResponse response = convertToBundesbankResponse(ecbResponse);

            // Validate the conversion result
            if (response.hasData()) {
                logger.info("Successfully parsed {} exchange rates for {} from ECB",
                        response.getCurrencyCount(), response.getDate());
                return response;
            } else {
                logger.warn("Parsed XML successfully but no exchange rate data found");
                return BundesbankResponse.failure("No exchange rate data found in response");
            }

        } catch (JsonProcessingException e) {
            logger.error("JSON/XML processing error during ECB XML parsing", e);
            return BundesbankResponse.failure("XML parsing failed: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during ECB XML parsing", e);
            return BundesbankResponse.failure("IO error while parsing XML: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during ECB XML parsing", e);
            return BundesbankResponse.failure("Unexpected parsing error: " + e.getMessage());
        }
    }

    /**
     * Convert ECB response to internal BundesbankResponse format
     */
    private BundesbankResponse convertToBundesbankResponse(EcbResponse ecbResponse) {
        logger.debug("Converting EcbResponse to BundesbankResponse");

        BundesbankResponse response = BundesbankResponse.success(LocalDate.now(), "ECB");

        try {
            if (ecbResponse.getCube() == null) {
                logger.warn("ECB response missing outer cube");
                return response;
            }

            EcbOuterCube outerCube = ecbResponse.getCube();
            if (outerCube.getTimeCube() == null) {
                logger.warn("ECB response missing time cube");
                return response;
            }

            EcbTimeCube timeCube = outerCube.getTimeCube();

            parseAndSetDate(timeCube, response);

            parseAndSetCurrencyRates(timeCube, response);

            if (response.getRates().isEmpty()) {
                logger.warn("No currency rates found in ECB response");
            } else {
                logger.debug("Successfully converted {} currency rates", response.getRates().size());
            }

        } catch (Exception e) {
            logger.error("Error during ECB response conversion", e);
            return BundesbankResponse.failure("Failed to convert ECB response: " + e.getMessage());
        }

        return response;
    }

    /**
     * Parse and set the date from ECB time cube
     */
    private void parseAndSetDate(EcbTimeCube timeCube, BundesbankResponse response) {
        if (timeCube.getTime() != null && !timeCube.getTime().trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(timeCube.getTime().trim(), DATE_FORMATTER);
                response.setDate(date);
                logger.debug("Parsed date: {}", date);
            } catch (Exception e) {
                logger.warn("Failed to parse date: '{}', using current date. Error: {}",
                        timeCube.getTime(), e.getMessage());
                response.setDate(LocalDate.now());
            }
        } else {
            logger.warn("No date found in ECB response, using current date");
            response.setDate(LocalDate.now());
        }
    }

    /**
     * Parse and set currency rates from ECB currency cubes
     */
    private void parseAndSetCurrencyRates(EcbTimeCube timeCube, BundesbankResponse response) {
        if (timeCube.getCurrencyCubes() == null || timeCube.getCurrencyCubes().isEmpty()) {
            logger.warn("No currency cubes found in ECB response");
            return;
        }

        int successCount = 0;
        int errorCount = 0;

        for (EcbCurrencyCube currencyCube : timeCube.getCurrencyCubes()) {
            try {
                String currencyCode = currencyCube.getCurrency();
                String rateStr = currencyCube.getRate();

                if (currencyCode == null || currencyCode.trim().isEmpty()) {
                    logger.warn("Currency cube missing currency code");
                    errorCount++;
                    continue;
                }

                if (rateStr == null || rateStr.trim().isEmpty()) {
                    logger.warn("Currency cube missing rate for currency: {}", currencyCode);
                    errorCount++;
                    continue;
                }

                BigDecimal rate = new BigDecimal(rateStr.trim());

                if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                    logger.warn("Invalid rate value for {}: {} (must be positive)", currencyCode, rate);
                    errorCount++;
                    continue;
                }

                String currencyName = getCurrencyName(currencyCode.trim().toUpperCase());
                response.addCurrency(currencyCode.trim().toUpperCase(), currencyName, rate);

                logger.debug("Added currency: {} ({}) = {}", currencyCode, currencyName, rate);
                successCount++;

            } catch (NumberFormatException e) {
                logger.warn("Failed to parse rate as number for currency {}: '{}'. Error: {}",
                        currencyCube.getCurrency(), currencyCube.getRate(), e.getMessage());
                errorCount++;
            } catch (Exception e) {
                logger.warn("Failed to process currency rate: {} = {}. Error: {}",
                        currencyCube.getCurrency(), currencyCube.getRate(), e.getMessage());
                errorCount++;
            }
        }

        logger.info("Currency parsing completed: {} successful, {} errors", successCount, errorCount);

        if (successCount == 0 && errorCount > 0) {
            logger.error("Failed to parse any currency rates from {} cubes", errorCount);
        }
    }

    /**
     * Get currency display name by ISO code
     */
    private String getCurrencyName(String currencyCode) {
        if (currencyCode == null) {
            return "Unknown Currency";
        }

        Map<String, String> currencyNames = getCurrencyNameMapping();
        return currencyNames.getOrDefault(currencyCode.toUpperCase(), currencyCode + " Currency");
    }

    /**
     * Currency code to name mapping
     */
    private Map<String, String> getCurrencyNameMapping() {
        Map<String, String> currencyNames = new HashMap<>();

        currencyNames.put("USD", "US Dollar");
        currencyNames.put("JPY", "Japanese Yen");
        currencyNames.put("GBP", "British Pound Sterling");
        currencyNames.put("CHF", "Swiss Franc");
        currencyNames.put("CAD", "Canadian Dollar");
        currencyNames.put("AUD", "Australian Dollar");
        currencyNames.put("NZD", "New Zealand Dollar");
        currencyNames.put("BGN", "Bulgarian Lev");
        currencyNames.put("CZK", "Czech Koruna");
        currencyNames.put("DKK", "Danish Krone");
        currencyNames.put("HUF", "Hungarian Forint");
        currencyNames.put("PLN", "Polish Zloty");
        currencyNames.put("RON", "Romanian Leu");
        currencyNames.put("SEK", "Swedish Krona");
        currencyNames.put("NOK", "Norwegian Krone");
        currencyNames.put("ISK", "Icelandic Krona");
        currencyNames.put("TRY", "Turkish Lira");
        currencyNames.put("CNY", "Chinese Yuan Renminbi");
        currencyNames.put("HKD", "Hong Kong Dollar");
        currencyNames.put("IDR", "Indonesian Rupiah");
        currencyNames.put("INR", "Indian Rupee");
        currencyNames.put("KRW", "South Korean Won");
        currencyNames.put("MYR", "Malaysian Ringgit");
        currencyNames.put("PHP", "Philippine Peso");
        currencyNames.put("SGD", "Singapore Dollar");
        currencyNames.put("THB", "Thai Baht");
        currencyNames.put("BRL", "Brazilian Real");
        currencyNames.put("ILS", "Israeli New Shekel");
        currencyNames.put("MXN", "Mexican Peso");
        currencyNames.put("ZAR", "South African Rand");

        return currencyNames;
    }
}