package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Geometry {

    @SerializedName("location")
    private GeoLocation marker;
    @SerializedName("location_type")
    private String locationType;
    @SerializedName("viewport")
    private Viewport viewport;

    public Geometry() {
    }

    public Geometry(GeoLocation marker, String locationType, Viewport viewport) {
        this.marker = marker;
        this.locationType = locationType;
        this.viewport = viewport;
    }

    public GeoLocation getMarker() {
        return marker;
    }

    public void setMarker(GeoLocation marker) {
        this.marker = marker;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
}