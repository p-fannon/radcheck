package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

import javax.validation.constraints.NotNull;

public class Measurements {

    @SerializedName("id")
    private int radId;
    @SerializedName("user_id")
    private int radUserId;
    @SerializedName("value")
    private double radValue;
    @SerializedName("unit")
    private String radUnit;
    @SerializedName("location_name")
    private String radLocationName;
    @SerializedName("device_id")
    private String radDeviceId;
    @SerializedName("original_id")
    private String radOriginalId;
    @SerializedName("mesaurement_import_id")
    private String radMeasurementImportId;
    @SerializedName("captured_at")
    private String radTimestamp;
    @SerializedName("height")
    private double radHeight;
    @SerializedName("devicetype_id")
    private String radDevicetypeId;
    @SerializedName("sensor_id")
    private String radSensorId;
    @SerializedName("station_id")
    private String radStationId;
    @SerializedName("channel_id")
    private String radChannelId;
    @NotNull
    @SerializedName("latitude")
    private double radLat;
    @NotNull
    @SerializedName("longitude")
    private double radLng;

    public Measurements() {
    }

    public Measurements(int radId, int radUserId, double radValue, String radUnit, String radLocationName, String radDeviceId, String radOriginalId, String radMeasurementImportId, String radTimestamp, double radHeight, String radDevicetypeId, String radSensorId, String radStationId, String radChannelId, double radLat, double radLng) {
        this.radId = radId;
        this.radUserId = radUserId;
        this.radValue = radValue;
        this.radUnit = radUnit;
        this.radLocationName = radLocationName;
        this.radDeviceId = radDeviceId;
        this.radOriginalId = radOriginalId;
        this.radMeasurementImportId = radMeasurementImportId;
        this.radTimestamp = radTimestamp;
        this.radHeight = radHeight;
        this.radDevicetypeId = radDevicetypeId;
        this.radSensorId = radSensorId;
        this.radStationId = radStationId;
        this.radChannelId = radChannelId;
        this.radLat = radLat;
        this.radLng = radLng;
    }

    public String getRadUnit() {
        return radUnit;
    }

    public void setRadUnit(String radUnit) {
        this.radUnit = radUnit;
    }

    public double getRadValue() {
        return radValue;
    }

    public void setRadValue(double radValue) {
        this.radValue = radValue;
    }

    public double getRadLat() {
        return radLat;
    }

    public void setRadLat(double radLat) {
        this.radLat = radLat;
    }

    public double getRadLng() {
        return radLng;
    }

    public void setRadLng(double radLng) {
        this.radLng = radLng;
    }

    public String getRadTimestamp() {
        return radTimestamp;
    }

    public void setRadTimestamp(String radTimestamp) {
        this.radTimestamp = radTimestamp;
    }

    public int getRadId() {
        return radId;
    }

    public void setRadId(int radId) {
        this.radId = radId;
    }

    public int getRadUserId() {
        return radUserId;
    }

    public void setRadUserId(int radUserId) {
        this.radUserId = radUserId;
    }

    public String getRadLocationName() {
        return radLocationName;
    }

    public void setRadLocationName(String radLocationName) {
        this.radLocationName = radLocationName;
    }

    public String getRadDeviceId() {
        return radDeviceId;
    }

    public void setRadDeviceId(String radDeviceId) {
        this.radDeviceId = radDeviceId;
    }

    public String getRadOriginalId() {
        return radOriginalId;
    }

    public void setRadOriginalId(String radOriginalId) {
        this.radOriginalId = radOriginalId;
    }

    public String getRadMeasurementImportId() {
        return radMeasurementImportId;
    }

    public void setRadMeasurementImportId(String radMeasurementImportId) {
        this.radMeasurementImportId = radMeasurementImportId;
    }

    public double getRadHeight() {
        return radHeight;
    }

    public void setRadHeight(double radHeight) {
        this.radHeight = radHeight;
    }

    public String getRadDevicetypeId() {
        return radDevicetypeId;
    }

    public void setRadDevicetypeId(String radDevicetypeId) {
        this.radDevicetypeId = radDevicetypeId;
    }

    public String getRadSensorId() {
        return radSensorId;
    }

    public void setRadSensorId(String radSensorId) {
        this.radSensorId = radSensorId;
    }

    public String getRadStationId() {
        return radStationId;
    }

    public void setRadStationId(String radStationId) {
        this.radStationId = radStationId;
    }

    public String getRadChannelId() {
        return radChannelId;
    }

    public void setRadChannelId(String radChannelId) {
        this.radChannelId = radChannelId;
    }

}
