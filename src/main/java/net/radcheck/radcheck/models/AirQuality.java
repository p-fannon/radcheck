package net.radcheck.radcheck.models;


import com.google.gson.annotations.SerializedName;

public class AirQuality {
    @SerializedName("status")
    private String aqiStatus;
    @SerializedName("data")
    private Data aqiData;

    public AirQuality() { }

    public AirQuality(String aqiStatus, Data aqiData) {
        this.aqiStatus = aqiStatus;
        this.aqiData = aqiData;
    }

    public String getAqiStatus() {
        return aqiStatus;
    }

    public void setAqiStatus(String aqiStatus) {
        this.aqiStatus = aqiStatus;
    }

    public Data getAqiData() {
        return aqiData;
    }

    public void setAqiData(Data aqiData) {
        this.aqiData = aqiData;
    }
}
