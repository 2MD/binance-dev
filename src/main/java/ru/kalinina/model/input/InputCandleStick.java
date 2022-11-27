package ru.kalinina.model.input;

import com.jsoniter.annotation.JsonProperty;
import ru.kalinina.Main;
import ru.kalinina.utils.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InputCandleStick {
    @JsonProperty("s")
    private String symbol;
    @JsonProperty("k")
    private InputKline kline;

    public String getSymbol() {
        return symbol;
    }

    public InputKline getKline() {
        return kline;
    }

    @Override
    public String toString() {
        return "Пара " + Currency.extractCurrency(this.getSymbol()) + " " +
                ((this.getKline().getHighPrice().compareTo(this.getKline().getOpenPrice()) > 0) ? "Дала рост " : "Упала ") +
                " на " + this.getKline().getDiffPercentBtwOpenAndHigh() + "%" + " за интервал " + this.getKline().getInterval();
    }
}
