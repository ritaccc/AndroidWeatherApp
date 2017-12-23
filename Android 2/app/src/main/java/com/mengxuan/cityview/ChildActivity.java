package com.mengxuan.cityview;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mengxuan.cityview.weatherUtils.Weather;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

public class ChildActivity extends AppCompatActivity {

    TextView mDisplayText;
    private ViewFlipper mViewFlipper;
    private Intent mIntent;
    RelativeLayout r_layout;

    TextView textView;
    TextView tempView;
    TextView dateView;
    TextView highTempView;
    TextView lowTempView;
    TextView myLocationView;
    TextView statusView;
    String currentWeather;
    String hourlyForecast;
    City currentCity;
    String timeZone;
    String myCity = "";
    boolean isC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        textView = (StrokeTextView)findViewById(R.id.city);
        tempView = (StrokeTextView)findViewById(R.id.weather);
        dateView = (StrokeTextView)findViewById(R.id.date);
        highTempView = (StrokeTextView)findViewById(R.id.highTemp);
        lowTempView = (StrokeTextView)findViewById(R.id.lowTemp);
        statusView = (StrokeTextView)findViewById(R.id.status);
        myLocationView = (StrokeTextView)findViewById(R.id.myLocation);

        mIntent = getIntent();
        int cityNumber = mIntent.getIntExtra(getString(R.string.city_number), 0);
        isC = mIntent.getBooleanExtra(getString(R.string.temp_isc), true);
        myCity = mIntent.getStringExtra(getString(R.string.my_city));
        ArrayList<City> cityList = (ArrayList<City>) mIntent.getSerializableExtra(getString(R.string.city_list));
        currentCity = cityList.get(cityNumber);
        timeZone = currentCity.timeZoneId;
        Double lat = currentCity.lat;
        Double lon = currentCity.lon;
        setTime();


        CurrentWeatherTask currentWeatherTask = new CurrentWeatherTask();
        if(lat == null || lon == null){
            currentWeatherTask.execute(new String[]{"/weather","37", "122", "/forecast"});
        }else{
            currentWeatherTask.execute(new String[]{"/weather",lat+"", lon+"", "/forecast"});
        }


        //mDisplayText = (TextView) findViewById(R.id.tv_display);
        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        if (mIntent.hasExtra(getString(R.string.city_name))) {
            String cityName = mIntent.getStringExtra(getString(R.string.city_name));
            textView.setText(cityName);
        }

        if(myCity.equals(currentCity.name)) {
            myLocationView.setText("(You are here!)");
        }

