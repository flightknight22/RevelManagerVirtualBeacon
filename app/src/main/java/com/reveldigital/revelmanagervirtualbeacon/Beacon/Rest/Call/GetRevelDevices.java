package com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponderString;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Avery Knight on 12/13/2016.
 */

public class GetRevelDevices extends AsyncTask<String, String, String> {
    OkHttpClient client = new OkHttpClient();
    Context appContext;
    IResponderString responder;
    String apiKey;


    public GetRevelDevices(IResponderString r, Context e, String apiKey) {
        responder = r;
        appContext=e;
        this.apiKey = apiKey;
        Globals.doneloading = false;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            Log.d("Do In Back","Made It to: "+getClass());
            Request accountInfoRequest = new Request.Builder()
                    .url("http://api.reveldigital.com/account?api_key="+apiKey)
                    .build();

            Response accountInfoResponse = client.newCall(accountInfoRequest).execute();
            if(accountInfoResponse.isSuccessful()) {
                try {
                    JSONObject jsnObj = new JSONObject(accountInfoResponse.body().string());
                    if(jsnObj.getString("tags")!=null) {
                        JSONObject accountInfo = new JSONObject(jsnObj.getString("tags"));
                        int distance = accountInfo.getInt("Distance");
                        accountInfo = accountInfo.getJSONObject("vuforia info");
                        Iterator<?> keys = accountInfo.keys();
                        while(keys.hasNext() ) {
                            String key = (String)keys.next();
                            if(accountInfo.getString(key).length()<10){
                                return "keyIssue";
                            }

                        }
                        String caKey, csKey, ssKey, saKey;
                        caKey = accountInfo.getString("caKey");
                        csKey = accountInfo.getString("csKey");
                        saKey = accountInfo.getString("saKey");
                        ssKey = accountInfo.getString("ssKey");

                        Globals.vuforiaApiKey_Access =saKey;
                        Globals.vuforiaApiKey_Secret=ssKey;

                        Globals.vuforiaApiKey_Access_cleint=caKey;
                        Globals.vuforiaApiKey_Secret_client=csKey;

                    } else {
                        return "noTags";
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }


            } else {
                return null;
            }

            Request request = new Request.Builder()
                    .url("http://api.reveldigital.com/devices?api_key="+apiKey+"&device_type_id=ARBeacon")
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
        ArrayList<Beacon> beaconList = new ArrayList<>();
        try {
            JSONArray deviceJsnArray = new JSONArray(result);
            for (int i = 0; i < deviceJsnArray.length(); i++) {
                JSONObject device = deviceJsnArray.getJSONObject(i);

                String mac = device.getString("mac_address");
                if(mac.equals("AR_BEACON") || mac.equals("QR_BEACON")){
                    String id = device.getString("id");
                    String name = device.getString("name");
                    String regID = device.getString("registration_key");
                    JSONObject locationInfo = device.getJSONObject("location");
                    String city = locationInfo.getString("city");
                    String state = locationInfo.getString("state");
                    String country = locationInfo.getString("country");
                    String postal_code = locationInfo.getString("postal_code");
                    String address = locationInfo.getString("address");
                    String type = mac;
                    beaconList.add(new Beacon(name, type, id, regID, city, state, country, postal_code, address));
                }

            }
            Globals.beaconFullList = beaconList;
            Globals.restCallsLeft = beaconList.size();
            for(Beacon beacon:beaconList){
                    new GetRevelDeviceInformation(appContext, beacon.getRegId(), beacon.getDeviceId(), apiKey).execute();
            }
        } catch (Exception ignored) {
        }
        responder.getStringResults(result);
    }
}