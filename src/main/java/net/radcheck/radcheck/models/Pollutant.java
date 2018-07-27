package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

public class Pollutant {
    @SerializedName("conc")
    private int plConcentration;
    @SerializedName("aqius")
    private int plAqiUs;
    @SerializedName("aqicn")
    private int plAqiCn;

    public Pollutant() {
    }

    public Pollutant(int plConcentration, int plAqiUs, int plAqiCn) {
        this.plConcentration = plConcentration;
        this.plAqiUs = plAqiUs;
        this.plAqiCn = plAqiCn;
    }

    public int getPlConcentration() {
        return plConcentration;
    }

    public void setPlConcentration(int plConcentration) {
        this.plConcentration = plConcentration;
    }

    public int getPlAqiUs() {
        return plAqiUs;
    }

    public void setPlAqiUs(int plAqiUs) {
        this.plAqiUs = plAqiUs;
    }

    public int getPlAqiCn() {
        return plAqiCn;
    }

    public void setPlAqiCn(int plAqiCn) {
        this.plAqiCn = plAqiCn;
    }
}
