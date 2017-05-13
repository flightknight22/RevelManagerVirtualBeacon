package com.reveldigital.revelmanagervirtualbeacon.Classes;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by averyknight on 12/20/16.
 */

public class ImageDBEntry {

    private String jsonObjectString, key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;

    }

    public void setName(String name) {
        this.name = name;
    }

    private String encodedImage;
    private String name;

    public String getEncodedImage() {
        return encodedImage;
    }

    public void setEncodedImage(String encodedImage) {
        this.encodedImage = encodedImage;
    }

    public String getJsonObjectString() {
        return jsonObjectString;
    }

    public void setJsonObjectString(String jsonObjectString) {
        this.jsonObjectString = jsonObjectString;
    }

    public ImageDBEntry() {
    }


}

