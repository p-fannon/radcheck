package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Units {
    @SerializedName("p2")
    private String pmTwoPointFive;
    @SerializedName("p1")
    private String pmTen;
    @SerializedName("o3")
    private String ozone;
    @SerializedName("n2")
    private String nitrogenDioxide;
    @SerializedName("s2")
    private String sulfurDioxide;
    @SerializedName("co")
    private String carbonMonoxide;

    public Units() {
    }

    public Units(String pmTwoPointFive, String pmTen, String ozone, String nitrogenDioxide, String sulfurDioxide, String carbonMonoxide) {
        this.pmTwoPointFive = pmTwoPointFive;
        this.pmTen = pmTen;
        this.ozone = ozone;
        this.nitrogenDioxide = nitrogenDioxide;
        this.sulfurDioxide = sulfurDioxide;
        this.carbonMonoxide = carbonMonoxide;
    }

    public String getPmTwoPointFive() {
        return pmTwoPointFive;
    }

    public void setPmTwoPointFive(String pmTwoPointFive) {
        this.pmTwoPointFive = pmTwoPointFive;
    }

    public String getPmTen() {
        return pmTen;
    }

    public void setPmTen(String pmTen) {
        this.pmTen = pmTen;
    }

    public String getOzone() {
        return ozone;
    }

    public void setOzone(String ozone) {
        this.ozone = ozone;
    }

    public String getNitrogenDioxide() {
        return nitrogenDioxide;
    }

    public void setNitrogenDioxide(String nitrogenDioxide) {
        this.nitrogenDioxide = nitrogenDioxide;
    }

    public String getSulfurDioxide() {
        return sulfurDioxide;
    }

    public void setSulfurDioxide(String sulfurDioxide) {
        this.sulfurDioxide = sulfurDioxide;
    }

    public String getCarbonMonoxide() {
        return carbonMonoxide;
    }

    public void setCarbonMonoxide(String carbonMonoxide) {
        this.carbonMonoxide = carbonMonoxide;
    }
}
