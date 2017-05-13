package com.reveldigital.revelmanagervirtualbeacon.Classes;

import android.graphics.Bitmap;

import org.json.JSONObject;

/**
 * Created by Avery Knight on 12/14/2016.
 */

public class VuforiaImage {
    private String key;
    private Bitmap image;
    JSONObject jsonObject;
    private String name;
    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public VuforiaImage(String id) {
        this.id = id;
    }
}
