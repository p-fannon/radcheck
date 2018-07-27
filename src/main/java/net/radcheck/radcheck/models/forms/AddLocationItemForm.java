package net.radcheck.radcheck.models.forms;

import net.radcheck.radcheck.models.LatLon;
import net.radcheck.radcheck.models.User;

import javax.validation.constraints.NotNull;

public class AddLocationItemForm {

    private User user;

    private LatLon location;

    @NotNull
    private int userId;

    @NotNull
    private int locationId;

    public AddLocationItemForm() {
    }

    public AddLocationItemForm(User user, LatLon location) {
        this.user = user;
        this.location = location;
    }
}
