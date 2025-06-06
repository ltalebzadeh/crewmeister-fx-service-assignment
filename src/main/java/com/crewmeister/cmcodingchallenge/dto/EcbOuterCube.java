package com.crewmeister.cmcodingchallenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EcbOuterCube {

    @JacksonXmlProperty(localName = "Cube")
    private EcbTimeCube timeCube;
}