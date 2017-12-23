package com.mengxuan.cityview.weatherUtils;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    final static String APPID = "fe08e9cca54e25e1f183f5bf71f0fdcc";

    final static String WEATHER_BASE_URL =
            "http://api.openweathermap.org/data/2.5/weather";
    final static String BOOK_URL = "http://10.0.2.2:8080/books?author=aa";

    final static String LATITUDE_QUERY = "lat";
    final static String LONGITUDE_QUERY = "lon";
    final static String APPID_QUERY = "APPID";


    /**
     * Builds the URL used to query Weather.
     *
     * @param lat The latitude of the city.
     * @param lon The longitude of the city.
     * @return The URL to use to query the weather server.
     */
    public static URL buildUrl(String lat, String lon) {
        // Fill in this method to build the proper Github query URL
//        Uri builtUri = Uri.parse(WEATHER_BASE_URL).buildUpon()
//                .appendQueryParameter(LATITUDE_QUERY, lat)
//                .appendQueryParameter(LONGITUDE_QUERY, lon)
//                .appendQueryParameter(APPID_QUERY, APPID)
//                .build();

        Uri builtUri = Uri.parse(BOOK_URL).buildUpon().build();

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
