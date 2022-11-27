package ru.kalinina.service;

import com.neovisionaries.ws.client.*;
import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kalinina.CandleBot;
import ru.kalinina.model.Event;
import ru.kalinina.model.Settings;
import ru.kalinina.model.input.InputCandleStick;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.kalinina.Main.*;

public class WebSocketService implements Runnable {
    private final HttpService httpService;
    private final CandleBot bot;
    private final List<String> partition;

    public WebSocketService(HttpService httpService, CandleBot bot, List<String> partition) {
        this.httpService = httpService;
        this.bot = bot;
        this.partition = partition;
    }

    private Boolean isCandleDiffNeedPercent(InputCandleStick candles, Settings setting) {
        return candles.getKline().getDiffPercentBtwOpenAndHigh() >= Double.parseDouble(setting.getPercent());
    }

    private void send(InputCandleStick candles, Settings settings, Double percent) throws TelegramApiException, InterruptedException {
        Boolean isNoneExists = isNoneExistEvent(candles.getSymbol(), settings.getInterval(), settings.getChartId());
        Boolean goodDiffPercent = isCandleDiffNeedPercent(candles, settings);
        Boolean socketIsConnect = socketIsConnectByChartId(settings.getChartId());
        if (goodDiffPercent && isNoneExists && socketIsConnect && EVENTS.add(new Event(candles.getSymbol(), settings.getInterval(), LocalDateTime.now(), percent, settings.getChartId()))) {
            log.info("Coin " + candles.getSymbol() + " for chart " + settings.getChartId() + " for period " + candles.getKline().getInterval() + " " + isNoneExists + " percent " + percent + " " + goodDiffPercent + " " + socketIsConnect);
            bot.sendMessage(settings.getChartId(), EmojiParser.parseToUnicode("📣") + " " + candles.toString());
        }
    }

    @Override
    public void run() {
        log.info("Start monitoring with CASH_SETTINGS_CANDLE_MONITORING = " + String.join("\n", CASH_SETTINGS_CANDLE_MONITORING.stream().map(Settings::toString).collect(Collectors.toSet())));
        CASH_SETTINGS_CANDLE_MONITORING.forEach(settings -> {
                    try {
                        WebSocketListener listener = new WebSocketAdapter() {
                            @Override
                            public void onTextMessage(WebSocket ws, String message) throws TelegramApiException {
                                InputCandleStick candles = httpService.getCandles(message);
                                Double percent = candles.getKline().getDiffPercentBtwOpenAndHigh();
                                log.info("Coin " + settings.getChartId() + " " + candles.getSymbol() + " period " + candles.getKline().getInterval() + " interval " + settings.getInterval() + " isNoneExists: " + isNoneExistEvent(candles.getSymbol(), settings.getInterval(), settings.getChartId()) +  " percent " + percent + " isCoolPercent: " + isCandleDiffNeedPercent(candles, settings) + " isChartCorrect: " + socketIsConnectByChartId(settings.getChartId()));
                                try {
                                    send(candles, settings, percent);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                                cause.printStackTrace();
                            }

                            @Override
                            public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                                cause.printStackTrace();
                            }

                            @Override
                            public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
                                cause.printStackTrace();
                            }
                        };


                        String subscribePairs = String.join(",", this.partition.stream()
                                .map(pair -> "\"" + pair.toLowerCase() + "@kline_" + settings.getInterval() + "\"")
                                .collect(Collectors.toSet()));
                        String textSubscribe = "{\"method\": \"SUBSCRIBE\", \"params\": [" + subscribePairs + "],  \"id\": 1}";
                        log.info(textSubscribe);

                        WebSocket websocket = new WebSocketFactory()
                                .createSocket("wss://stream.binance.com:9443/ws")
                                .connect()
                                .sendText(textSubscribe)
                                .setFrameQueueSize(1000)
                                .setMaxPayloadSize(1000)
                                .setMissingCloseFrameAllowed(false)
                                .addListener(listener);

                        SOCKET_CONNECTORS.get(settings.getChartId()).setWebSocket(websocket);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }
}
