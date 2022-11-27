package ru.kalinina.model.input;

public class InputCoin {
    private String symbol;
    private String baseAsset;
    private String quoteAsset;

    public String getQuoteAsset() {
        return quoteAsset;
    }

    public String getBaseAsset() {
        return baseAsset;
    }

    public String getSymbol() {
        return symbol;
    }
}
