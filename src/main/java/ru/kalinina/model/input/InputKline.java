package ru.kalinina.model.input;

import com.jsoniter.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InputKline {
    @JsonProperty("o")
    private String openPrice; //3
    @JsonProperty("h")
    private String highPrice; //1
    @JsonProperty("l")
    private String lowPrice;
    @JsonProperty("c")
    private String lastPrice;
    @JsonProperty("i")
    private String interval;

    public BigDecimal getOpenPrice() {
        return new BigDecimal(openPrice);
    }

    public BigDecimal getHighPrice() {
        return new BigDecimal(highPrice);
    }

    public Double getDiffPercentBtwOpenAndHigh() {
        BigDecimal diff = this.getHighPrice().subtract(this.getOpenPrice()).abs();
        double persentDiff = 0.0;
        if (diff.compareTo(BigDecimal.ZERO) != 0 && this.getHighPrice().compareTo(BigDecimal.ZERO) != 0) {
            try {
                persentDiff = diff
                        .divide(this.getHighPrice(), RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return persentDiff;
    }

    public BigDecimal getLowPrice() {
        return new BigDecimal(lowPrice);
    }

    public String getLastPrice() {
        return lastPrice;
    }

    public String getInterval() {
        return interval;
    }
}
