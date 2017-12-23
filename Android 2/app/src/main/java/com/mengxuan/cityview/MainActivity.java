package com.mengxuan.cityview;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.mengxuan.cityview.dateTimeUtils.DateTimeNetUtils;
import com.mengxuan.cityview.dateTimeUtils.TimeZoneJsonPaser;
import com.mengxuan.cityview.weatherUtils.NetworkUtils;
import com.mengxuan.cityview.weatherUtils.Weather;
import com.mengxuan.cityview.weatherUtils.WeatherJsonPaser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Adapter.ListItemClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        Adapter.ListItemRemoveClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    protected GeoDataClient mGeoDataClient;
    private static final String TAG = "MainActivityModule";

    //Id to identify a contacts permission request.
    public static final int REQUEST_LOCATION = 1;
    private Adapter mAdapter;
    private RecyclerView mCitysList;
    private String mCurrentCity = "";
    private PlaceDetectionClient mPlaceDetectionClient;
    private Weather mWeather;
    private Double mLat, mLon;
    private boolean isC = true;
    private String mTimeZone = "";
    private double timeStamp= 0;
    private boolean timeZoneTaskFinished = false;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location currentLocation;
    Geocoder gcd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCitysList = (RecyclerView) findViewById(R.id.tv_citys);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mCitysList.setLayoutManager(layoutManager);
        mCitysList.setHasFixedSize(true);
        gcd = new Geocoder(this, Locale.getDefault());
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        /*
         * The Adapter is responsible for displaying each item in the list.
         */
        mAdapter = new Adapter(this, this, isC);
        mCitysList.setAdapter(mAdapter);

        setupSharedPreferences();
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        checkPermission();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mCurrentCity = place.getName().toString();
                mLat = place.getLatLng().latitude;
                mLon = place.getLatLng().longitude;

                addCity(mCurrentCity, mLat, mLon);
            }

            @Override
            public void onError(Status status) {
                // TODO:Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        final Button currentLocationBut = (Button) findViewById(R.id.current_place_button);
        currentLocationBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLocation != null) {
                    mLat = currentLocation.getLatitude();
                    mLon = currentLocation.getLongitude();
                    try {
                        List<Address> addresses = gcd.getFromLocation(mLat, mLon, 1);
                        mCurrentCity = addresses.get(0).getLocality();
                        mAdapter.setMyLocationName(mCurrentCity);
                        if (addresses.size() > 0) {
                            addCity(mCurrentCity, mLat, mLon);
                        }
                    } catch (IOException e) {
                        Log.i(TAG, "An error occurred when get current location.");
                    }
                }
            }
        });
    }
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }
    protected void createLocationRequest() {
        //remove location updates so that it resets
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this); //Import should not be **android.Location.LocationListener**
        //import should be **import com.google.android.gms.location.LocationListener**;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //restart location updates with the new interval
        if (checkPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    private void addCity(String mCityName, double latitude, double longitude) {
        timeZoneTaskFinished = false;
        timeStamp = Calendar.getInstance().getTimeInMillis()/1000;
        URL timeZoneSearchUrl = DateTimeNetUtils.buildUrl(Double.toString(latitude), Double.toString(longitude), timeStamp);
        new TimeZoneQueryTask().execute(timeZoneSearchUrl);
        URL weatherSearchUrl = NetworkUtils.buildUrl(Double.toString(latitude), Double.toString(longitude));
        new WeatherQueryTask().execute(weatherSearchUrl);

    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        return true;
    }

    // Change the name of default setup to setupSharedPreferences
    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        // Get a reference to the default shared preferences from the PreferenceManager class
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Get the value of the show_C checkbox preference and set to isC
        isC = sharedPreferences.getBoolean(getString(R.string.pref_show_C_key),
                getResources().getBoolean(R.bool.pref_show_C_default));
        mAdapter.setIsC(isC);
        mCitysList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_show_C_key))) {
            isC = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_C_default));
            mAdapter.setIsC(isC);
            mAdapter.notifyDataSetChanged();
        }
    }

    // Override onDestroy and unregister the listener
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        mGoogleApiClient.disconnect();
    }

    // Create a class called WeatherQueryTask that extends AsyncTask<URL, Void, String>
    public class WeatherQueryTask extends AsyncTask<URL, Void, String> {

        // Override the doInBackground method to perform the query. Return the results.
        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String weatherResults = null;
            try {
                weatherResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return weatherResults;
        }

        // Override onPostExecute to display the results in the TextView
        @Override
        protected void onPostExecute(String weatherResults) {
            if (weatherResults != null && !weatherResults.equals("")) {
                mWeather = WeatherJsonPaser.parseJson(weatherResults);
                while( timeZoneTaskFinished == false )
                {
                    try
                    {
                        Log.d("myApp", "Waiting for timeZone task finish");
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                //mWeatherResultsTextView.setText(weatherResults);

                City curCity = new City(mWeather, mCurrentCity, mLat, mLon, mTimeZone);
                mAdapter.addNewCitry(curCity);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    // Create a class called TimeZoneQueryTask that extends AsyncTask<URL, Void, String>
    public class TimeZoneQueryTask extends AsyncTask<URL, Void, String> {

        // Override the doInBackground method to perform the query. Return the results.
        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String tiumeZoneResults = null;
            try {
                tiumeZoneResults = DateTimeNetUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return tiumeZoneResults;
        }

        // Override onPostExecute to display the results in the TextView
        @Override
        protected void onPostExecute(String timezoneResults) {
            if (timezoneResults != null && !timezoneResults.equals("")) {
                mTimeZone = TimeZoneJsonPaser.parseJson(timezoneResults);
                timeZoneTaskFinished = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.main, menu);
        /* Return true so that the visualizer_menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int number) {
        Intent intent = new Intent(MainActivity.this, ChildActivity.class);
        ArrayList<City> cityList = mAdapter.getCityList();
        intent.putExtra(getString(R.string.city_name), cityList.get(number).name)
                .putExtra(getString(R.string.temp_isc), isC)
                .putExtra(getString(R.string.city_number), number)
                .putExtra(getString(R.string.my_city), mAdapter.getMyLocationName())
                .putExtra(getString(R.string.city_list), cityList);
        startActivity(intent);
    }

    @Override
    public void onListItemRemoveClick(int number) {
        mAdapter.removeCity(number);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"onConnected",Toast.LENGTH_SHORT).show();
        if(checkPermission()) {
            createLocationRequest();
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "location :"+location.getLatitude()+" , "+location.getLongitude(), Toast.LENGTH_SHORT).show();
    }
}

