package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Weather {
    @SerializedName("ts")
    private String weatherTimestamp;
    @SerializedName("tp")
    private int tempCelsius;
    @SerializedName("pr")
    private int airPressure;
    @SerializedName("hu")
    private int humidity;
    @SerializedName("ws")
    private double windSpeed;
    @SerializedName("wd")
    private int windDirection;
    @SerializedName("ic")
    private String weatherIcon;

    public Weather() {
    }

    public Weather(String weatherTimestamp, int tempCelsius, int airPressure, int humidity, double windSpeed, int windDirection, String weatherIcon) {
        this.weatherTimestamp = weatherTimestamp;
        this.tempCelsius = tempCelsius;
        this.airPressure = airPressure;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.weatherIcon = weatherIcon;
    }

    public String getWeatherTimestamp() {
        return weatherTimestamp;
    }

    public void setWeatherTimestamp(String weatherTimestamp) {
        this.weatherTimestamp = weatherTimestamp;
    }

    public int getTempCelsius() {
        return tempCelsius;
    }

    public void setTempCelsius(int tempCelsius) {
        this.tempCelsius = tempCelsius;
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

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
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