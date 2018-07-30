package net.radcheck.radcheck.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue
    private int id;
    @NotNull
    private String email;
    @NotNull
    private String password;
    @ManyToMany
    private List<LatLon> userLocations;
    private List<String> customNames;
    @CreationTimestamp
    private Date joinedOn;
    @UpdateTimestamp
    private Date lastLogon;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<LatLon> getUserLocations() {
        return userLocations;
    }

    public void setUserLocations(List<LatLon> userLocations) {
        this.userLocations = userLocations;
    }

    public List<String> getCustomNames() {
        return customNames;
    }

    public void setCustomNames(List<String> customNames) {
        this.customNames=customNames;
    }

    public Date getJoinedOn() {
        return joinedOn;
    }

    public Date getLastLogon() {
        return lastLogon;
    }

    public void setLastLogon(Date lastLogon) {
        this.lastLogon = lastLogon;
    }

    public void addLocation(LatLon newLocation, String locationName) {
        userLocations.add(newLocation);
        customNames.add(locationName);
    }
    public void editLocation(LatLon currentLocation, String newName) {
        int index = userLocations.indexOf(currentLocation);
        customNames.remove(index);
        customNames.add(index, newName);
    }
    public void removeLocation(LatLon deleteLocation) {
        int index = userLocations.indexOf(deleteLocation);
        userLocations.remove(index);
        customNames.remove(index);
    }

}
