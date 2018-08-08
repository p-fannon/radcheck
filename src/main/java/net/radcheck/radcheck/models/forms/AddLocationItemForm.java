package net.radcheck.radcheck.models.forms;

import net.radcheck.radcheck.models.LatLon;
import net.radcheck.radcheck.models.Query;
import net.radcheck.radcheck.models.User;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ControllerAdvice
public class AddLocationItemForm {

    private User user;

    private Query query;

    private LatLon location;

    private String city;

    private String country;

    private long userId;

    @Size(min=3, max=30, message = "Please enter a name between 3 and 30 characters")
    private String locationName;

    public AddLocationItemForm() {
    }

    public AddLocationItemForm(@NotNull User user, @NotNull LatLon location) {
        this.user = user;
        this.location = location;
        this.query = location.getQuery();
        this.userId = user.getId();
        this.city = query.getCity();
        this.country = query.getCountry();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LatLon getLocation() {
        return location;
    }

    public void setLocation(LatLon location) {
        this.location = location;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
