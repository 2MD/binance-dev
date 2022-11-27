package ru.kalinina;


import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kalinina.model.Event;
import ru.kalinina.model.Settings;
import ru.kalinina.model.SocketConnector;
import ru.kalinina.service.CashService;
import ru.kalinina.service.CoinService;
import ru.kalinina.service.HttpService;
import ru.kalinina.utils.Currency;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class Main {
    public static Set<String> CASH_ACTUAL_LIST_OF_COINS = ConcurrentHashMap.newKeySet();
    public static Set<Settings> CASH_SETTINGS_CANDLE_MONITORING = ConcurrentHashMap.newKeySet();
    public static Set<Event> EVENTS = ConcurrentHashMap.newKeySet();
    private static String messageWithCoins = "";
    public static final Logger log = Logger.getLogger(Main.class);
    public static final CashService cashService = new CashService();
    public static Map<String, SocketConnector> SOCKET_CONNECTORS = new ConcurrentHashMap<>();

    public static synchronized Boolean isNoneExistEvent(String symbol, String interval, String chartId) {
        log.info(Arrays.toString(EVENTS.toArray()));
        log.info("Find symbol = " + symbol + " interval = " + interval + " chartId = " + chartId);
        return EVENTS.stream().noneMatch(e -> e.getSymbol().equals(symbol) && e.getInterval().equals(interval) && e.getChartId().equals(chartId));
    }

    public static Boolean socketIsConnectByChartId(String chartId) {
        return SOCKET_CONNECTORS.get(chartId).getIsConnect();
    }

    public static void main(String[] args) {
        HttpService httpService = new HttpService();
        CoinService coinService = new CoinService();

        cashService.readCashState();
        boolean isRestart = !CASH_SETTINGS_CANDLE_MONITORING.isEmpty();
        if (isRestart) {
            CASH_SETTINGS_CANDLE_MONITORING.forEach(c -> SOCKET_CONNECTORS.put(c.getChartId(), new SocketConnector(true)));
        }

        CandleBot bot = new CandleBot(httpService, isRestart);
        bot.botConnect();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

        scheduler.scheduleWithFixedDelay(() -> {
            LocalDateTime current = LocalDateTime.now();
            log.info("Event start..size: " + EVENTS.size());
            EVENTS.removeIf(e -> e.getTime().isBefore(current));
            log.info("Event end. Size " + EVENTS.size());
        }, 0, 1, TimeUnit.MINUTES);

        scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        coinService.setInputCoins(httpService.getCoins());
                        Set<String> newCoins = coinService.getNewSymbols(CASH_ACTUAL_LIST_OF_COINS, coinService.findUniqCoins());
                        CASH_ACTUAL_LIST_OF_COINS.addAll(newCoins);

                        messageWithCoins = String.join("\n", newCoins.stream().map(Currency::extractCurrency).collect(Collectors.toSet()));
                        CASH_SETTINGS_CANDLE_MONITORING.stream().map(Settings::getChartId).forEach(chart -> {
                            try {
                                if (!messageWithCoins.isEmpty()) {
                                    log.info("New coins: " + messageWithCoins);
                                    cashService.saveCashState();
                                    bot.sendMessage(chart, "❗Новые пары:\n" + messageWithCoins);
                                }
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                , 0, 10, TimeUnit.MINUTES);
    }
}
