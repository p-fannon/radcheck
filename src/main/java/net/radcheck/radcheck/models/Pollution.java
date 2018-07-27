package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Pollution {
    @SerializedName("ts")
    private String pollutionTimestamp;
    @SerializedName("aqius")
    private int aqiUsStd;
    @SerializedName("mainus")
    private String mainUsPollutant;
    @SerializedName("aqicn")
    private int aqiCnStd;
    @SerializedName("maincn")
    private String mainCnPollutant;
    @SerializedName("pl")
    private Pollutant aqiPollutant;

    public Pollution() {
    }

    public Pollution(String pollutionTimestamp, int aqiUsStd, String mainUsPollutant, int aqiCnStd, String mainCnPollutant, Pollutant aqiPollutant) {
        this.pollutionTimestamp = pollutionTimestamp;
        this.aqiUsStd = aqiUsStd;
        this.mainUsPollutant = mainUsPollutant;
        this.aqiCnStd = aqiCnStd;
        this.mainCnPollutant = mainCnPollutant;
        this.aqiPollutant = aqiPollutant;
    }

    public String getPollutionTimestamp() {
        return pollutionTimestamp;
    }

    public void setPollutionTimestamp(String pollutionTimestamp) {
        this.pollutionTimestamp = pollutionTimestamp;
    }

    public int getAqiUsStd() {
        return aqiUsStd;
    }

    public void setAqiUsStd(int aqiUsStd) {
        this.aqiUsStd = aqiUsStd;
    }

    public String getMainUsPollutant() {
        return mainUsPollutant;
    }

    public void setMainUsPollutant(String mainUsPollutant) {
        this.mainUsPollutant = mainUsPollutant;
    }

    public int getAqiCnStd() {
        return aqiCnStd;
    }

    public void setAqiCnStd(int aqiCnStd) {
        this.aqiCnStd = aqiCnStd;
    }

    public String getMainCnPollutant() {
        return mainCnPollutant;
    }

    public void setMainCnPollutant(String mainCnPollutant) {
        this.mainCnPollutant = mainCnPollutant;
    }

    public Pollutant getAqiPollutant() {
        return aqiPollutant;
    }

    public void setAqiPollutant(Pollutant aqiPollutant) {
        this.aqiPollutant = aqiPollutant;
    }
}
