package com.mize.domain;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class ExchangeRateList {

    public ExchangeRateList(String base, Map<String, Double> rates, LocalDateTime timestamp) {
        this.baseCurrency = base;
        this.rates = rates;
        this.timestamp = timestamp;
    }
    private  String baseCurrency = null;
    private  Map<String, Double> rates;
    private  LocalDateTime timestamp;

    @Override
    public String toString() {
        return "ExchangeRateList{" +
                "baseCurrency='" + baseCurrency + '\'' +
                ", rates=" + rates.size() + " currencies" +
                ", timestamp=" + timestamp +
                '}';
    }
}