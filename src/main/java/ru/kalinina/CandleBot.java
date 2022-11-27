package ru.kalinina;

import org.apache.commons.collections4.ListUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.kalinina.model.SocketConnector;
import ru.kalinina.service.CommandService;
import ru.kalinina.service.HttpService;
import ru.kalinina.service.WebSocketService;
import ru.kalinina.utils.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static ru.kalinina.Main.*;

public class CandleBot extends TelegramLongPollingBot {
    final int RECONNECT_PAUSE = 10000;
    private HttpService httpService;

    public CandleBot(HttpService httpService, Boolean isRun) {
        this.httpService = httpService;
        if (isRun) socketRun();
    }

    private void socketRun() {
        List<List<String>> partitions = ListUtils.partition(new ArrayList<>(CASH_ACTUAL_LIST_OF_COINS), 100);
       //Because the library web socket has limit of subscriptions
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        for (List<String> partition : partitions) {
            executorService.submit(new WebSocketService(this.httpService, this, partition));
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasCallbackQuery()) {
            Message message = update.getMessage();
            try {
                Command command = Command.findCommand(message.getText());
                if (command == Command.START && CASH_SETTINGS_CANDLE_MONITORING.stream().filter(c -> c.getChartId().equals(message.getChatId().toString())).collect(Collectors.toSet()).isEmpty())
                    execute(
                            sendImage(message.getChatId(),
                                    Objects.requireNonNull(this.getClass().getResource("/candle.png")).getPath())
                    );
                execute(CommandService.commandExecute(command, message.getChatId(), message.getText()));
                if (SOCKET_CONNECTORS.values().stream().anyMatch(SocketConnector::getIsConnect)) socketRun();
            } catch (TelegramApiException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public SendPhoto sendImage(Long chatId, String path) {
        SendPhoto photo = new SendPhoto();
        photo.setPhoto(new InputFile(new File(path)));
        photo.setChatId(chatId.toString());
        return photo;
    }

    public void sendMessage(String chatId, String message) throws TelegramApiException {
        SendMessage messageSend = new SendMessage();
        messageSend.setText(message);
        messageSend.setChatId(chatId);
        execute(messageSend);
    }

    @Override
    public String getBotUsername() {
        return "binance_candle_bot";
    }

    @Override
    public String getBotToken() {
        return "5764433077:AAEEz_sGAoGobLesX33NyiP0lkH_TIoiVVE";
    }

    public void botConnect() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiRequestException e) {
            try {
                Thread.sleep(RECONNECT_PAUSE);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return;
            }
            botConnect();
        } catch (TelegramApiException telegramApiException) {
            telegramApiException.printStackTrace();
        }
    }
}
