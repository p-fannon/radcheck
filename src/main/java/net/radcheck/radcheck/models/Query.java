package net.radcheck.radcheck.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "query")
public class Query {

    @Id
    @GeneratedValue
    private int id;
    private String rating;
    private double radValue;
    private int aqiValue;
    private double windSpeed;
    private int windDirection;
    private int temp;
    private String mainPollutant;
    private String weatherIcon;
    private String city;
    private String country;
    private int viewCount;
    private boolean isCurrent;
    @OneToOne(mappedBy = "query")
    private LatLon location;
    @CreationTimestamp
    private Timestamp creationTimestamp;
    @UpdateTimestamp
    private Timestamp updateTimestamp;
    private Timestamp minMeasurementTimestamp;
    private Timestamp maxMeasurementTimestamp;
    private Timestamp aqiTimestamp;

    public Query() {
    }

    public Query(String rating, double radValue, int aqiValue, double windSpeed, int windDirection, int temp, String mainPollutant, String weatherIcon, String city, String country, int viewCount, boolean isCurrent) {
        this.rating=rating;
        this.radValue=radValue;
        this.aqiValue=aqiValue;
        this.windSpeed=windSpeed;
        this.windDirection=windDirection;
        this.temp=temp;
        this.mainPollutant = mainPollutant;
        this.weatherIcon=weatherIcon;
        this.city = city;
        this.country = country;
        this.viewCount=viewCount;
        this.isCurrent=isCurrent;
    }

    public int getId() {
        return id;
    }

    public String getRating() {
        return rating;
    }

    public double getRadValue() {
        return radValue;
    }

    public int getAqiValue() {
        return aqiValue;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public int getTemp() {
        return temp;
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

    public LatLon getLocation() {
        return location;
    }

    public Timestamp getCreationTimestamp() {
        return creationTimestamp;
    }

    public Timestamp getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setRadValue(double radValue) {
        this.radValue = radValue;
    }

    public void setAqiValue(int aqiValue) {
        this.aqiValue = aqiValue;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public void setLocation(LatLon location) {
        this.location = location;
    }

    public void setUpdateTimestamp(Timestamp updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
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
        this.viewCount=viewCount;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent=current;
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
