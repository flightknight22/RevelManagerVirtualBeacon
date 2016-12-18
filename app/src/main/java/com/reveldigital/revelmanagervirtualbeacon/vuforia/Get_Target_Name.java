package com.reveldigital.revelmanagervirtualbeacon.vuforia;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.reveldigital.revelmanagervirtualbeacon.Classes.vuforia_image;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;

import java.net.URI;
import java.util.Date;

import org.apache.http.impl.cookie.DateUtils;
import org.json.JSONObject;


/**
 * Created by Avery Knight on 12/14/2016.
 */

public class Get_Target_Name extends AsyncTask<String, String, String> {

    private String accessKey = Globals.vuforiaApiKey_Access;
    private String secretKey = Globals.vuforiaApiKey_Secret;
    private String url = "https://vws.vuforia.com";
    private String id;




    public Get_Target_Name(String id) {
        this.id = id;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            HttpGet getRequest = new HttpGet();
            HttpClient client = new DefaultHttpClient();
            getRequest.setURI(new URI(url + "/targets/"+id));
            setHeaders(getRequest);

            HttpResponse response = client.execute(getRequest);
            //System.out.println(EntityUtils.toString(response.getEntity()));
            Log.d("crazyname","stuff");
            return EntityUtils.toString(response.getEntity());

        } catch (Exception e){

        }
        return null;
    }

    private void setHeaders(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }


    @Override
    protected void onPostExecute(String result)
    {

        try{
            Log.d("namecrazy",result);
            JSONObject jsonObject = new JSONObject(result);
            if(jsonObject.getString("result_code").equals("Success")){
                jsonObject = jsonObject.getJSONObject("target_record");
                vuforia_image image = Globals.findVuforiaImageById(jsonObject.getString("target_id"));
                image.setName(jsonObject.getString("name"));
            }
        } catch (Exception e){

        }
    }
}

