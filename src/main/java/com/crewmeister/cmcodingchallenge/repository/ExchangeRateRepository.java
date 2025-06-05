package com.crewmeister.cmcodingchallenge.repository;

import com.crewmeister.cmcodingchallenge.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query("SELECT er FROM ExchangeRate er JOIN FETCH er.currency ORDER BY er.rateDate DESC, er.currency.code ASC")
    List<ExchangeRate> findAllWithCurrency();

    @Query("SELECT er FROM ExchangeRate er JOIN FETCH er.currency WHERE er.currency.code = :currencyCode AND er.rateDate = :date")
    Optional<ExchangeRate> findByCurrencyCodeAndDate(@Param("currencyCode") String currencyCode,
                                                     @Param("date") LocalDate date);

}
