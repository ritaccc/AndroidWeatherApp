package com.mengxuan.cityview.weatherUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherJsonPaser {
    public static Weather parseJson(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray weatherJsonArr = jsonObject.getJSONArray("weather");
            JSONObject weatherJson = weatherJsonArr.getJSONObject(0);
            String mainWeather = weatherJson.getString("main");
            String descriptionWeather = weatherJson.getString("description");

            JSONObject mainJson = jsonObject.getJSONObject("main");
            Double temp = Double.valueOf(mainJson.getString("temp"));
            Double tempMax = Double.valueOf(mainJson.getString("temp_max"));
            Double tempMin = Double.valueOf(mainJson.getString("temp_min"));

            return new Weather(mainWeather, descriptionWeather, temp, tempMax, tempMin);

        } catch (JSONException e) {
            return null;
        }
    }

}
