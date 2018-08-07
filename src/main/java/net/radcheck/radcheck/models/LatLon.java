package net.radcheck.radcheck.models;

import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "location")
@ControllerAdvice
public class LatLon {

    @Id
    @GeneratedValue
    @Column(name = "location_id")
    private int id;
    @NotNull
    private double lat;
    @NotNull
    private double lon;
    @NotNull
    @OneToOne
    private Query query;

    public LatLon() {
    }

    public LatLon(double lat, double lon, Query query) {
        this.lat = lat;
        this.lon = lon;
        this.query = query;
    }

    public int getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }
}
