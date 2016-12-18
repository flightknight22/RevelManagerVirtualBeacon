package com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Avery Knight on 12/14/2016.
 */

public class get_playlist_id extends AsyncTask<String, String, String> {
    OkHttpClient client = new OkHttpClient();
    String apiKey, deviceID;


    public get_playlist_id(String deviceID, String apiKey) {
        this.deviceID = deviceID;
        this.apiKey = apiKey;

    }

    @Override
    protected String doInBackground(String... strings) {
        try {

            Request request = new Request.Builder()
                    .url("http://api.reveldigital.com/schedules?api_key="+apiKey+"&device_id="+deviceID+"&format=json")
                    .build();


            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                return response.body().string();
            }

        }
        catch(Exception e)
        {
        }
        return null;
    }
    @Override
    protected void onPostExecute(String result)
    {
        if(result==null){
            result = "Nothing";
        }
        Log.d("result",result);
        try{
            JSONArray jsonArray = new JSONArray(result);
            if(jsonArray.length()>0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String playlist_id = jsonObject.getString("playlist_id");
                Beacon beacon = Globals.findBeaconByID(deviceID);
                beacon.setPlaylist_id(playlist_id);
            }
        } catch (Exception e){
            Log.d("DoInBackground","There Was A Problem");
        }
    }
}