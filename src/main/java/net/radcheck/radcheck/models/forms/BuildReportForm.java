package net.radcheck.radcheck.models.forms;

import net.radcheck.radcheck.models.LatLon;
import net.radcheck.radcheck.models.User;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.*;

public class BuildReportForm {

    private User user;

    private List<LatLon> locations;

    private List<String> names;

    @Size(min=3, max=30)
    private String reportName;

    private Date todayDate;

    private HashMap<String, Integer> userDefinedLocations;

    private Set<Map.Entry<String, Integer>> reportOptions;

    public BuildReportForm() {
    }

    public BuildReportForm(User user, Instant date) {
        this.user = user;
        locations = user.getLocations();
        names = user.getNames();
        for (LatLon location : locations) {
            userDefinedLocations.put(names.get(locations.indexOf(location)), location.getId());
        }
        reportOptions = userDefinedLocations.entrySet();
        todayDate = Date.from(date);
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
}
