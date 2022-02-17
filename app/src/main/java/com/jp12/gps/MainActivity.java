package com.jp12.gps;

import static android.os.SystemClock.elapsedRealtime;

import androidx.annotation.NonNull;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView textView, textView2;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    private final int REQUEST_PERMISSION_COARSE_LOCATION = 1;
    Location location = null;
    LocationManager locationManager;
    Float sumDist = 0.0f;
    LocationListener locationListener;
    ArrayList<String> locations = new ArrayList<>();
    ArrayList<Long> timeSpentLocation = new ArrayList<Long>();
    Long prevTimeOfUpdate = null;
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
            Log.d("TAG", "allowed already");
        }
    }
    public void onPressed(View v){
        Button b = findViewById(R.id.button);
        if(b.getText().toString().equalsIgnoreCase("start tracking")){
            sumDist = 0.0f;
            locations.clear();
            timeSpentLocation.clear();
            prevTimeOfUpdate = null;

            Location location = null;
            activateLocationService();
            b.setText("Stop tracking");
        } else{
            pauseLocationService();

            b.setText("Start tracking");

        }
    }
    @SuppressLint("MissingPermission")
    private void activateLocationService() {

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationListener = new GPSListener();
        Log.d("TAG", "activated stuff");

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,  0, locationListener);
        Log.d("TAG", "updating?");
    }
    private void pauseLocationService(){
        locationManager.removeUpdates(locationListener);
        sumDist = 0.0f;
        textView2.setText("0.0" + " m");
        Location location = null;
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
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    showExplanation("Please allow Location Permission", "This app won't work without it", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_FINE_LOCATION);
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
            Log.d("TAG","loc changed");
            try {
                if(location == null){
                    Thread.sleep(5000);
                } else{
                    if(loc.distanceTo(location) < 0.15){

                    }else{

                        sumDist += loc.distanceTo(location);
                    }
                }
                location = loc;
            } catch (Exception e) {
                e.printStackTrace();
            }
            String address = null;
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                address = addresses.get(0).getAddressLine(0);
                Log.d("TAG",address);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("TAG","addrContain: "+locations.contains(address));
            if(locations.contains(address)){
                Log.d("TAG","prevTimeUpdate: " + prevTimeOfUpdate);
                Log.d("TAG","elapsedRealtime: " + elapsedRealtime());
                Log.d("TAG","locations.indexOf: " + locations.indexOf(address));
                timeSpentLocation.set(locations.indexOf(address), timeSpentLocation.get(locations.indexOf(address)) + (elapsedRealtime() - prevTimeOfUpdate));
            }else{
                locations.add(address);
                Log.d("TAG", String.valueOf(locations));
                timeSpentLocation.add(0L);
                Log.d("TAG", String.valueOf(timeSpentLocation));
            }
            Long max = Collections.max(timeSpentLocation);
            int index = timeSpentLocation.indexOf(max);
            String s = "Longitude: " + loc.getLongitude() + "\n" + "Latitude: " + loc.getLatitude() + "\n\nAddress: "
                    + address + "\nMost Popular Location: " + locations.get(index) + "\nTime spent: "+ (timeSpentLocation.get(index)/1000)%60;
            Log.d("TAG","Time spent: "+ (timeSpentLocation.get(index)/1000)%60);
            Log.d("TAG","adsf: "+s);
            textView.setText(s);
            textView2.setText(sumDist + " m");
            prevTimeOfUpdate = elapsedRealtime();
        }
        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}