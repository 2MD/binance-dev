package ru.kalinina.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.kalinina.Main;
import ru.kalinina.model.Settings;
import ru.kalinina.model.SocketConnector;
import ru.kalinina.utils.Command;

import java.util.*;
import java.util.stream.Collectors;

import static ru.kalinina.Main.*;

public final class CommandService {
    private static HashMap<Long, Command> USER_LAST_MESSAGES = new HashMap<>();

    public static SendMessage commandExecute(Command command, Long chartId, String inputMessage) throws InterruptedException {
        String message = "";
        String chartStr = chartId.toString();
        Command lastCommand = USER_LAST_MESSAGES.get(chartId);
        if (command != Command.START && command != Command.HELP && command != Command.RESET && command != Command.ALLACTIVES && lastCommand != null)
            command = lastCommand;
        switch (command) {
            case INTERVAL: {
                USER_LAST_MESSAGES.clear();
                Main.log.info("Input params of interval: " + inputMessage);
                Set<Settings> intervals = Arrays.stream(inputMessage.trim().split(","))
                        .map(e -> {
                            String[] values = e.trim().split("=");
                            return new Settings(values[0].trim(), values[1].trim(), chartStr);
                        })
                        .collect(Collectors.toSet());
                CASH_SETTINGS_CANDLE_MONITORING.addAll(intervals);
                cashService.saveCashState();
                if (SOCKET_CONNECTORS.containsKey(chartStr))
                SOCKET_CONNECTORS.get(chartStr).setIsConnect(true);
                else
                  SOCKET_CONNECTORS.put(chartStr, new SocketConnector(true));
                message = "Отлично! Теперь приложение будет мониторить: \n" + CASH_SETTINGS_CANDLE_MONITORING
                        .stream()
                        .filter(s -> s.getChartId().equals(chartStr))
                        .map(Settings::toString)
                        .collect(Collectors.joining("\n"));
                break;
            }
            case RESET: {
                USER_LAST_MESSAGES.entrySet().removeIf(entry -> entry.getKey().equals(chartId));
                CASH_SETTINGS_CANDLE_MONITORING.removeIf(s -> s.getChartId().equals(chartStr));
                cashService.saveCashState();
                SocketConnector socketConnector = SOCKET_CONNECTORS.get(chartStr);
                socketConnector.setIsConnect(false);
                if(socketConnector.getWebSocket() != null) socketConnector.getWebSocket().disconnect();
                Thread.sleep(3000);
                SOCKET_CONNECTORS.remove(chartStr);
                message = "Настройки сброшены. Можно начать настроить занова, введя команду /start";
                break;
            }
            case ALLACTIVES: {
                message = "Список активов, которые сейяас мониторятся:\n" + String.join("\n", CASH_ACTUAL_LIST_OF_COINS);
                break;
            }
            case UNKNOWN:
            case HELP:
            case START: {
                if (USER_LAST_MESSAGES.containsKey(chartId) && USER_LAST_MESSAGES.containsValue(Command.INTERVAL)) {
                    message = "Итак, мы уже настраивали бота.\n Хочешь сбросить настройки? Тогда введи команду /reset\n" +
                            "Если хочешь посмотреть на все активы в мониторинге /all\n" +
                            "Если хочешь задать новые интервалы для мониторинга /interval";
                } else {
                    message = "Давай настроим бота!\n За какие интервалы времени мы будем анализировать свечи?\n"
                            + "Доступные интервалы для мниторинга: 1s, 2s,...1m,15m,..,1h,5w..  и тд\n"
                            + " m -> minutes; h -> hours; d -> days; w -> weeks; M -> months.\n"
                            + "И какой процент мы будем высчитывать за какой интервал времени?\n"
                            + "По достижению этого процента от точки 3 до 1 свечи, тебе будет приходить уведомление.\n"
                            + "Введите интервалы через запятую. Пример: 1m=1.5%, 15m=3%";
                    USER_LAST_MESSAGES.clear();
                    USER_LAST_MESSAGES.put(chartId, Command.INTERVAL);
                }
                break;
            }
        }

        SendMessage messageSend = new SendMessage();
        messageSend.setText(message);
        messageSend.setChatId(chartStr);

        ReplyKeyboardMarkup menuMark = new ReplyKeyboardMarkup();
        KeyboardRow keyboardFirstRow = new KeyboardRow(
                Arrays.asList(new KeyboardButton("/reset"),
                        new KeyboardButton("/all"),
                        new KeyboardButton("/start")));

        menuMark.setKeyboard(Collections.singletonList(keyboardFirstRow));
        menuMark.setResizeKeyboard(true);
        menuMark.setOneTimeKeyboard(false);
        menuMark.setSelective(false);
        messageSend.setReplyMarkup(menuMark);

        return messageSend;
    }

}
