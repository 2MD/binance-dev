package ru.kalinina.model;

public class Settings {
    private String interval;
    private String percent;
    private String chartId;
    private final String FIRST_PART_OF_STR_INTERVAL = "Интервал мониторинга ";
    private final String SECOND_PART_OF_STR_PERCENT= " процент ";
    private final String CHART= " чат ";

    public Settings(String interval, String percent, String chartId) {
        this.interval = interval;
        this.percent = percent;
        this.chartId = chartId;
    }

    public Settings(String str) {
        int indexInterval = str.indexOf(FIRST_PART_OF_STR_INTERVAL);
        int intervalLen = FIRST_PART_OF_STR_INTERVAL.length();
        int shiftInterval = indexInterval + intervalLen;
        int indexPercent = str.lastIndexOf(SECOND_PART_OF_STR_PERCENT);
        int percentLen = SECOND_PART_OF_STR_PERCENT.length();
        int shiftPercent = percentLen + indexPercent;
        int indexChart = str.lastIndexOf(CHART);
        int chartLen = CHART.length();
        int shiftCart = chartLen + indexChart;

        this.interval = str.substring(shiftInterval, indexPercent);
        this.percent = str.substring(shiftPercent, indexChart);
        this.chartId = str.substring(shiftCart);
    }

    public String getInterval() {
        return interval;
    }

    public String getPercent() {
        return percent.replace("%", "");
    }

    @Override
    public String toString() {
        return FIRST_PART_OF_STR_INTERVAL + this.getInterval() + SECOND_PART_OF_STR_PERCENT + this.getPercent() + CHART + this.getChartId();
    }

    public String getChartId() {
        return chartId;
    }
}
