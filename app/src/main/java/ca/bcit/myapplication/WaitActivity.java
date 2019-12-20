package ca.bcit.myapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static ca.bcit.myapplication.MapsActivity.artArray;


public class WaitActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ProgressDialog pDialog;
    public ArrayList<String> latitudeStore = new ArrayList<>();
    public ArrayList<String> nameStore = new ArrayList<>();
    public ArrayList<String> timeStore = new ArrayList<>();
    public ArrayList<String> longitudeStore = new ArrayList<>();
    String busRouteno;
    String busStopno;


    private String GOOGLE_MAP_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=";
    private String SERVICE_URL = "http://api.translink.ca/rttiapi/v1/stops/";
    private String API_KEY = "?apikey=9UuAuljUvZpcSYfft7yM";
    private String ESTIMATES = "/estimates";
    private double lat;
    private double lon;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        busStopno = (String) intent.getExtras().get("BusStop");
        busRouteno = (String) intent.getExtras().get("RouteNo");
        final String routeUrl = busRouteno;
        final String reqUrl = SERVICE_URL + busStopno + API_KEY;
        final String reqUrlWait = SERVICE_URL + busStopno + ESTIMATES + API_KEY + "&routeNo=" + routeUrl;


        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_wait);

/**
 * Get wait time,lat and lon from translink API -  lat, lon, and name of public art array
 * Also updates UI array adapter in runnable thread based on wait times retrieved from translink api
 */
        new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(WaitActivity.this);
                pDialog.setMessage("Please wait...");
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                String[] countdownArray = null;

                HttpHandler sh = new HttpHandler();
                String jsonStr = sh.makeServiceCall(reqUrl);
                String jsonStrWait = sh.makeServiceCall(reqUrlWait);
                List<String> listContents = null;

                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONArray jsonArray = new JSONArray(jsonStrWait);

                    JSONArray jsonWaitArray = jsonArray.getJSONObject(0).getJSONArray("Schedules");

                    int length = jsonWaitArray.length();
                    listContents = new ArrayList<String>(length);

                    countdownArray = new String[length];

                    for (int i = 0; i < length; i++) {
                        JSONObject sched = jsonWaitArray.getJSONObject(i);
                        String countDown = sched.getString("ExpectedCountdown");
                        String destination = sched.getString("Destination");
                        listContents.add(destination + " - LEAVING IN : " + countDown + " MINS");
                        countdownArray[i] = countDown;
                    }


                    lon = (double) jsonObj.get("Longitude");
                    lat = (double) jsonObj.get("Latitude");

                    final List<String> finalListContents = listContents;
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            ListView myListView = findViewById(R.id.listViewWait);
                            myListView.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, finalListContents));

                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                String mapJson;
                GOOGLE_MAP_URL += Double.toString(lat) + "," + Double.toString(lon) + "&destinations=";
                String completeURL;
                for (int i = 0; i < artArray.size(); i++) {
                    completeURL = GOOGLE_MAP_URL + artArray.get(i).getY() + "," + artArray.get(i).getX() + "&mode=walking&key=AIzaSyBYjCpfeoLktveV-n6Jzy6DB2gYyMxofxM";
                    mapJson = sh.makeServiceCall(completeURL);

                    try {

                        JSONObject jsonObject = new JSONObject(mapJson);
                        JSONArray jsonArray = new JSONArray(jsonObject.getString("rows"));
                        String time = jsonArray.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getString("text");
                        timeStore.add(time);
                        latitudeStore.add(artArray.get(i).getY());
                        longitudeStore.add(artArray.get(i).getX());
                        nameStore.add(artArray.get(i).getName());


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                return null;
            }


            @Override
            protected void onPostExecute(Object o) {
                if (pDialog.isShowing())
                    pDialog.dismiss();

                if (latitudeStore.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No Round Trips Available before bus arrives", Toast.LENGTH_SHORT).show();
                }

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(WaitActivity.this);
            }
        }.execute();

    }

    /**
     * Add markers to map based on lat and lon retrieved in async task
     * In marker name add the name of the art and walking trip duration from google maps
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        LatLng loc = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(loc)
                .title("Stop Location: " + busStopno));


        if (latitudeStore.size() > 0) {
            for (int i = 0; i < latitudeStore.size(); i++) {
                LatLng loc2 = new LatLng(Double.parseDouble(latitudeStore.get(i)), Double.parseDouble(longitudeStore.get(i)));

                mMap.addMarker(new MarkerOptions().position(loc2)
                        .title(nameStore.get(i) + " - " + timeStore.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        }


        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
    }
}
