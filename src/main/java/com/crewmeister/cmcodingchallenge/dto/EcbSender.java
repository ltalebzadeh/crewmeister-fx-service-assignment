package com.crewmeister.cmcodingchallenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EcbSender {

    @JacksonXmlProperty(localName = "name", namespace = "http://www.gesmes.org/xml/2002-08-01")
    private String name;
}