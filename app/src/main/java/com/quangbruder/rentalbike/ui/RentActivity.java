package com.quangbruder.rentalbike.ui;


import static com.quangbruder.rentalbike.Helper.distance;
import static com.quangbruder.rentalbike.Helper.isRunning;
import static com.quangbruder.rentalbike.Helper.retrieveBikeID;
import static com.quangbruder.rentalbike.Helper.retrieveToken;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.quangbruder.rentalbike.Helper;
import com.quangbruder.rentalbike.R;
import com.quangbruder.rentalbike.SendLocationTimer;
import com.quangbruder.rentalbike.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RentActivity extends AppCompatActivity {

    TextView tvDistance;
    Chronometer chronometer;
    Button btnStop;
    String bikeId;
    String bookingId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);

        Intent intent = getIntent();
        bikeId = intent.getStringExtra("bikeId");
        bookingId = intent.getStringExtra("bookingId");

        tvDistance = findViewById(R.id.tvDistance);
        tvDistance.setText(distance+" km");

        chronometer = findViewById(R.id.chronometer);
        chronometer.start();

        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endJourney();
            }
        });

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


    }

    /**
     * Send PUT-Request to end the journey
     */
    public void endJourney(){
        chronometer.stop();
        Toast.makeText(getApplicationContext(), "Timed: "+chronometer.getText(), Toast.LENGTH_SHORT).show();
        if(null != timer) timer.cancel();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("bikeId",retrieveBikeID(getApplicationContext()));
            jsonObject.put("id",Integer.valueOf(bookingId));
            jsonObject.put("endTime", Helper.simpleDateFormat.format(new Timestamp(System.currentTimeMillis())));
            jsonObject.put("distance",(Integer)Math.round(distance));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("JSON Object: "+jsonObject.toString());

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URLs.URL_ROUTE_END,jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Response is: "+ response);
                        Toast.makeText(getApplicationContext(), "Thanks for the journey", Toast.LENGTH_SHORT).show();
                        isRunning = false;
                        gotoMainActivity();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("End erro: "+error.toString());
                Toast.makeText(getApplicationContext(), "End failed. Please try again", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization",retrieveToken(getApplicationContext()));
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendAndUpdateLocation(URLs.URL_LOCATE_BIKE,10000);
    }

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    Timer timer;

    /**
     * Create time schedule for send and update location
     * @param url
     * @param x
     */
    public void sendAndUpdateLocation(String url, int x){
        timer = new Timer();
        timer.schedule(new SendLocationTimer(url,getApplicationContext(),bikeId,tvDistance,fusedLocationProviderClient,cancellationTokenSource,this),0,x);
    }

    public void gotoMainActivity(){
        finish();
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }




}
