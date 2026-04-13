package com.example.expensetrackeradmin.helpers;

public class CurrencyHelper {
    
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_EUR = "EUR";
    public static final String CURRENCY_VND = "VND";
    public static final String CURRENCY_GBP = "GBP";
    public static final String CURRENCY_JPY = "JPY";
    
    private static final double RATE_EUR_TO_USD = 1.09;
    private static final double RATE_VND_TO_USD = 0.000039;
    private static final double RATE_GBP_TO_USD = 1.27;
    private static final double RATE_JPY_TO_USD = 0.0067;
    
    public static double convertToUsd(double amount, String currency) {
        if (currency == null || currency.isEmpty()) {
            return amount;
        }
        
        switch (currency.toUpperCase()) {
            case CURRENCY_USD:
                return amount;
            case CURRENCY_EUR:
                return amount * RATE_EUR_TO_USD;
            case CURRENCY_VND:
                return amount * RATE_VND_TO_USD;
            case CURRENCY_GBP:
                return amount * RATE_GBP_TO_USD;
            case CURRENCY_JPY:
                return amount * RATE_JPY_TO_USD;
            default:
                return amount;
        }
    }
    
    public static String[] getSupportedCurrencies() {
        return new String[]{CURRENCY_USD, CURRENCY_EUR, CURRENCY_VND, CURRENCY_GBP, CURRENCY_JPY};
    }
}