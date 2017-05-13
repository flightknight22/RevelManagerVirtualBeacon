package com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call;

import android.os.AsyncTask;
import android.util.Log;

import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Avery Knight on 12/15/2016.
 */

public class GetRevelRegistrationKey extends AsyncTask<String, String, String> {
    OkHttpClient client = new OkHttpClient();
    String apiKey, deviceID;
    IResponder responder;


    public GetRevelRegistrationKey(IResponder r) {responder = r;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            Log.d("regkeyresult","here");
            Request request = new Request.Builder()
                    .url("https://svc1.reveldigital.com/device/activation/get?deviceTypeId=ARBeacon&format=json")
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

        try{
            Log.d("regkeyresult",result);
            JSONObject jsonObject = new JSONObject(result);
            String activationCode = jsonObject.getString("ActivationCode");
            responder.getRegKeyResults(activationCode);
        }catch (Exception e){
            Log.d("regkeyresult","No result1");
            responder.getRegKeyResults(null);
        }

    }
}
