package ru.kalinina.service;

import com.jsoniter.JsonIterator;
import org.apache.commons.io.IOUtils;
import ru.kalinina.model.input.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class HttpService {
    public String getJsonFromResponse(String url) {
        try {
            return IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<InputCoin> getCoins() {
        return JsonIterator.deserialize(
                Objects.requireNonNull(getJsonFromResponse("https://api.binance.com/api/v3/exchangeInfo?permissions=SPOT")),
                InputCoins.class
        ).getSymbols();
    }

    public InputCandleStick getCandles(String json) {
        return JsonIterator.deserialize(
                json,
                InputCandleStick.class
        );
    }
}
