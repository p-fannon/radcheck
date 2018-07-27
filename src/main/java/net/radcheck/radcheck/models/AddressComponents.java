package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AddressComponents {

    @SerializedName("long_name")
    private String longName;
    @SerializedName("short_name")
    private String shortName;
    @SerializedName("types")
    private ArrayList<String> types;

    public AddressComponents() {
    }

    public AddressComponents(String longName, String shortName, ArrayList<String> types) {
        this.longName = longName;
        this.shortName = shortName;
        this.types = types;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }
}
