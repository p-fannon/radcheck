package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Results {

    @SerializedName("address_components")
    private ArrayList<AddressComponents> addressComponents;
    @SerializedName("formatted_address")
    private String formattedAddress;
    @SerializedName("geometry")
    private Geometry geometry;
    @SerializedName("place_id")
    private String placeId;
    @SerializedName("types")
    private ArrayList<String> types;

    public Results() {
    }

    public Results(ArrayList<AddressComponents> addressComponents, String formattedAddress, Geometry geometry, String placeId, ArrayList<String> types) {
        this.addressComponents = addressComponents;
        this.formattedAddress = formattedAddress;
        this.geometry = geometry;
        this.placeId = placeId;
        this.types = types;
    }

    public ArrayList<AddressComponents> getAddressComponents() {
        return addressComponents;
    }

    public void setAddressComponents(ArrayList<AddressComponents> addressComponents) {
        this.addressComponents = addressComponents;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }
}
