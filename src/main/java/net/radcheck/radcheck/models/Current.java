package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Current {
    @SerializedName("weather")
    private Weather aqiWeather;
    @SerializedName("pollution")
    private Pollution aqiPollution;

    public Current() {
    }

    public Current(Weather aqiWeather, Pollution aqiPollution) {
        this.aqiWeather = aqiWeather;
        this.aqiPollution = aqiPollution;
    }

    public Weather getAqiWeather() {
        return aqiWeather;
    }

    public void setAqiWeather(Weather aqiWeather) {
        this.aqiWeather = aqiWeather;
    }

    public Pollution getAqiPollution() {
        return aqiPollution;
    }

    public void setAqiPollution(Pollution aqiPollution) {
        this.aqiPollution = aqiPollution;
    }
}
