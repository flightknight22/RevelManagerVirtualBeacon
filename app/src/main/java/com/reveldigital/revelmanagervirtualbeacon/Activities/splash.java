package com.reveldigital.revelmanagervirtualbeacon.Activities;

/**
 * Created by Avery Knight on 12/13/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;
import com.reveldigital.revelmanagervirtualbeacon.R;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.getDevices;

public class splash extends AppCompatActivity implements IResponder {
    EditText editText;
    ViewSwitcher viewSwitcher;
    String apiKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        editText = (EditText) findViewById(R.id.login);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);

        Thread myThread = new Thread(){
            @Override
            public void run() {
                try {
                    SharedPreferences prefs = getSharedPreferences(Globals.sharedPreferenceName, 0);
                    apiKey = prefs.getString(Globals.apiKey,null);
                    if(apiKey!=null){
                        editText.setText(apiKey);
                        if(isNetworkAvailable()){
                            new getDevices(splash.this, getApplicationContext(), apiKey).execute();
                        } else {
                            Toast.makeText(splash.this, "No Active Internet Connection! Please Check Connectivity And Try Again.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        viewSwitcher.showNext();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void getResults(String results) {
        if(results!=null) {
            goToMain();
        }else {
            Toast.makeText(this, "Something Went Wrong. Please Check API KEY And Try Again.", Toast.LENGTH_SHORT).show();
            viewSwitcher.showNext();
        }
    }

    public void goToMain(){
        editText.setText(apiKey);
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
    public void checkApi(View v){
        viewSwitcher.showNext();
        SharedPreferences.Editor editor = getSharedPreferences(Globals.sharedPreferenceName, MODE_PRIVATE).edit();
        String collectedApiKey = editText.getText().toString();
        if(collectedApiKey!=null && collectedApiKey!=""){
            collectedApiKey = collectedApiKey.replaceAll("\\s+","");
            editor.putString(Globals.apiKey, collectedApiKey);
            editor.commit();
            new getDevices(splash.this, getApplicationContext(), collectedApiKey).execute();
        }
    }

}