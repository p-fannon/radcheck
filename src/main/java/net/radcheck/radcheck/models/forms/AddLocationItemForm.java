package net.radcheck.radcheck.models.forms;

import net.radcheck.radcheck.models.LatLon;
import net.radcheck.radcheck.models.User;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ControllerAdvice
public class AddLocationItemForm {

    private LatLon location;

    private String city;

    private String country;

    @Size(min=3, max=30)
    private String locationName;

    public AddLocationItemForm() {
    }

    public AddLocationItemForm(@NotNull LatLon location) {
        this.location = location;
        this.city = location.getCity();
        this.country = location.getCountry();
    }

    public LatLon getLocation() {
        return location;
    }

    public void setLocation(LatLon location) {
        this.location = location;
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

}
