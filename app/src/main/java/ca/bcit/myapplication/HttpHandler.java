package ca.bcit.myapplication;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpHandler {
    private static final String TAG = HttpHandler.class.getSimpleName();
    public HttpHandler() {   }

    public String makeServiceCall(String reqUrl) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(reqUrl).addHeader("accept", "application/JSON")
                .build();

        Response response;

        try{
            response = client.newCall(request).execute();
            return response.body().string();


        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }

        return null;
    }


}
