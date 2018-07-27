package net.radcheck.radcheck.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class GMap {

    @NotNull
    @Size(min=3)
    private String address;

    public GMap() {
    }

    public GMap(String address){
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
