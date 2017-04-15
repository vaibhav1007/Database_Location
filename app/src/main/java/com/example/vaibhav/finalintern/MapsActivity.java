package com.example.vaibhav.finalintern;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        new CheckConnectionStatus().execute("https://anthropogenic-imple.000webhostapp.com/webapp/jason_get_data.php");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();

    }
    public void setUpMap() {

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        Location location = getLastKnownLocation();
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

    }
    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return TODO;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    //AsyncTask to process network request
    class CheckConnectionStatus extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            URL url = null;
            try {
                url = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


                InputStream inputStream = urlConnection.getInputStream();


                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String s = bufferedReader.readLine();
                bufferedReader.close();

                return s;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage(), e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            JSONObject jsonObject = null;
            try {

                jsonObject = new JSONObject(s);

                ArrayList<Info2>list2=new ArrayList<Info2>();

                JSONArray jsonArray = jsonObject.getJSONArray("server_response");

                Info2[] a2 = new Info2[jsonArray.length()];

                for (int i =0; i<jsonArray.length();i++)
                {
                    JSONObject object = jsonArray.getJSONObject(i);



                    a2[i]=new Info2(i,object.getString("name"), BigDecimal.valueOf(object.getDouble("lat")).floatValue(),BigDecimal.valueOf(object.getDouble("long")).floatValue());
                    list2.add(a2[i]);
                }

                for(Info2 b:list2){
                    LatLng abc = new LatLng(b.lati, b.longi);
                    System.out.println("--------------------------------------"+b.lati + " "+ b.longi);
                    //to locate all the loations,omit the next condition
                    if(distance(getLastKnownLocation().getLatitude(),getLastKnownLocation().getLongitude(),b.lati,b.longi)==1)
                    mMap.addMarker(new MarkerOptions().position(abc).title(b.name));

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private int distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        System.out.println("---------------------------------"+dist);
        if(dist<=80)
            return 1;
        else return 0;

    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
public void click(View view){
    Intent intent1 = new Intent(this, MapsActivity2.class);
    startActivity(intent1);
}

}
class Info2 {
    String name;
    int id;
    float lati,longi;
    public Info2(int id,String name, float lati, float longi) {
        this.id = id;
        this.name = name;
        this.lati = lati;
        this.longi = longi;
    }
}
