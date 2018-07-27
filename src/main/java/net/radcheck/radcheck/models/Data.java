package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Data {

    @SerializedName("name")
    private String aqiName;
    @SerializedName("local_name")
    private String aqiLocalName;
    @SerializedName("city")
    private String aqiCity;
    @SerializedName("state")
    private String aqiState;
    @SerializedName("country")
    private String aqiCountry;
    @SerializedName("location")
    private Location aqiLocation;
    @SerializedName("forecasts")
    private ArrayList<Forecasts> aqiForecasts;
    @SerializedName("current")
    private Current aqiCurrent;
    @SerializedName("history")
    private History aqiHistory;
    @SerializedName("units")
    private Units aqiUnits;

    public Data() {
    }

    public Data(String aqiName, String aqiLocalName, String aqiCity, String aqiState, String aqiCountry, Location aqiLocation, ArrayList<Forecasts> aqiForecasts, Current aqiCurrent, History aqiHistory, Units aqiUnits) {
        this.aqiName = aqiName;
        this.aqiLocalName = aqiLocalName;
        this.aqiCity = aqiCity;
        this.aqiState = aqiState;
        this.aqiCountry = aqiCountry;
        this.aqiLocation = aqiLocation;
        this.aqiForecasts = aqiForecasts;
        this.aqiCurrent = aqiCurrent;
        this.aqiHistory = aqiHistory;
        this.aqiUnits = aqiUnits;
    }

    public String getAqiName() {
        return aqiName;
    }

    public void setAqiName(String aqiName) {
        this.aqiName = aqiName;
    }

    public String getAqiLocalName() {
        return aqiLocalName;
    }

    public void setAqiLocalName(String aqiLocalName) {
        this.aqiLocalName = aqiLocalName;
    }

    public String getAqiCity() {
        return aqiCity;
    }

    public void setAqiCity(String aqiCity) {
        this.aqiCity = aqiCity;
    }

    public String getAqiState() {
        return aqiState;
    }

    public void setAqiState(String aqiState) {
        this.aqiState = aqiState;
    }

    public String getAqiCountry() {
        return aqiCountry;
    }

    public void setAqiCountry(String aqiCountry) {
        this.aqiCountry = aqiCountry;
    }

    public Location getAqiLocation() {
        return aqiLocation;
    }

    public void setAqiLocation(Location aqiLocation) {
        this.aqiLocation = aqiLocation;
    }

    public ArrayList<Forecasts> getAqiForecasts() {
        return aqiForecasts;
    }

    public void setAqiForecasts(ArrayList<Forecasts> aqiForecasts) {
        this.aqiForecasts = aqiForecasts;
    }

    public Current getAqiCurrent() {
        return aqiCurrent;
    }

    public void setAqiCurrent(Current aqiCurrent) {
        this.aqiCurrent = aqiCurrent;
    }

    public History getAqiHistory() {
        return aqiHistory;
    }

    public void setAqiHistory(History aqiHistory) {
        this.aqiHistory = aqiHistory;
    }

    public Units getAqiUnits() {
        return aqiUnits;
    }

    public void setAqiUnits(Units aqiUnits) {
        this.aqiUnits = aqiUnits;
    }
}
