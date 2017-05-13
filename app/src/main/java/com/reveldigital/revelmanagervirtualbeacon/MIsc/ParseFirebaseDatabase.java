package com.reveldigital.revelmanagervirtualbeacon.MIsc;

/**
 * Created by averyknight on 12/23/16.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import android.util.Base64;
import android.util.Log;


import com.google.firebase.database.DataSnapshot;
import com.reveldigital.revelmanagervirtualbeacon.Classes.ImageDBEntry;
import com.reveldigital.revelmanagervirtualbeacon.Classes.VuforiaImage;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;

import org.json.JSONObject;


public class ParseFirebaseDatabase extends AsyncTask<String, String, Void> {
    String key;
    public ParseFirebaseDatabase(String key){
        this.key = key;
    }

    @Override
    protected Void doInBackground(String... params) {
        if(Globals.snapshot!=null) {
            ImageDBEntry entry = new ImageDBEntry();

            for (DataSnapshot child : Globals.snapshot.child(Globals.vuforiaApiKey_Access_cleint).getChildren()) {
                if(!child.hasChild("aKey")) {
                    if (child.child("name").getValue(String.class).equals(key)) {
                        Log.d("Name", child.child("name").getValue(String.class) + " " + key);
                        try {
                            entry.setJsonObjectString(child.child("jsonObjectString").getValue(String.class));
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        entry.setName(key);
                        entry.setEncodedImage(child.child("encodedImage").getValue(String.class));
                        entry.setKey(child.getKey());
                    }
                } else {
                    Globals.hasKeys = true;
                }


            }
            VuforiaImage image = Globals.findVuforiaImageByName(key);
            if(image!=null && entry!=null) {
                if (entry.getEncodedImage() != null) {
                    byte[] decodedString = Base64.decode(entry.getEncodedImage(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    image.setImage(decodedByte);
                }
                if (entry.getJsonObjectString() != null) {
                    try {
                        image.setJsonObject(new JSONObject(entry.getJsonObjectString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(entry.getKey()!=null){
                    image.setKey(entry.getKey());
                }
            }
        }
        return null;
    }

}
