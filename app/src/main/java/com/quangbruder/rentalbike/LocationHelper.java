package com.quangbruder.rentalbike;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.quangbruder.rentalbike.Helper.retrieveBikeID;
import static com.quangbruder.rentalbike.Helper.retrieveToken;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class LocationHelper {

    public static Location currentLocation;
    public static Location lastLocation;
    static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static float distance = 0.0F;

    public static void updateDistance(Location origin, Location destination, TextView tvDistance){
        if (origin!= null) {
            distance += origin.distanceTo(destination)/1000.0;
        }
        System.out.println("new distance: "+distance);
        tvDistance.setText( (float)Math.round(distance * 100) / 100+" km");
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    public static void getDeviceLocation(FusedLocationProviderClient fusedLocationProviderClient,  CancellationTokenSource cancellationTokenSource, TextView tvDistance, Activity activity) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY,cancellationTokenSource.getToken());
            locationResult.addOnCompleteListener(activity, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current lo cation of the device.
                        if ( task.getResult() != null) {
                            lastLocation = LocationHelper.currentLocation;
                            LocationHelper.currentLocation =task.getResult();
                            if(tvDistance != null) updateDistance(lastLocation,LocationHelper.currentLocation,tvDistance);
                            Log.d(TAG, "Location: longtitude:"+LocationHelper.currentLocation.getLongitude()+"; latitude"+LocationHelper.currentLocation.getLatitude());
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });

        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    // [END maps_current_place_get_device_location]

    public static void postLocation(Context context,String url){
        System.out.println("PostLocation Func");
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("bikeId",retrieveBikeID(context));
            jsonObject.put("latitude",String.valueOf(LocationHelper.currentLocation.getLatitude()));
            jsonObject.put("longtitude",String.valueOf(LocationHelper.currentLocation.getLongitude()));
            jsonObject.put("timeCreated",LocationHelper.simpleDateFormat.format(new Timestamp(System.currentTimeMillis())));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("RESPONSE: "+response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error: " + error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",retrieveToken(context));
                return headers;
            }
        };
        requestQueue.add(request);
    }

    public static Map<String,String> createBonusParameters(String customerId, Double distance){
        Map<String, String> result = new HashMap<String, String>();
        result.put("customerId",customerId);
        result.put("payloadDistance",distance+"");
        return result;
    }


    /*
    public static void putBonus(String url,Map<String, String> params, Context context){
        System.out.println("Send PUT request to Server");
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        CustomRequest request = new CustomRequest(Request.Method.PUT, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("RESPONSE: "+response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error: " + error);
            }
        });
        requestQueue.add(request);
    }

     */


}
