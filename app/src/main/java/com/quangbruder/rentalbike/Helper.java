package com.quangbruder.rentalbike;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Helper {

    public static void storeToken(Context context, String token){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Token", token);
        editor.apply();
    }

    public static String retrieveToken(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("Token", "");
    }

    public static String retrieveBikeID(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = preferences.getString("BikeId", "");
        if(!id.equalsIgnoreCase(""))
        {
            return id;
        }
        return null;
    }

    public static void storeBikeId(Context context, String id){
        System.out.println("BikeId:"+id+", is saved");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("BikeId",id);
        editor.apply();
    }



}
