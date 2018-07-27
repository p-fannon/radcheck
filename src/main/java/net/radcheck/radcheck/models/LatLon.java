package net.radcheck.radcheck.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class LatLon {

    @Id
    @GeneratedValue
    private int id;
    @NotNull
    private double lat;
    @NotNull
    private double lon;
    @ManyToMany(mappedBy = "userLocations")
    private List<User> ownerList;
    @OneToOne
    private Query query;

    public LatLon() {
    }

    public LatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
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

    public List<User> getOwnerList() {
        return ownerList;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setOwnerList(List<User> ownerList) {
        this.ownerList = ownerList;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }
}
