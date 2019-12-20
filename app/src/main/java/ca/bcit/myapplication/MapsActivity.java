package ca.bcit.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private ProgressDialog pDialog;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    boolean bool = true;
    private Marker CurrentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;
    private String SERVICE_URL = "http://api.translink.ca/rttiapi/v1/stops/";
    public static ArrayList<Art> artArray = new ArrayList<>();
    public static ArrayList<BusStop> busArray = new ArrayList<>();
    public ArrayList<String> latitudeStoreBus = new ArrayList<>();
    public ArrayList<String> nameStore = new ArrayList<>();
    public ArrayList<String> longitudeStoreBus = new ArrayList<>();

    /**
     * Create map activity fragment and center on new west. Add close by bus stops from translink API
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String json;
        //
        try {
            InputStream is = this.getAssets().open("publicArt.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject o = new JSONObject(json);

            JSONArray jsonArray = new JSONArray(o.getString("features"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject jsonArray1 = new JSONObject(jsonObject.getString("properties"));
                String name = jsonArray1.getString("Name");
                String x = jsonArray1.getString("X");
                String y = jsonArray1.getString("Y");
                Art art = new Art(name, x, y);
                artArray.add(art);

            }

            InputStream is_bus = this.getAssets().open("BUS_STOPS.json");
            int size_bus = is_bus.available();
            byte[] buffer_bus = new byte[size_bus];
            is_bus.read(buffer_bus);
            is_bus.close();

            json = new String(buffer_bus, StandardCharsets.UTF_8);
            JSONObject bus_object = new JSONObject(json);

            JSONArray jsonArray_bus = new JSONArray(bus_object.getString("features"));
            for (int i = 0; i < jsonArray_bus.length(); i++) {
                JSONObject jsonObject_bus = jsonArray_bus.getJSONObject(i);
                JSONObject jsonArray1 = new JSONObject(jsonObject_bus.getString("properties"));
                String name = jsonArray1.getString("stop_code");
                String x = jsonArray1.getString("X");
                String y = jsonArray1.getString("Y");
                BusStop busStop = new BusStop(name, x, y);
                if (busStop.getStop_code() != "") {
                    busArray.add(busStop);
                }
            }
            for (int i = 0; i < busArray.size(); i++) {
                latitudeStoreBus.add(busArray.get(i).getY());
                longitudeStoreBus.add(busArray.get(i).getX());
                nameStore.add(busArray.get(i).getStop_code());
            }


        } catch (IOException ex) {
            ex.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        bool = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        bool = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        bool = true;
    }

    protected void searchBtnPress(View v) {

        final String last_part_of_url = "/estimates?apikey=9UuAuljUvZpcSYfft7yM&count=3&timeframe=120";
        EditText busstop = findViewById(R.id.number_enter);
        final String busStopNo = busstop.getText().toString();
        final String reqUrl = SERVICE_URL + busStopNo + last_part_of_url;

        new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Showing progress dialog

                pDialog = new ProgressDialog(MapsActivity.this);
                pDialog.setMessage("Please wait...");
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                HttpHandler sh = new HttpHandler();
                String jsonStr = sh.makeServiceCall(reqUrl);
                System.out.println("ssss" + jsonStr);
                bool = true;

                if (jsonStr.charAt(2) == 'C') {
                    System.out.println(jsonStr.charAt(2));
                    bool = false;
                    System.out.println("BOOelan: +s--------------- " + bool);

                }

                return bool;
            }

            @Override
            protected void onPostExecute(Object o) {

                if (pDialog.isShowing())
                    pDialog.dismiss();

                if (bool) {
                    // bool = false;
                    Intent intent_name = new Intent();
                    intent_name.setClass(getApplicationContext(), ChooseBusActivity.class);
                    intent_name.putExtra("busStop", busStopNo);
                    startActivity(intent_name);


                } else if (!(bool)) {
                    Toast.makeText(getApplicationContext(), "Invalid Stop Number", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(bool);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //----------------------------------------------------------------------------------------------------
    //Starting point of code to set the marker to user's current location on the map.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        float zoomLevel = 16.0f;
        LatLng newWest = new LatLng(49.2057, -122.910956);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.addMarker(new MarkerOptions().position(newWest).title("Position in NewWestMinster"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newWest, zoomLevel));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (latitudeStoreBus.size() > 0) {
            for (int i = 0; i < latitudeStoreBus.size(); i++) {
                LatLng loc2 = new LatLng(Double.parseDouble(latitudeStoreBus.get(i)), Double.parseDouble(longitudeStoreBus.get(i)));

                mMap.addMarker(new MarkerOptions().position(loc2)
                        .title(nameStore.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        }


    }

    public boolean checkUserLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        } else {
            return true;
        }
    }

    //THis method Handles permission Request Response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (googleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    protected void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (CurrentUserLocationMarker != null) {
            CurrentUserLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        System.out.println(latLng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        CurrentUserLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));

        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //Ending point of code to set the marker to user's current location on the map.
    //----------------------------------------------------------------------------------------------------


}
