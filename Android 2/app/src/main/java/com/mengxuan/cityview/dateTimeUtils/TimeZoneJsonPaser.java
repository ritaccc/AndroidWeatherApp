package com.mengxuan.cityview.dateTimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class TimeZoneJsonPaser {
    public static String parseJson(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            String timeZoneId = jsonObject.getString("timeZoneId");


            return timeZoneId;

        } catch (JSONException e) {
            return null;
        }
    }

}
