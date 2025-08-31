package com.mize.domain;

import java.util.Map;

import lombok.Getter;

@Getter
public class ExchangeRateList {

    public ExchangeRateList(String base, Map<String, Double> rates){
        this.baseCurrency = base;
        this.rates = rates;
    }
    private  String baseCurrency = null;
    private final  Map<String, Double> rates;

    @Override
    public String toString() {
        return "ExchangeRateList{" +
                "baseCurrency='" + baseCurrency + '\'' +
                ", rates=" + rates.size() + " currencies" +
                '}';
    }
}