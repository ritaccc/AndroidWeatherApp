package com.mengxuan.cityview;

import com.mengxuan.cityview.weatherUtils.Weather;
import java.io.Serializable;


public class City implements Serializable {
    public Weather weather;
    public String name;
    public Double lat;
    public Double lon;
    public String timeZoneId;

    public City(Weather weather, String name, Double lat, Double lon, String timeZoneId) {
        this.weather = weather;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.timeZoneId = timeZoneId;
    }
}
