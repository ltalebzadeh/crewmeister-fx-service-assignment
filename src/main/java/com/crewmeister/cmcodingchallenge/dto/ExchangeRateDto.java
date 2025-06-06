package com.crewmeister.cmcodingchallenge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDto {

    @JsonProperty("currency_code")
    @NotBlank(message = "Currency code cannot be blank")
    private String currencyCode;

    @JsonProperty("currency_name")
    @NotBlank(message = "Currency name cannot be blank")
    private String currencyName;

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Date cannot be null")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;

    @JsonProperty("rate")
    @NotNull(message = "Exchange rate cannot be null")
    @Positive(message = "Exchange rate must be positive")
    private BigDecimal rate;
}
