package com.quangbruder.rentalbike;

import static android.content.ContentValues.TAG;

import static com.quangbruder.rentalbike.Helper.retrieveBikeID;
import static com.quangbruder.rentalbike.Helper.retrieveToken;
import static com.quangbruder.rentalbike.Helper.storeBikeId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextView tvBikeId;
    EditText editTextPin;
    Button btnCheck;
    String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPin = findViewById(R.id.editTextPin);
        btnCheck = findViewById(R.id.btnCheck);
        tvBikeId = findViewById(R.id.tvBikeId);

        //storeBikeId(this,"");
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationPermission();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(retrieveBikeID(this)==null || retrieveBikeID(this).isEmpty()){
            gotoLoginActivity();
        } else {
            System.out.println("BIKEID = "+retrieveBikeID(this)+"\n Dont need to go to LoginActivity");
        }

        sendCurrentLocation(URLs.URL_LOCATE_BIKE,10000);
        setUI();


    }

    public void pinCheck(Context context,String bikeId, String pin){
        System.out.println("PIN check func: "+pin);
        String url = URLs.URL_BIKE_CHECK_PIN+"?bikeId="+bikeId+"&payloadPin="+pin;

        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("Response is: "+ response);
                        System.out.println("Booking Id from Server: "+response);
                        bookingId = response;
                        timer.cancel();
                        gotoRentActivity();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
                Toast.makeText(context, "Pin is wrong", Toast.LENGTH_LONG).show();
            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",retrieveToken(context));
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    public void setUI(){
        tvBikeId.setText("BIKE: "+retrieveBikeID(getApplicationContext()));
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pin = editTextPin.getText().toString();
                System.out.println("PIN: "+pin);
                pinCheck(getApplicationContext(),retrieveBikeID(getApplicationContext()),pin);
            }
        });
    }

    public void gotoLoginActivity(){
        finish();
        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void gotoRentActivity(){
        finish();
        Intent myIntent = new Intent(MainActivity.this, RentActivity.class);
        myIntent.putExtra("bikeId", retrieveBikeID(getApplicationContext()));
        myIntent.putExtra("bookingId", bookingId);
        MainActivity.this.startActivity(myIntent);
    }

    Timer timer;
    public void sendCurrentLocation(String url, int x){
        timer = new Timer();
        timer.schedule(new SendLocationTimer(url,getApplicationContext(),retrieveBikeID(getApplicationContext()),null,fusedLocationProviderClient,cancellationTokenSource,this),0,x);

    }


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();


    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            System.out.println("get locationPermissionGranted: "+locationPermissionGranted);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }


}