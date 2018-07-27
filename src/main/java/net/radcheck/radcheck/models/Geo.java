package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Geo {

    @SerializedName("results")
    private ArrayList<Results> results;
    @SerializedName("status")
    private String status;

    public Geo() {
    }

    public Geo(ArrayList<Results> results, String status) {
        this.results = results;
        this.status = status;
    }

    public ArrayList<Results> getResults() {
        return results;
    }

    public void setResults(ArrayList<Results> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
