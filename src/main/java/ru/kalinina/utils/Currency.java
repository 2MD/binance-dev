package ru.kalinina.utils;

import java.util.Arrays;

public enum Currency {
    BUSD("BUSD"),
    USDT("USDT"),
    BTC("BTC"),
    BNB("BNB");

    private final String name;

    Currency(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static String extractCurrency(String pair) {
        return Arrays.stream(Currency.values())
                .filter(c -> pair.contains(c.name))
                .map(c -> pair.substring(0, pair.indexOf(c.getName())) + "/" +  c.getName())
                .findFirst()
                .orElse("");
    }
}
