package net.radcheck.radcheck.models;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class GMap {

    @NotEmpty
    private String address;
    @NotNull
    private double lat;
    @NotNull
    private double lon;

    public GMap() {
    }

    public GMap(String address, double lat, double lon){
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat=lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon=lon;
    }

}
