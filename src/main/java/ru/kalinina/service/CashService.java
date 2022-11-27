package ru.kalinina.service;

import ru.kalinina.Main;
import ru.kalinina.model.Settings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CashService {
    private final String CASH_STATE_FILE_NAME = "saveState.txt";
    private final String COINS_NAME_VAR = "coins=";
    private final String SETTING_NAME_VAR = "settings=";

    public void saveCashState() {
        try (PrintWriter writer =
                    new PrintWriter(
                            Objects.requireNonNull(this.getClass().getClassLoader().getResource(CASH_STATE_FILE_NAME)).getPath())) {
           Main.log.info("Save cash..");
           writer.print("");
           writer.println(COINS_NAME_VAR + String.join(",", Main.CASH_ACTUAL_LIST_OF_COINS));
           writer.println(SETTING_NAME_VAR + String.join(",", Main.CASH_SETTINGS_CANDLE_MONITORING.stream().map(Settings::toString).collect(Collectors.toSet())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readCashState() {
        try (InputStreamReader streamReader =
                     new InputStreamReader(getFileFromResourceAsStream(), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(COINS_NAME_VAR) && line.length() > COINS_NAME_VAR.length()) {
                    String cashCoins = line.substring(line.indexOf(COINS_NAME_VAR) + COINS_NAME_VAR.length());
                    Main.log.info("Read from cash coins:\n" + cashCoins);
                    Main.CASH_ACTUAL_LIST_OF_COINS = Arrays.stream(cashCoins.split(",")).collect(Collectors.toSet());
                } else if (line.contains(SETTING_NAME_VAR) && line.length() > SETTING_NAME_VAR.length()) {
                    String settings = line.substring(line.lastIndexOf(SETTING_NAME_VAR));
                    Main.log.info("Read from cash settings:\n" + settings);
                    Main.CASH_SETTINGS_CANDLE_MONITORING = Arrays
                            .stream(settings.split(","))
                            .map(Settings::new)
                            .collect(Collectors.toSet());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream getFileFromResourceAsStream() {
        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(CASH_STATE_FILE_NAME);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + CASH_STATE_FILE_NAME);
        } else {
            return inputStream;
        }

    }
}
