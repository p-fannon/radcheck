package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class History {
    @SerializedName("weather")
    private ArrayList<Weather> historyWeather;
    @SerializedName("pollution")
    private ArrayList<Pollution> historyPollution;

    public History() {
    }

    public History(ArrayList<Weather> historyWeather, ArrayList<Pollution> historyPollution) {
        this.historyWeather = historyWeather;
        this.historyPollution = historyPollution;
    }

    public ArrayList<Weather> getHistoryWeather() {
        return historyWeather;
    }

    public void setHistoryWeather(ArrayList<Weather> historyWeather) {
        this.historyWeather = historyWeather;
    }

    public ArrayList<Pollution> getHistoryPollution() {
        return historyPollution;
    }

    public void setHistoryPollution(ArrayList<Pollution> historyPollution) {
        this.historyPollution = historyPollution;
    }
}
