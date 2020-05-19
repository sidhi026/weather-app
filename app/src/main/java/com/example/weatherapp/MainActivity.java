package com.example.weatherapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationManagerCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView city;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double longitude;
    private double latitude;
    private Location locationObject;
    boolean exit=false;
    long lastUpdateTime;
//    private UpdateText updatedThread;
    String TAG = "Weather_App";
    private Criteria criteria;
    private TextView area, updated_atTxt, degree, weather;
    private TextView time2;
    private TextView textView3;
    private ConstraintLayout myLayout;
    private String bestProvider;


    private final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Calendar calender = Calendar.getInstance();
        Calendar calander = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        String time = simpleDateFormat.format(calander.getTime());
        String temp = time.substring(0, 2);
        int i = Integer.parseInt(temp);
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calender.getTime());
        TextView day = findViewById(R.id.day);
        weather = (TextView) findViewById(R.id.weather);
        degree = (TextView) findViewById(R.id.degree);

        day.setText(currentDate);

        city = (TextView) findViewById(R.id.city);
        area = (TextView) findViewById(R.id.area);

        pl.droidsonroids.gif.GifImageView gf = (GifImageView) findViewById(R.id.abcd);
        updated_atTxt = findViewById(R.id.updated_at);
        myLayout = findViewById(R.id.mainlayout);
        if (i >= 19 || i <= 5) {
            gf.setVisibility(View.VISIBLE);
            weather.setTextColor(Integer.parseInt("ffffff", 16) + 0xFF000000);
            area.setTextColor(Integer.parseInt("ffffff", 16) + 0xFF000000);
            degree.setTextColor(Integer.parseInt("ffffff", 16) + 0xFF000000);
            updated_atTxt.setTextColor(Integer.parseInt("ffffff", 16) + 0xFF000000);
        } else {
            gf.setVisibility(View.INVISIBLE);
            weather.setTextColor(Integer.parseInt("000000", 16) + 0xFF000000);
            area.setTextColor(Integer.parseInt("000000", 16) + 0xFF000000);
            degree.setTextColor(Integer.parseInt("000000", 16) + 0xFF000000);
            updated_atTxt.setTextColor(Integer.parseInt("000000", 16) + 0xFF000000);
        }
//        getLocation();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG,"Inside onLocationChanged");
                latitude=location.getLatitude();
                longitude=location.getLongitude();
                loc_func();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                }, 10);
            } else {
                updateLocation();
            }
            return;
        }
    }
public void updateLocation(){
        try {
            locationObject=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(locationObject!=null){
                latitude=locationObject.getLatitude();
                longitude=locationObject.getLongitude();
                loc_func();
                Log.d(TAG,"Hey");
            }
            if(!isLocationEnabled(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500000,1000,locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500000,1000,locationListener);
            Log.d(TAG,"Here");
        }catch (SecurityException e){}

}

    private void loc_func() {
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = null;
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String city = addresses.get(0).getLocality();
            String area = addresses.get(0).getSubLocality();
            Log.d("Checking", city);
            this.city.setText(city);
            this.area.setText(area);
            find_weather();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error:" + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void find_weather() {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=07d1276b5795099b69cd970a6e80a5f9&units=metric";
        Log.d(TAG, "Request made to API");
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_obj = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String temperature = String.valueOf(main_obj.getDouble("temp"));
                    String description = object.getString("description");
                    String city = response.getString("name");

                    degree.setText(temperature);
                    weather.setText(description);
                    exit=true;
                    lastUpdateTime=System.currentTimeMillis();
                    updateTextThread();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {

            }
        }
        );
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }

    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }
    public static boolean isLocationEnabled(Context context)
    {
        int locationMode = 0;
        String locationProviders;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            try
            {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else
        {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
    private void updateTextThread() {

        new Thread() {
            public void run() {
                exit=false;
                while (!exit) {

                    try {
                        runOnUiThread(new Runnable() {
                            long currentTime,mins;
                            String u;
                            @Override
                            public void run() {
                                currentTime=System.currentTimeMillis();
                                mins=(currentTime-lastUpdateTime)/60000;
                                if(mins>60){
                                    u=""+(mins/60)+" hr"+" "+(mins%60);
                                }else{
                                    u=""+mins%60;
                                }
                                updated_atTxt.setText("Last Updated "+u+" mins ago");
                            }
                        });
                        Thread.sleep(60000);
//                        Log.d(TAG,"Resuming ");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}

