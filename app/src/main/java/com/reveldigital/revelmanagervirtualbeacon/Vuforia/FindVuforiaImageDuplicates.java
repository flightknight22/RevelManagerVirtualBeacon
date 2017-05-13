package com.reveldigital.revelmanagervirtualbeacon.Vuforia;

/**
 * Created by averyknight on 12/20/16.
 */

import android.os.AsyncTask;

import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;

import java.util.ArrayList;
import java.util.Date;




/**
 * Created by Avery Knight on 12/14/2016.
 */

public class FindVuforiaImageDuplicates extends AsyncTask<String, String, String> {

    private String accessKey = Globals.vuforiaApiKey_Access;
    private String secretKey = Globals.vuforiaApiKey_Secret;
    private String url = "https://vws.vuforia.com";
    private String id;
    IResponder r;




    public FindVuforiaImageDuplicates(String id, IResponder r) {
        this.id = id;
        this.r = r;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            HttpGet getRequest = new HttpGet();
            HttpClient client = new DefaultHttpClient();
            getRequest.setURI(new URI(url + "/duplicates/"+id));
            setHeaders(getRequest);

            HttpResponse response = client.execute(getRequest);
            //System.out.println(EntityUtils.toString(response.getEntity()));
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
        ArrayList<String> duplicateList = new ArrayList<>();
        try{
            JSONObject jsnObject = new JSONObject(result);
            JSONArray jsnArray=jsnObject.getJSONArray("similar_targets");
            for(int i=0; i<jsnArray.length(); i++){
                 duplicateList.add(jsnArray.getString(i));
            }
            if(duplicateList.isEmpty()){
                duplicateList.add(0, "-1");
            }
            r.getVuforiaResponse(duplicateList, id);
        } catch (Exception e){
            r.getVuforiaResponse(null, null);
        }
    }
}