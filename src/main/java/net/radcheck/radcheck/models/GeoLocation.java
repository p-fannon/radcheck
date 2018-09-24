package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class GeoLocation {

    @SerializedName("lat")
    private double geoLatitude;
    @SerializedName("lng")
    private double geoLongitude;

    public GeoLocation() {
    }

    public GeoLocation(double geoLatitude, double geoLongitude) {
        this.geoLatitude = geoLatitude;
        this.geoLongitude = geoLongitude;
    }

    public double getGeoLatitude() {
        return geoLatitude;
    }

    public void setGeoLatitude(double geoLatitude) {
        this.geoLatitude = geoLatitude;
    }

    public double getGeoLongitude() {
        return geoLongitude;
    }

    public void setGeoLongitude(double geoLongitude) {
        this.geoLongitude = geoLongitude;
    }
}