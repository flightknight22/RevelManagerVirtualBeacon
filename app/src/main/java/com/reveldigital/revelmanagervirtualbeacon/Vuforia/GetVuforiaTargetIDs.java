package com.reveldigital.revelmanagervirtualbeacon.Vuforia;


import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.reveldigital.revelmanagervirtualbeacon.Classes.VuforiaImage;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.impl.cookie.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by Avery Knight on 12/14/2016.
 */

public class GetVuforiaTargetIDs extends AsyncTask<String, String, String> {

    private String accessKey = Globals.vuforiaApiKey_Access;
    private String secretKey = Globals.vuforiaApiKey_Secret;
    private String url = "https://vws.vuforia.com";

    IResponder responder;



    public GetVuforiaTargetIDs() {

    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            HttpGet getRequest = new HttpGet();
            HttpClient client = new DefaultHttpClient();
            getRequest.setURI(new URI(url + "/targets"));
            setHeaders(getRequest);

            HttpResponse response = client.execute(getRequest);

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
        ArrayList<VuforiaImage> imageList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray imageJsnArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < imageJsnArray.length(); i++){
                String id= imageJsnArray.getString(i);
                VuforiaImage image = new VuforiaImage(id);
                new GetVuforiaTargetName(id).execute();
                imageList.add(image);
            }
        }
        catch (Exception e) {
        }
        if(!imageList.isEmpty()) {
            Globals.vuforiaImagesFullList = imageList;
        }
    }
}

