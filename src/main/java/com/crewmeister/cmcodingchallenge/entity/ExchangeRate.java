package com.crewmeister.cmcodingchallenge.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_currency_date",
                columnNames = {"currency_code", "rate_date"}
        ))
@Data
@NoArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull(message = "Currency cannot be null")
    private Currency currency;

    @Column(name = "rate_date", nullable = false)
    @NotNull(message = "Rate date cannot be null")
    @PastOrPresent(message = "Rate date cannot be in the future")
    private LocalDate rateDate;

    @Column(precision = 19, scale = 6, nullable = false)
    @NotNull(message = "Exchange rate cannot be null")
    @Positive(message = "Exchange rate must be positive")
    private BigDecimal rate;

    public ExchangeRate(Currency currency, LocalDate rateDate, BigDecimal rate) {
        this.currency = currency;
        this.rateDate = rateDate;
        this.rate = rate;
    }
}