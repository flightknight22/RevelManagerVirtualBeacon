package com.reveldigital.revelmanagervirtualbeacon.Vuforia;

import android.os.AsyncTask;
import android.util.Log;

import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * Created by averyknight on 12/20/16.
 */

public class DeleteVuforiaTarget extends AsyncTask<String, String, String> implements TargetStatusListener{

    private String accessKey = Globals.vuforiaApiKey_Access;
    private String secretKey = Globals.vuforiaApiKey_Secret;
    private String url = "https://vws.vuforia.com";
    private String targetId;

    private final float pollingIntervalMinutes = 60;//poll at 1-hour interval
    private TargetStatusPoller targetStatusPoller;


    public DeleteVuforiaTarget(String id) {
        this.targetId = id;
    }

    @Override
    protected String doInBackground(String... strings) {
        //HttpDelete deleteRequest = new HttpDelete();
        //HttpClient client = new DefaultHttpClient();
        try {
            updateTargetActivation(false);
            //deleteRequest.setURI(new URI(url + "/targets/" + targetId));
            //setHeaders(deleteRequest);

            //HttpResponse response = client.execute(deleteRequest);
            //Log.d("Delete Response ", EntityUtils.toString(response.getEntity()));
            //return EntityUtils.toString(response.getEntity());
        } catch (Exception e){
            e.printStackTrace();

        }
        return  null;
    }



    private void setHeaders(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }

    // sets the targets active_flag to the Boolean value of the argument
    private void updateTargetActivation(Boolean b) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
        HttpPut putRequest = new HttpPut();
        HttpClient client = new DefaultHttpClient();
        putRequest.setURI(new URI(url + "/targets/" + targetId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("active_flag", b );// add a JSON field for the active_flag

        putRequest.setEntity(new StringEntity(requestBody.toString()));
        // Set the Headers for this Put request
        // Must be done after setting the body
        SignatureBuilder sb = new SignatureBuilder();
        putRequest.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        putRequest.setHeader(new BasicHeader("Content-Type", "application/json"));
        putRequest.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(putRequest, secretKey));

        HttpResponse response = client.execute(putRequest);
        Log.d("Update Response ", EntityUtils.toString(response.getEntity()));
    }

    public void deactivateThenDeleteTarget() {
        // Update the target's active_flag to false and then Delete the target when the state change has been processed;
        try {
            updateTargetActivation( false );
        } catch ( URISyntaxException | IOException | JSONException e) {
            e.printStackTrace();
            return;
        }

        // Poll the target status until the active_flag is confirmed to be set to false
        // The TargetState will be passed to the OnTargetStatusUpdate callback
        targetStatusPoller = new TargetStatusPoller(pollingIntervalMinutes, targetId, accessKey, secretKey, this );
        targetStatusPoller.startPolling();
    }

    // Called with each update of the target status received by the TargetStatusPoller
    @Override
    public void OnTargetStatusUpdate(TargetState target_state) {
        if (target_state.hasState) {

            if (target_state.getActiveFlag() == false) {

                targetStatusPoller.stopPolling();


                    Log.d("Delete",".. deleting target ..");

                    //deleteTarget();


            }
        }
    }


    @Override
    protected void onPostExecute(String result)
    {
        Log.d("Delete", "Finished");
    }
}
