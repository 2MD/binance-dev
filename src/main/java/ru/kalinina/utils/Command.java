package ru.kalinina.utils;

import java.util.Arrays;

public enum Command {
    START("/start"),
    ALLACTIVES("/all"),
    INTERVAL("/interval"),
    PERCENT("/percent"),
    HELP("/help"),
    UNKNOWN("unknown"),
    RESET("/reset");

    private final String name;

    Command(String name) {
        this.name = name;
    }

    public static Command findCommand(String inputMessage) {
        try {
            return Arrays.stream(Command.values())
                    .filter(x -> inputMessage.equals(x.getName()))
                    .findFirst()
                    .orElse(Command.UNKNOWN);
        } catch (Exception e) {
            return Command.UNKNOWN;
        }
    }

    public String getName() {
        return name;
    }
}
