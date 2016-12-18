package com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;
import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.InputStream;

/**
 * Created by Avery Knight on 12/13/2016.
 */

public class create_qr_code extends AsyncTask<String, String, String> {
    OkHttpClient client = new OkHttpClient();
    Context appContext;
    IResponder responder;
    String regKey, apiKey, playlist_ID;

    public create_qr_code(IResponder r, Context e, String regKey, String apiKey, String playlist_ID) {
        responder = r;
        appContext=e;
        this.regKey = regKey;
        this.apiKey = apiKey;
        this.playlist_ID = playlist_ID;
        Log.d("thestuff",regKey+" : "+playlist_ID);
    }

    @Override
    protected String doInBackground(String... strings) {
            try {
                if (regKey!=null && playlist_ID!=null && !regKey.equals("null") && !playlist_ID.equals("null") ) {
                    Log.d("result1", "first");
                    Request request = new Request.Builder()
                            .url("http://tinyurl.com/api-create.php?url=https://shindig.reveldigital.com/gadgets/ifr?url=http%3A%2F%2Freveldigital.github.io%2Freveldigital-gadgets%2Fmobile-slideshow.xml&up_Opacity=1&up_ForeColor=FFFFFF&up_BackColor=&up_Name=Google+Gadget+1&up_Source=http%3A%2F%2Freveldigital.github.io%2Freveldigital-gadgets%2Fmobile-slideshow.xml&up_RDW=280&up_RDH=190&up_apikey=" + apiKey + "&up_playlistId=" + playlist_ID + "&synd=open&w=1181&h=966")
                            .build();


                    Response response = client.newCall(request).execute();

                    String url = response.body().string();
                    Log.d("url", url);
                    Log.d("result1", "first2");
                    Request request2 = new Request.Builder()
                            .url("http://qrickit.com/api/qr?d=" + url + "%23" + regKey)
                            .build();


                    Response response2 = client.newCall(request2).execute();
                    if (response2.isSuccessful()) {
                        InputStream inputStream = response2.body().byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            Beacon beacon = Globals.findBeaconByRegID(regKey);
                            beacon.setDisplayImage(bitmap);
                        }
                        return "success";
                    }

                }
            } catch (Exception e)
            {

            }

        return null;
    }
    @Override
    protected void onPostExecute(String result)
    {
        responder.getResults(result);
    }
}

