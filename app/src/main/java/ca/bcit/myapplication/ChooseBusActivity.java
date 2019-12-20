package ca.bcit.myapplication;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChooseBusActivity extends AppCompatActivity {
    private String SERVICE_URL = "http://api.translink.ca/rttiapi/v1/stops/";
    private ProgressDialog pDialog;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> routeArrayList = new ArrayList<>();
    private HashMap<String, String> destinationNameMap = new HashMap<>();

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_choose_bus);
        final String busStopNo = (String) getIntent().getExtras().get("busStop");
        final String last_part_of_url = "/estimates?apikey=9UuAuljUvZpcSYfft7yM&count=3&timeframe=120";
        final String reqUrl = SERVICE_URL + busStopNo + last_part_of_url;


        TextView heading = findViewById(R.id.heading);
        heading.setText(heading.getText() + busStopNo);
        listView = findViewById(R.id.listBuses);

        //Get the bus routes from translink API based on input bus stop number
        new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Showing progress dialog
                pDialog = new ProgressDialog(ChooseBusActivity.this);
                pDialog.setMessage("Please wait...");
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                HttpHandler sh = new HttpHandler();
                String jsonStr = sh.makeServiceCall(reqUrl);

                // check first to see if schedule exists, if so, add to routearraylist
                if (jsonStr.charAt(2) != 'C') {
                    if (jsonStr != null) {
                        try {
                            JSONArray jsonList = new JSONArray(jsonStr);
                            for (int i = 0; i < jsonList.length(); i++) {
                                JSONObject c = jsonList.getJSONObject(i);
                                routeArrayList.add(c.getString("RouteNo"));
                                JSONArray schedulearray = new JSONArray(c.getString("Schedules"));
                                JSONObject destination = schedulearray.getJSONObject(0);
                                destinationNameMap.put(routeArrayList.get(i), destination.getString("Destination"));
                            }

                            return null;


                        } catch (final JSONException e) {
                            e.printStackTrace();

                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                listView.setAdapter(new MyListAdapter(ChooseBusActivity.this, R.layout.bus_information, routeArrayList));


            }
        }.execute();

    }
/// display active bus routes retrieved from translink API
    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;

        private MyListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewHolder = null;
            if (convertView == null) {

                LayoutInflater inflator = LayoutInflater.from(getContext());
                convertView = inflator.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.button = convertView.findViewById(R.id.routeNo);

                String destinationName = destinationNameMap.get(getItem(position));
                viewHolder.button.setText(getItem(position) + " " + destinationName);

                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String busStopNo = (String) getIntent().getExtras().get("busStop");
                        Toast.makeText(getContext(), busStopNo, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChooseBusActivity.this, WaitActivity.class);
                        final String routeNo = routeArrayList.get(position);
                        intent.putExtra("BusStop", busStopNo);
                        intent.putExtra("RouteNo", routeNo);


                        startActivity(intent);
                    }
                });
                convertView.setTag(viewHolder);

                viewHolder.button.setText(getItem(position) + " " + destinationName);
            } else {
                mainViewHolder = (ViewHolder) convertView.getTag();


            }

            return convertView;
        }
    }

    public class ViewHolder {
        public Button button;
    }


}


