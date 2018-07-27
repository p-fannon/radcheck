package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Forecasts {

    @SerializedName("ts")
    private String forecastTimestamp;
    @SerializedName("aqius")
    private int aqiUsStd;
    @SerializedName("aqicn")
    private int aqiCnStd;
    @SerializedName("tp")
    private int tempCelsius;
    @SerializedName("tp_min")
    private int tempMinCelsius;
    @SerializedName("pr")
    private int airPressure;
    @SerializedName("hu")
    private int humidity;
    @SerializedName("ws")
    private int windSpeed;
    @SerializedName("wd")
    private int windDirection;
    @SerializedName("ic")
    private String weatherIcon;

    public Forecasts() {
    }

    public Forecasts(String forecastTimestamp, int aqiUsStd, int aqiCnStd, int tempCelsius, int tempMinCelsius, int airPressure, int humidity, int windSpeed, int windDirection, String weatherIcon) {
        this.forecastTimestamp = forecastTimestamp;
        this.aqiUsStd = aqiUsStd;
        this.aqiCnStd = aqiCnStd;
        this.tempCelsius = tempCelsius;
        this.tempMinCelsius = tempMinCelsius;
        this.airPressure = airPressure;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.weatherIcon = weatherIcon;
    }

    public String getForecastTimestamp() {
        return forecastTimestamp;
    }

    public void setForecastTimestamp(String forecastTimestamp) {
        this.forecastTimestamp = forecastTimestamp;
    }

    public int getAqiUsStd() {
        return aqiUsStd;
    }

    public void setAqiUsStd(int aqiUsStd) {
        this.aqiUsStd = aqiUsStd;
    }

    public int getAqiCnStd() {
        return aqiCnStd;
    }

    public void setAqiCnStd(int aqiCnStd) {
        this.aqiCnStd = aqiCnStd;
    }

    public int getTempCelsius() {
        return tempCelsius;
    }

    public void setTempCelsius(int tempCelsius) {
        this.tempCelsius = tempCelsius;
    }

    public int getTempMinCelsius() {
        return tempMinCelsius;
    }

    public void setTempMinCelsius(int tempMinCelsius) {
        this.tempMinCelsius = tempMinCelsius;
    }

    public int getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(int airPressure) {
        this.airPressure = airPressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }
}
