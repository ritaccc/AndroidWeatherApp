package com.mengxuan.cityview.weatherUtils;

import java.io.Serializable;

public class Weather implements Serializable {
    public String mainWeather;
    public String descriptionWeather;
    public Double temp;
    public Double tempMax;
    public Double tempMin;

    public Weather(String mainWeather, String descriptionWeather, Double temp, Double tempMax, Double tempMin) {
        this.mainWeather = mainWeather;
        this.descriptionWeather = descriptionWeather;
        this.temp = temp;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
    }

    public static String getTempString(Double curTemp, boolean isC) {
        Double tempFmt = 0.0;
        if (isC) {
            tempFmt = curTemp - 273.15;
        } else {
            tempFmt = curTemp * 9/5 - 459.67;
        }
        return String.format("%.2f", tempFmt)+ (char) 0x00B0 + (isC?"C":"F");
    }
}
