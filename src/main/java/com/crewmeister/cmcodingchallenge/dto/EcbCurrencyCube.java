package com.crewmeister.cmcodingchallenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EcbCurrencyCube {

    @JacksonXmlProperty(isAttribute = true, localName = "currency")
    private String currency;

    @JacksonXmlProperty(isAttribute = true, localName = "rate")
    private String rate;
}