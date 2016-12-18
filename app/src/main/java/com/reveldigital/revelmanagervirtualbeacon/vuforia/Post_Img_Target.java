package com.reveldigital.revelmanagervirtualbeacon.vuforia;

import android.os.AsyncTask;
import android.util.Log;

import com.reveldigital.revelmanagervirtualbeacon.Classes.vuforia_image;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;


import org.apache.commons.codec.android.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import java.util.Date;

/**
 * Created by Avery Knight on 12/15/2016.
 */

public class Post_Img_Target extends AsyncTask<String, String, String>{

    private String accessKey = Globals.vuforiaApiKey_Access;
    private String secretKey = Globals.vuforiaApiKey_Secret;
    private String url = "https://vws.vuforia.com";
    private String targetName;
    private String imageLocation;
    private TargetStatusPoller targetStatusPoller;

    private final float pollingIntervalMinutes = 60;//poll at 1-hour interval

    public Post_Img_Target(String imageLocation, String targetName) {
        this.imageLocation = imageLocation;
        this.targetName = targetName;
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            HttpPost postRequest = new HttpPost();
            HttpClient client = new DefaultHttpClient();
            postRequest.setURI(new URI(url + "/targets"));
            JSONObject requestBody = new JSONObject();

            setRequestBody(requestBody);
            postRequest.setEntity(new StringEntity(requestBody.toString()));
            setHeaders(postRequest); // Must be done after setting the body

            HttpResponse response = client.execute(postRequest);
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("target: " + responseBody);

            JSONObject jobj = new JSONObject(responseBody);
            Log.d("crazystuff", jobj.toString());
            String uniqueTargetId = jobj.has("target_id") ? jobj.getString("target_id") : "";
            System.out.println("\nCreated target with id: " + uniqueTargetId);

            return uniqueTargetId;
        } catch (Exception e){

        }
        return null;
    }



    private void setRequestBody(JSONObject requestBody) throws IOException, JSONException {
        File imageFile = new File(imageLocation);
        if (!imageFile.exists()) {
            System.out.println("File location does not exist!");
            System.exit(1);
        }
        byte[] image = FileUtils.readFileToByteArray(imageFile);
        requestBody.put("name", targetName); // Mandatory
        requestBody.put("width", 320.0); // Mandatory

        requestBody.put("image", new String(Base64.encodeBase64(image))); // Mandatory
        requestBody.put("active_flag", 1); // Optional

        requestBody.put("application_metadata", new String(Base64.encodeBase64("Vuforia test metadata".getBytes()))); // Optional
    }

    private void setHeaders(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader(new BasicHeader("Content-Type", "application/json"));
        request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }

    /**
     * Posts a new target to the Cloud database;
     * then starts a periodic polling until 'status' of created target is reported as 'success'.
     */


    @Override
    protected void onPostExecute(String result) {

        try {
            Log.d("namecrazy", result);
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject.getString("result_code").equals("Success")) {
                jsonObject = jsonObject.getJSONObject("target_record");
                vuforia_image image = Globals.findVuforiaImageById(jsonObject.getString("target_id"));
                image.setName(jsonObject.getString("name"));
            }
        } catch (Exception e) {

        }
    }
}
