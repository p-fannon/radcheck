package net.radcheck.radcheck.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.sql.Date;

@Entity
public class Query {

    @Id
    @GeneratedValue
    private int id;
    private double rating;
    private double radValue;
    private int aqiValue;
    private double windSpeed;
    private int windDirection;
    private int temp;
    private String weatherIcon;
    @OneToOne(mappedBy = "query")
    private LatLon location;
    @CreationTimestamp
    private Date creationTimestamp;
    @UpdateTimestamp
    private Date updateTimestamp;

    public Query() {
    }

    public int getId() {
        return id;
    }

    public Query(double rating, double radValue, int aqiValue, double windSpeed, int windDirection, int temp, String weatherIcon) {
        this.rating = rating;
        this.radValue = radValue;
        this.aqiValue = aqiValue;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.temp = temp;
        this.weatherIcon = weatherIcon;
    }

    public double getRating() {
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

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public LatLon getLocation() {
        return location;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public Date getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setRating(double rating) {
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

    public void setUpdateTimestamp(Date updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }
}
