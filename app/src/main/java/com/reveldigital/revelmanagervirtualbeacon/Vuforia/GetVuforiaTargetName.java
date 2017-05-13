package com.reveldigital.revelmanagervirtualbeacon.Vuforia;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.reveldigital.revelmanagervirtualbeacon.Classes.VuforiaImage;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.MIsc.ParseFirebaseDatabase;

import java.net.URI;
import java.util.Date;

import org.apache.http.impl.cookie.DateUtils;
import org.json.JSONObject;


/**
 * Created by Avery Knight on 12/14/2016.
 */

public class GetVuforiaTargetName extends AsyncTask<String, String, String> {

    private String accessKey = Globals.vuforiaApiKey_Access;
    private String secretKey = Globals.vuforiaApiKey_Secret;
    private String url = "https://vws.vuforia.com";
    private String id;




    public GetVuforiaTargetName(String id) {
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
            JSONObject jsonObject = new JSONObject(result);
            if(jsonObject.getString("result_code").equals("Success")){
                jsonObject = jsonObject.getJSONObject("target_record");
                VuforiaImage image = Globals.findVuforiaImageById(jsonObject.getString("target_id"));
                image.setName(jsonObject.getString("name"));
                new ParseFirebaseDatabase(image.getName()).execute();

            }
        } catch (Exception e){

        }
    }
}

