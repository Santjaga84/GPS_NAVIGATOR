package com.example.gps_navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int DEFAULT_UPDATE_INTERVAL = 3;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;

    private TextView tv_labellat;
    private TextView tv_lat;
    private TextView tv_labellon;
    private TextView tv_lon;
    private TextView tv_labelaltitude;
    private TextView tv_altitude;
    private TextView tv_labelaccuracy;
    private TextView tv_accuracy;
    private TextView tv_labelspeed;
    private TextView tv_speed;
    private TextView tv_lbladdress;
    private TextView tv_address;
    private TextView tv_labelupdates;
    private TextView tv_updates;
    private TextView tv_labelsensor;
    private TextView tv_sensor;
    private TextView tv_labelCrumbCounts;
    private TextView tv_countOfCrumbs;

    private View divider;

    private Button btn_newWayPoint;
    private Button btn_showWayPoint;
    private Button btn_showWap;

    Switch sw_locationsupdates;
    Switch sw_gps;

    //Location request is a config file all settings related to FusedLocationProvider

    //Google API for location services

    boolean updateOn = false;

    Location currentLocation;

    List<Location> savedLocations;

    FusedLocationProviderClient fusedLocationProviderClient;

    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {


        tv_labellat = findViewById(R.id.tv_labellat);
        tv_lat = findViewById(R.id.tv_lat);
        tv_labellon = findViewById(R.id.tv_labellon);
        tv_lon = findViewById(R.id.tv_lon);
        tv_labelaltitude = findViewById(R.id.tv_labelaltitude);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_labelaccuracy = findViewById(R.id.tv_labelaccuracy);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_labelspeed = findViewById(R.id.tv_labelspeed);
        tv_speed = findViewById(R.id.tv_speed);
        tv_lbladdress = findViewById(R.id.tv_lbladdress);
        tv_address = findViewById(R.id.tv_address);
        tv_lbladdress = findViewById(R.id.tv_lbladdress);
        tv_labelupdates = findViewById(R.id.tv_labelupdates);
        tv_updates = findViewById(R.id.tv_updates);
        tv_labelsensor = findViewById(R.id.tv_labelsensor);
        tv_sensor = findViewById(R.id.tv_sensor);

        tv_countOfCrumbs = findViewById(R.id.tv_countOfCrumbs);
        tv_labelCrumbCounts = findViewById(R.id.tv_labelCrumbCounts);

        divider = findViewById(R.id.divider);

        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPoint = findViewById(R.id.btn_showWayPoint);
        btn_showWap = findViewById(R.id.btn_showWap);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        sw_gps.setOnClickListener(this);
        sw_locationsupdates.setOnClickListener(this);

        btn_newWayPoint.setOnClickListener(this);
        btn_showWayPoint.setOnClickListener(this);
        btn_showWap.setOnClickListener(this);

        // event that is triggered whenver the update interval is met
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save the location

                updateUIValues(locationResult.getLastLocation());
            }

        };

//    MyApplication myApplication = (MyApplication) getApplicationContext();
//    savedLocations = myApplication.getMyLocations();
//        savedLocations.add(currentLocation);

    }
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

                case R.id.sw_gps: {
                    if (sw_gps.isChecked()) {
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        tv_sensor.setText("Using GPS sensors");
                    } else {
                        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        tv_sensor.setText("Using Towers + WIFI");
                    }
                    updateGPS();
                    break;
                }

                case R.id.sw_locationsupdates: {
                    if (sw_locationsupdates.isChecked()) {
                        //turn on location tracking
                        startLocationUpdates();
                    } else {
                        // turn off tracking
                        stopLocationUpdates();
                    }
                    //updateGPS();
                    break;
                }

            case R.id.btn_newWayPoint: {
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);
                break;
            }

            case R.id.btn_showWayPoint: {
                SavedLocationsList();
                break;
            }

            case R.id.btn_showWap: {
               startMaps();
                break;
            }
        }

    }

    private void startMaps() {
        Intent intentStartMaps = new Intent(this,MapsActivity.class);
        startActivity(intentStartMaps);
    }

    private void SavedLocationsList() {

        Intent intentStart = new Intent(this,ShowSavedLocationsList.class);
        startActivity(intentStart);
    }

    private void stopLocationUpdates() {


        tv_lat.setText("Not tracking location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not tracking location");
        tv_address.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");
        tv_sensor.setText("Not tracking location");
        tv_updates.setText("Location is being tracked");


        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);

    }


    private void startLocationUpdates() {

        tv_updates.setText("Location is NOT being tracked");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else {
                    Toast.makeText(this,"This app requires permission to be granted in enter to work property",Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void updateGPS(){
        //Get permissions from the user to track GPS
        //get the current location from the fused client
        //update the UI - i.e. set all properties in their associated text view items

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

                @Override
                public void onSuccess(Location location) {
                 // we got permissions. Put the values of location. XXX into the UI components.

                    updateUIValues(location);
                    currentLocation = location;
                }

            });
        }
        else {
            // permissions not granted yet.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }

    }

    private void updateUIValues(Location location) {

        //update all of the text view objects with a new location.
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));


        } else {
            tv_altitude.setText("Not available");
        }
        if (location.hasSpeed()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));

        } else {
            tv_altitude.setText("Not available");
        }

        Geocoder geocoder = new Geocoder(this);

        try {

            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e) {
            tv_address.setText("Unable to get street address");
        }

        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        //show the number of waypoints saved
        tv_countOfCrumbs.setText(Integer.toString(savedLocations.size()));
    }
}