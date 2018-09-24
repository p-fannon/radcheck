package net.radcheck.radcheck.models;

import com.google.gson.annotations.SerializedName;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Table(name = "location")
@ControllerAdvice
public class LatLon {

    @Id
    @GeneratedValue
    @Column(name = "location_id")
    @SerializedName("id")
    private int id;
    @NotNull
    @SerializedName("lat")
    private double lat;
    @NotNull
    @SerializedName("lon")
    private double lon;
    @SerializedName("rating")
    private String rating;
    @SerializedName("rad_value")
    private double radValue;
    @SerializedName("total_meas")
    private int totalMeasurements;
    @SerializedName("aqi_value")
    private int aqiValue;
    @SerializedName("wind_speed")
    private double windSpeed;
    @SerializedName("wind_dir")
    private int windDirection;
    @SerializedName("temp")
    private int temp;
    @SerializedName("main_pollutant")
    private String mainPollutant;
    @SerializedName("weather_icon")
    private String weatherIcon;
    @SerializedName("city")
    private String city;
    @SerializedName("country")
    private String country;
    @SerializedName("view_count")
    private int viewCount;
    @SerializedName("current")
    private boolean isCurrent;
    @CreationTimestamp
    @SerializedName("create_ts")
    private Timestamp creationTimestamp;
    @UpdateTimestamp
    @SerializedName("update_ts")
    private Timestamp updateTimestamp;
    @SerializedName("min_meas_ts")
    private Timestamp minMeasurementTimestamp;
    @SerializedName("max_meas_ts")
    private Timestamp maxMeasurementTimestamp;
    @SerializedName("aqi_ts")
    private Timestamp aqiTimestamp;

    public LatLon() {
    }

    public LatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public int getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public double getRadValue() {
        return radValue;
    }

    public void setRadValue(double radValue) {
        this.radValue = radValue;
    }

    public int getTotalMeasurements() {
        return totalMeasurements;
    }

    public void setTotalMeasurements(int totalMeasurements) {
        this.totalMeasurements = totalMeasurements;
    }

    public int getAqiValue() {
        return aqiValue;
    }

    public void setAqiValue(int aqiValue) {
        this.aqiValue = aqiValue;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public String getMainPollutant() {
        return mainPollutant;
    }

    public void setMainPollutant(String mainPollutant) {
        this.mainPollutant = mainPollutant;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public Timestamp getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Timestamp creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Timestamp getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Timestamp updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Timestamp getMinMeasurementTimestamp() {
        return minMeasurementTimestamp;
    }

    public void setMinMeasurementTimestamp(Timestamp minMeasurementTimestamp) {
        this.minMeasurementTimestamp = minMeasurementTimestamp;
    }

    public Timestamp getMaxMeasurementTimestamp() {
        return maxMeasurementTimestamp;
    }

    public void setMaxMeasurementTimestamp(Timestamp maxMeasurementTimestamp) {
        this.maxMeasurementTimestamp = maxMeasurementTimestamp;
    }

    public Timestamp getAqiTimestamp() {
        return aqiTimestamp;
    }

    public void setAqiTimestamp(Timestamp aqiTimestamp) {
        this.aqiTimestamp = aqiTimestamp;
    }

}
