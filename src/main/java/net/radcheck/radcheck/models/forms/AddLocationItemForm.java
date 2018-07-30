package net.radcheck.radcheck.models.forms;

import net.radcheck.radcheck.models.LatLon;
import net.radcheck.radcheck.models.User;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AddLocationItemForm {

    private User user;

    private LatLon location;

    @NotNull
    private int userId;

    @NotNull
    private int locationId;

    @Size(min=3, max=30, message="Please enter a name for this location between 3 and 30 characters")
    private String locationName;

    public AddLocationItemForm() {
    }

    public AddLocationItemForm(User user, LatLon location, String locationName) {
        this.user = user;
        this.location = location;
        this.locationName = locationName;
    }
}
