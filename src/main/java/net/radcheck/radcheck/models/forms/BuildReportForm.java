package net.radcheck.radcheck.models.forms;

import net.radcheck.radcheck.models.LatLon;
import net.radcheck.radcheck.models.User;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.*;

public class BuildReportForm {

    private User user;

    private List<LatLon> locations;

    private List<String> names;

    @NotNull
    @NotEmpty
    private List<Integer> locationIds;

    private HashMap<String, Integer> userDefinedLocations = new HashMap<>();

    private Set<Map.Entry<String, Integer>> reportOptions;

    public BuildReportForm() {
    }

    public BuildReportForm(User user) {
        this.user = user;
        locations = user.getLocations();
        names = user.getNames();
        for (LatLon location : locations) {
            userDefinedLocations.put(names.get(locations.indexOf(location)), location.getId());
        }
        reportOptions = userDefinedLocations.entrySet();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        this.names = names;
    }

    public HashMap<String, Integer> getUserDefinedLocations() {
        return userDefinedLocations;
    }

    public void setUserDefinedLocations(HashMap<String, Integer> userDefinedLocations) {
        this.userDefinedLocations = userDefinedLocations;
    }

    public Set<Map.Entry<String, Integer>> getReportOptions() {
        return reportOptions;
    }

    public void setReportOptions(Set<Map.Entry<String, Integer>> reportOptions) {
        this.reportOptions = reportOptions;
    }

    public List<Integer> getLocationIds() {
        return locationIds;
    }

    public void setLocationIds(List<Integer> locationIds) {
        this.locationIds = locationIds;
    }

}
