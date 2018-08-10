package net.radcheck.radcheck.models;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

public class GMap {

    @NotEmpty
    @NotBlank
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
