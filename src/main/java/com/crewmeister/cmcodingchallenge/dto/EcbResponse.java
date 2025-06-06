package com.crewmeister.cmcodingchallenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "Envelope")
public class EcbResponse {

    @JacksonXmlProperty(localName = "subject", namespace = "http://www.gesmes.org/xml/2002-08-01")
    private String subject;

    @JacksonXmlProperty(localName = "Sender", namespace = "http://www.gesmes.org/xml/2002-08-01")
    private EcbSender sender;

    @JacksonXmlProperty(localName = "Cube")
    private EcbOuterCube cube;
}