package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Location extends Data {

    @SerializedName("type")
    private String locationType;
    @SerializedName("latitude")
    private double locationLat;
    @SerializedName("longitude")
    private double locationLng;
    @SerializedName("coordinates")
    private double [] locationCoordinates = {locationLat, locationLng};

    public Location() {}

    public Location(String locationType, double locationLat, double locationLng, double[] coordinates) {
        this.locationType = locationType;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.locationCoordinates = coordinates;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(double locationLng) {
        this.locationLng = locationLng;
    }

    public double[] getLocationCoordinates() {
        return locationCoordinates;
    }

    public void setLocationCoordinates(double[] locationCoordinates) {
        this.locationCoordinates = locationCoordinates;
    }
}
