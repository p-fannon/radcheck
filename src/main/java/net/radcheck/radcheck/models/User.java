package net.radcheck.radcheck.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.HashMap;

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
    private HashMap<LatLon, String> userLocations;
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

    public HashMap<LatLon, String> getUserLocations() {
        return userLocations;
    }

    public void setUserLocations(HashMap<LatLon, String> userLocations) {
        this.userLocations = userLocations;
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

}
