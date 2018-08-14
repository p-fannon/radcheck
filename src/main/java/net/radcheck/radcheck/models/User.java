package net.radcheck.radcheck.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.persistence.*;
import java.sql.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user")
@ControllerAdvice
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private long id;
    @Column(name = "email")
    @NotEmpty(message = "Please provide an email")
    private String email;
    @Column(name = "password")
    @Length(min = 5, message = "Your password needs at least 5 characters")
    @NotEmpty(message = "Please provide your password")
    @org.springframework.data.annotation.Transient
    private String password;
    @Column(name = "active")
    private int active;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
    @ManyToMany
    @JoinTable(name = "user_locations", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "location_id"))
    private List<LatLon> locations;
    @Column(name = "location_names")
    @ElementCollection(targetClass=String.class)
    private List<String> names;
    @CreationTimestamp
    private Date joinedOn;

    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<LatLon> getLocations() {
        return locations;
    }

    public void setLocations(List<LatLon> locations) {
        this.locations = locations;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names=names;
    }

    public Date getJoinedOn() {
        return joinedOn;
    }

    public void addLocation(LatLon newLocation, String locationName) {
        locations.add(newLocation);
        names.add(locationName);
    }
    public void editLocation(LatLon currentLocation, String newName) {
        int index = 0;
        for (LatLon location : locations) {
            if (currentLocation.getId() == location.getId()) {
                index = locations.indexOf(location);
            }
        }
        names.remove(index);
        names.add(index, newName);
    }
    public void removeLocation(LatLon deleteLocation) {
        int index = 0;
        for (LatLon location : locations) {
            if (deleteLocation.getId() == location.getId()) {
                index = locations.indexOf(location);
            }
        }
        locations.remove(index);
        names.remove(index);
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
