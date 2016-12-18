package com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

/**
 * Created by Avery Knight on 12/13/2016.
 */

public class get_device_info extends AsyncTask<String, String, String> {
    OkHttpClient client = new OkHttpClient();
    Context appContext;
    String regKey, deviceID, apiKey;


    public get_device_info(Context e, String regKey, String deviceID, String apiKey) {
        appContext=e;
        this.regKey = regKey;
        this.deviceID = deviceID;
        this.apiKey = apiKey;

    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d("DoInBackground","Made It");
        Log.d("DoInBackground",regKey);
        try {

            Request request = new Request.Builder()
                    .url("http://svc1.reveldigital.com/v2/device/schedule/get/"+regKey+"?format=json")
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
            JSONObject jsonObject = new JSONObject(result);
            jsonObject = jsonObject.getJSONObject("reveldigital");
            if(jsonObject.has("schedule")){
                if(jsonObject.getJSONArray("schedule").getJSONObject(0).has("playlist")){
                    Log.d("DoInBackground","Device Has Schedule and Playlist");
                    JSONObject playlist_info=jsonObject.getJSONArray("schedule").getJSONObject(0).getJSONObject("playlist");
                    String playlist_id = playlist_info.getString("id");
                    String playlist_name = playlist_info.getString("name");
                    if(!playlist_id.equals("10855")){
                        Beacon beacon = Globals.findBeaconByID(deviceID);
                        beacon.setPlaylist_name(playlist_name);
                        new get_playlist_id(deviceID,apiKey).execute();
                    }
                }
            }
        } catch (Exception e){
            Log.d("DoInBackground","There Was A Problem");
        }
    }
}

