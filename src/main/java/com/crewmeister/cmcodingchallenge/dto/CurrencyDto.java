package com.crewmeister.cmcodingchallenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDto {

    @JsonProperty("code")
    @NotBlank(message = "Currency code cannot be blank")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String code;

    @JsonProperty("name")
    @NotBlank(message = "Currency name cannot be blank")
    @Size(max = 100, message = "Currency name cannot exceed 100 characters")
    private String name;
}
