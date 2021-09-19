package com.quangbruder.rentalbike;

import static com.quangbruder.rentalbike.LocationHelper.postLocation;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TimerTask;

public class SendLocationTimer extends TimerTask {
    String url;
    Context context;
    String bikeId;
    TextView tvDistance;
    FusedLocationProviderClient fusedLocationProviderClient;
    CancellationTokenSource cancellationTokenSource;
    Activity activity;

    public SendLocationTimer(String url, Context context, String bikeId, TextView tvDistance, FusedLocationProviderClient fusedLocationProviderClient, CancellationTokenSource cancellationTokenSource, Activity activity) {
        this.url = url;
        this.context = context;
        this.bikeId = bikeId;
        this.tvDistance = tvDistance;
        this.fusedLocationProviderClient = fusedLocationProviderClient;
        this.cancellationTokenSource = cancellationTokenSource;
        this.activity = activity;
    }

    @Override
    public void run() {
        LocationHelper.getDeviceLocation(fusedLocationProviderClient,cancellationTokenSource,tvDistance,activity);
        if (LocationHelper.currentLocation != null) {
            postLocation(context,url);
        } else {
            System.out.println("Location is null....");
        }
    }


}