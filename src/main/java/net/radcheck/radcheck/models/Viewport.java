package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Viewport {

    @SerializedName("northeast")
    private GeoLocation northEast;
    @SerializedName("southwest")
    private GeoLocation southWest;

    public Viewport() {
    }

    public Viewport(GeoLocation northEast, GeoLocation southWest) {
        this.northEast = northEast;
        this.southWest = southWest;
    }

    public GeoLocation getNorthEast() {
        return northEast;
    }

    public void setNorthEast(GeoLocation northEast) {
        this.northEast = northEast;
    }

    public GeoLocation getSouthWest() {
        return southWest;
    }

    public void setSouthWest(GeoLocation southWest) {
        this.southWest = southWest;
    }
}
