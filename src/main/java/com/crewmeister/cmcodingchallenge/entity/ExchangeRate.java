package com.crewmeister.cmcodingchallenge.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates")
@Data
@NoArgsConstructor
public class ExchangeRate {
    @Id
    private Long id;

    @ManyToOne
    private Currency currency;

    private LocalDate rateDate;
    private BigDecimal rate;

    public ExchangeRate(Currency currency, LocalDate rateDate, BigDecimal rate) {
        this.currency = currency;
        this.rateDate = rateDate;
        this.rate = rate;
    }
}
