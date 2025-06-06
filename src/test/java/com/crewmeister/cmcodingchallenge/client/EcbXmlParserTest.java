// EcbXmlParserTest.java
package com.crewmeister.cmcodingchallenge.client;

import com.crewmeister.cmcodingchallenge.dto.BundesbankResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EcbXmlParserTest {

    private final EcbXmlParser parser = new EcbXmlParser();

    @Test
    void parseEcbXml_ShouldParseCorrectly() {
        String ecbXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<gesmes:Envelope xmlns:gesmes=\"http://www.gesmes.org/xml/2002-08-01\" xmlns=\"http://www.ecb.int/vocabulary/2002-08-01/eurofxref\">" +
                "<gesmes:subject>Reference rates</gesmes:subject>" +
                "<gesmes:Sender>" +
                "<gesmes:name>European Central Bank</gesmes:name>" +
                "</gesmes:Sender>" +
                "<Cube>" +
                "<Cube time=\"2023-12-15\">" +
                "<Cube currency=\"USD\" rate=\"1.1411\"/>" +
                "<Cube currency=\"JPY\" rate=\"164.62\"/>" +
                "<Cube currency=\"GBP\" rate=\"0.84260\"/>" +
                "<Cube currency=\"CHF\" rate=\"0.9383\"/>" +
                "</Cube>" +
                "</Cube>" +
                "</gesmes:Envelope>";

        BundesbankResponse result = parser.parseEcbXml(ecbXml);

        assertTrue(result.isSuccessful());
        assertTrue(result.hasData());
        assertEquals(LocalDate.of(2023, 12, 15), result.getDate());
        assertEquals("ECB", result.getDataSource());
        assertEquals(4, result.getCurrencyCount());

        assertEquals(new BigDecimal("1.1411"), result.getRate("USD").orElse(null));
        assertEquals(new BigDecimal("164.62"), result.getRate("JPY").orElse(null));
        assertEquals(new BigDecimal("0.84260"), result.getRate("GBP").orElse(null));
        assertEquals(new BigDecimal("0.9383"), result.getRate("CHF").orElse(null));

        assertEquals("US Dollar", result.getCurrencyName("USD").orElse(null));
        assertEquals("Japanese Yen", result.getCurrencyName("JPY").orElse(null));
        assertEquals("British Pound Sterling", result.getCurrencyName("GBP").orElse(null));
        assertEquals("Swiss Franc", result.getCurrencyName("CHF").orElse(null));
    }

    @Test
    void parseEcbXml_ShouldHandleMalformedXml() {
        String malformedXml = "<invalid>xml</content>";

        BundesbankResponse result = parser.parseEcbXml(malformedXml);

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertTrue(result.getErrorMessage().contains("Invalid ECB XML format"));
    }

    @Test
    void parseEcbXml_ShouldHandleEmptyXml() {
        BundesbankResponse result = parser.parseEcbXml("");

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertEquals("Empty XML content", result.getErrorMessage());
    }
}