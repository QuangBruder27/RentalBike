package com.quangbruder.rentalbike;

import static com.quangbruder.rentalbike.Helper.storeBikeId;
import static com.quangbruder.rentalbike.Helper.storeToken;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText editTextBikeId, editTextPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextBikeId = findViewById(R.id.etBikeId);
        editTextPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bikeLogin(getApplicationContext(), editTextBikeId.getText().toString(), editTextPassword.getText().toString());
            }
        });
    }

    public void bikeLogin(Context context, String bikeId,String password){
        Map<String,String> map = new HashMap<>();
        map.put("bikeId",bikeId);
        System.out.println("BIke Id in Map in Func bikeLogin: "+bikeId);
        map.put("password",password);

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_BIKE_LOGIN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("Response is: "+ response);
                            storeToken(context, response);
                            storeBikeId(getApplicationContext(),bikeId);
                            gotoMainActivity();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error.toString());
                    Toast.makeText(context, "Login failed", Toast.LENGTH_LONG).show();
                }
            }){
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return map;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
    }



    public void gotoMainActivity(){
        finish();
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }




}