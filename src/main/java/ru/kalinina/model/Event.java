package ru.kalinina.model;

import java.time.LocalDateTime;

public class Event {
    private String symbol;
    private String interval;
    private Double percent;
    private String chartId;
    private LocalDateTime time;

    public Event(String symbol, String interval, LocalDateTime time, Double percent, String chartId) {
        this.interval = interval;
        this.symbol = symbol;
        this.time = time;
        this.percent = percent;
        this.chartId = chartId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getInterval() {
        return interval;
    }

    public Double getPercent() {
        return percent;
    }

    public String getChartId() {
        return chartId;
    }

    @Override
    public String toString() {
        return "Symbol = " + this.getSymbol() + " interval = " + this.getInterval() + " chart =  " + this.getChartId();
    }
}
