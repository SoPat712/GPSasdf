package com.jp12.gps;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView textView, textView2;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    private final int REQUEST_PERMISSION_COARSE_LOCATION = 1;
    Location location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int fineLoc = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLoc = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (fineLoc != PackageManager.PERMISSION_GRANTED && coarseLoc != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    showExplanation("Please allow Location Permission", "This app won't work without it", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                } else {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
                }
            }
        } else {
            System.out.println("allowed already");
            activateLocationService();
        }
    }

    @SuppressLint("MissingPermission")
    private void activateLocationService() {
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new GPSListener();
        System.out.println("activated stuff");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, (float) 0.1, locationListener);
        System.out.println("updating?");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                    activateLocationService();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private class GPSListener implements LocationListener {
        
        @Override
        public void onLocationChanged(Location loc) {
            System.out.println("loc changed");
            if(location == null){
                System.out.println("asdfasdfasf");
                location = loc;
            }
            textView.setText("");
            String cityName = null;
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                cityName = addresses.get(0).getAddressLine(0);
                System.out.println(cityName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String s;

            if(location != null) {
                s = "Starting long: "+location.getLongitude() + "\n" + "Starting lat: " + location.getLatitude() + "\n" +"Longitude: " + loc.getLongitude() + "\n" + "Latitude: " + loc.getLatitude() + "\n\nAddress: "
                        + cityName;

            } else{
                s = "Starting long: "+"..." + "\n" + "Starting lat: " + "..." + "\n" +"Longitude: " + loc.getLongitude() + "\n" + "Latitude: " + loc.getLatitude() + "\n\nAddress: "
                        + cityName;
            }
            System.out.println("adsf: "+s);
            textView.setText(s);
            if(location != null){
                textView2.setText(loc.distanceTo(location) + " m");

            }
        }

    }
}