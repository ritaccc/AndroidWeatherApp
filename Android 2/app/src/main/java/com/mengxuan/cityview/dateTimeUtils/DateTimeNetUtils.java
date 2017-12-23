package com.mengxuan.cityview.dateTimeUtils;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;

public class DateTimeNetUtils {

    final static String APPID = "AIzaSyC2herKmfIWP-oAG9bJcUdCYnW2mcK005U";

    final static String WEATHER_BASE_URL =
            "https://maps.googleapis.com/maps/api/timezone/json";

    final static String LOCATION_QUERY = "location";
    final static String TIMESTAMP_QUERY = "timestamp";
    final static String APPID_QUERY = "APPID";


    /**
     * Builds the URL used to query GoogleTimeZone API.
     *
     * @param lat The latitude of the city.
     * @param lon The longitude of the city.
     * @return The URL to use to query the weather server.
     */
    public static URL buildUrl(String lat, String lon, double timeStamp) {
        // Fill in this method to build the proper Github query URL
        Uri builtUri = Uri.parse(WEATHER_BASE_URL).buildUpon()
                .appendQueryParameter(LOCATION_QUERY, lat + ","+ lon)
                .appendQueryParameter(TIMESTAMP_QUERY, String.valueOf(timeStamp))
                .appendQueryParameter(APPID_QUERY, APPID)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