        ActionBar actionBar = this.getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mViewFlipper.setOnTouchListener(new OnSwipeTouchListener(ChildActivity.this) {
            public void onSwipeLeft() {
                if (mIntent.hasExtra(getString(R.string.city_number))) {
                    int cityNumber = mIntent.getIntExtra(getString(R.string.city_number), 0) + 1;
                    ArrayList<City> cityList = (ArrayList<City>) mIntent.getSerializableExtra(getString(R.string.city_list));
                    if (cityNumber<cityList.size()) {
                        mIntent.putExtra(getString(R.string.city_number), cityNumber)
                                .putExtra(getString(R.string.city_list), cityList)
                                .putExtra(getString(R.string.my_city), myCity)
                                .putExtra(getString(R.string.city_name), cityList.get(cityNumber).name);
                        startActivity(mIntent);
                    }
                }
            }

            public void onSwipeRight() {
                if (mIntent.hasExtra(getString(R.string.city_number))) {
                    int cityNumber = mIntent.getIntExtra(getString(R.string.city_number), 0)-1;
                    if (cityNumber >= 0) {
                        ArrayList<City> cityList = (ArrayList<City>) mIntent.getSerializableExtra(getString(R.string.city_list));
                        mIntent.putExtra(getString(R.string.city_number), cityNumber)
                                .putExtra(getString(R.string.city_list), cityList)
                                .putExtra(getString(R.string.my_city), myCity)
                                .putExtra(getString(R.string.city_name), cityList.get(cityNumber).name);
                        startActivity(mIntent);
                    }
                }
            }
        });
    }

    private class CurrentWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            if(urls.length == 4)
                return apiCall(urls[0], urls[1], urls[2]) + "~" + apiCall(urls[3], urls[1], urls[2]);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null) {
                String[] two_parts = result.split("~");
                currentWeather = two_parts[0];
                hourlyForecast = two_parts[1];
            }
            updateHrView();
            update5dView();
        }
    }

    private void updateHrView(){
        JsonElement jsonElement = new JsonParser().parse(currentWeather);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        Weather cityWeather = currentCity.weather;
        String weather = jsonObject.get("main").getAsJsonObject().get("temp").toString();
        String high = jsonObject.get("main").getAsJsonObject().get("temp_max").toString();
        String low = jsonObject.get("main").getAsJsonObject().get("temp_min").toString();
        JsonArray statusJ = jsonObject.get("weather").getAsJsonArray();
        String status = statusJ.get(0).getAsJsonObject().get("main").getAsString();
//        Bundle bundle = getArguments();
//        String message = Integer.toString(bundle.getInt("count"));
        tempView.setText(Weather.getTempString(cityWeather.temp, isC));
        highTempView.setText(Weather.getTempString(cityWeather.tempMax, isC));
        lowTempView.setText(Weather.getTempString(cityWeather.tempMin, isC));
        statusView.setText(status);
        setBackground(status);

        JsonArray list_hr = getForecastList();
        for(int i=0; i<=24; i=i+3){
            String time = list_hr.get(i/3).getAsJsonObject().get("dt_txt").getAsString();
            TextView timeView = getViewByHr("hr",i);
            String[] hour=utcToLocal(time).split("T");
            String[] hour_number=hour[1].split(":");
            timeView.setText(hour_number[0]+":"+hour_number[1]);
            String weather_hr = list_hr.get(i/3).getAsJsonObject().get("main").getAsJsonObject().get("temp").toString();
            JsonArray weather_array = list_hr.get(i/3).getAsJsonObject().get("weather").getAsJsonArray();
            String status_hr = weather_array.get(0).getAsJsonObject().get("main").getAsString();
            TextView tempHrView = getViewByHr("temp", i);
            tempHrView.setText(getTempString(weather_hr,isC));
            TextView statusHrView = getViewByHr("status", i);
            statusHrView.setText(status_hr);
        }
    }

    private void setTime(){
        //get local date
        //String currentTime = DateFormat.getDateInstance().format(new Date());
        String[] currentTime = getLocalTime().toString().split("-");
        int dayOfWeek = getLocalTime().getDayOfWeek();
        String dayofWeek_s = "";
        switch(dayOfWeek){
            case 1: dayofWeek_s = "Mon";
                    break;
            case 2: dayofWeek_s = "Tue";
                    break;
            case 3: dayofWeek_s = "Wed";
                    break;
            case 4: dayofWeek_s = "Thur";
                break;
            case 5: dayofWeek_s = "Fri";
                break;
            case 6: dayofWeek_s = "Sat";
                break;
            case 7: dayofWeek_s = "Sun";
                break;
            default:
                Log.d("Exception","error with day of week");
        }
        dateView.setText(dayofWeek_s+" "+currentTime[1]+"-"+currentTime[2].split("T")[0]);

    }

    private String apiCall(String path, String lat_s, String lon_s){
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http").setHost("api.openweathermap.org/data/2.5").setPath(path)
                    .setParameter("lat", lat_s)
                    .setParameter("lon", lon_s)
                    .setParameter("APPID", "e0a763375647ff09fc7c5772435113ba");
            URI uri = builder.build();
            HttpGet getRequest = new HttpGet(uri);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(getRequest);
            return EntityUtils.toString(response.getEntity());

        }catch(Exception e) {
            return null;
        }
    }

    private TextView getViewByHr(String param, int hr){
        String temp = param+hr;
        int resID = this.getResources().getIdentifier(temp, "id", this.getPackageName());
        return (TextView)findViewById(resID);
    }

    private void update5dView(){
        try {
            double high = 0l;
            double low = 0l;
            TextView date;
            TextView high5View = null;
            TextView low5View = null;
            TextView status5View = null;

            JsonArray list_hr = getForecastList();
            ArrayList<String[]> list_array = formatWeather(list_hr);
            //get local date
            //Date dt = new Date();
            //DateTime dtOrg = new DateTime(dt);
            DateTime dtOrg = getLocalTime();
            int j=0;
            for(int i=0;i<4;i++){
                date = getViewByHr("Day", i + 1);
                String nextDate = dtOrg.plusDays(i + 1).toLocalDate().toString();
                String[] nextDate_array = nextDate.split("-");
                //String day = nextDate[2];
                date.setText(nextDate_array[1]+"-"+nextDate_array[2]);

                high5View = getViewByHr("Day_h", i + 1);
                low5View = getViewByHr("Day_l", i + 1);
                status5View = getViewByHr("Day_s", i + 1);
                high = -999.99;
                low = 999.99;
                while(compareDate((list_array.get(j))[0],nextDate)){
                    j += 1;
                }

                while(j<list_array.size() && (list_array.get(j))[0].equals(nextDate)){
                    String newTemp_s = list_array.get(j)[2];
                    double newTemp = Double.parseDouble(newTemp_s);
                    if (Double.valueOf(newTemp).compareTo(Double.valueOf(high)) > 0) high = newTemp;
                    if (Double.valueOf(newTemp).compareTo(Double.valueOf(low)) < 0) low = newTemp;
                    if ((list_array.get(j))[1].equals("11") || (list_array.get(j))[1].equals("12") || (list_array.get(j))[1].equals("13"))
                        status5View.setText((list_array.get(j))[3]);
                    j += 1;
                }
                high5View.setText(getTempString(high + "",isC));
                low5View.setText(getTempString(low + "",isC));
            }

            /*for (int i = 0; i < list_hr.size(); i++) {
                if (i % 9 == 0) {
                    // update low and high to the 1st hr temp
                    // set up date

                    high5View = getViewByHr("Day_h", i / 10 + 1);
                    low5View = getViewByHr("Day_l", i / 10 + 1);
                    String temp_s = list_hr.get(i).getAsJsonObject().get("main").getAsJsonObject().get("temp").getAsString();
                    high = Double.parseDouble(temp_s);
                    low = high;
                    status5View = getViewByHr("Day_s", i / 10 + 1);
                }
                //get high and low temp
                //Long.valueOf(x).compareTo(Long.valueOf(y))
                String newTemp_s = list_hr.get(i).getAsJsonObject().get("main").getAsJsonObject().get("temp").getAsString();
                double newTemp = Double.parseDouble(newTemp_s);
                if (Double.valueOf(newTemp).compareTo(Double.valueOf(high)) > 0) high = newTemp;
                if (Double.valueOf(newTemp).compareTo(Double.valueOf(low)) < 0) low = newTemp;
                if ((i+1)%9==0 && high5View != null && low5View != null) {
                    // set up view text
                    high5View.setText(getTempString(high + "",isC));
                    low5View.setText(getTempString(low + "",isC));
                }
                if (i % 9 == 4 && status5View != null) {
                    // update status
                    JsonArray weather_array = list_hr.get(i).getAsJsonObject().get("weather").getAsJsonArray();
                    String newStatus = weather_array.get(0).getAsJsonObject().get("main").getAsString();
                    status5View.setText(newStatus);
                }

            }*/
        }catch(Exception e){
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }
    }

    private JsonArray getForecastList(){
        JsonElement jsonElement_hr = new JsonParser().parse(hourlyForecast);
        JsonObject jsonObject_hr = jsonElement_hr.getAsJsonObject();
        return jsonObject_hr.get("list").getAsJsonArray();
    }

    private void setBackground(String status){
        String status_noCase = status.toLowerCase();
        r_layout = (RelativeLayout)findViewById(R.id.relativeLayout);
        if(status_noCase.equals("clouds"))
            r_layout.setBackgroundResource(R.drawable.clouds);
        if(status_noCase.equals("rain"))
            r_layout.setBackgroundResource(R.drawable.rain);
        if(status_noCase.equals("clear"))
            r_layout.setBackgroundResource(R.drawable.clear);


    }

    private String getTempString(String temp, boolean isC) {
        Double temp_formatted = Double.parseDouble(temp);
        Double tempFmt = 0.0;
        if (isC) {
            tempFmt = temp_formatted - 273.15;
        } else {
            tempFmt = temp_formatted * 9/5 - 459.67;
        }
        return String.format("%.2f", tempFmt)+ (char) 0x00B0 + (isC?"C":"F");
    }

    //get local date and time
    private String utcToLocal(String utcTime){
        try{
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss z");
            DateTime dt = formatter.parseDateTime(utcTime+" UTC");
            dt = dt.withZone(DateTimeZone.UTC);
            TimeZone tz2 = TimeZone.getTimeZone(timeZone);
            DateTime localdt = new DateTime(dt, DateTimeZone.forID(tz2.getID()));
            return localdt.toString();
        } catch(Exception e){
            Log.d("Exception",e.getMessage());
        }
        return null;
    }

    private ArrayList<String[]> formatWeather(JsonArray weatherArray){
        ArrayList<String[]> formattedW =  new ArrayList<>();
        for(int i=0;i<weatherArray.size();i++){
            String[] weather = new String[4];
            String time = weatherArray.get(i).getAsJsonObject().get("dt_txt").getAsString();
            String[] hour_list=utcToLocal(time).split("T");
            // index 0: date in local time
            weather[0] = hour_list[0];
            String[] hour_number=hour_list[1].split(":");
            //index 1: hour number in local time
            weather[1] = hour_number[0];
            //index 2: weather temp
            weather[2] = weatherArray.get(i).getAsJsonObject().get("main").getAsJsonObject().get("temp").getAsString();
            //index 3: weather status
            JsonArray tempArray = weatherArray.get(i).getAsJsonObject().get("weather").getAsJsonArray();
            weather[3] = tempArray.get(0).getAsJsonObject().get("main").getAsString();
            formattedW.add(weather);
        }
        return formattedW;
    }

    private boolean compareDate(String firstDate, String nextDate){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime firstDate_d = formatter.parseDateTime(firstDate);
        String firstDate_s = firstDate_d.plusDays(1).toString();
        String nextDate_s = formatter.parseDateTime(nextDate).toString();
        return firstDate_s.equals(nextDate_s);
    }

    private DateTime getLocalTime(){
        DateTime utc = new DateTime(DateTimeZone.UTC);
        DateTimeZone tz = DateTimeZone.forID(timeZone);
        return utc.toDateTime(tz);
    }
}
