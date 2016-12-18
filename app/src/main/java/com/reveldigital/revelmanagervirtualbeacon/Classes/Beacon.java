package com.reveldigital.revelmanagervirtualbeacon.Classes;

import android.graphics.Bitmap;

/**
 * Created by Avery Knight on 11/28/2016.
 */

public class Beacon{
    private String name, type, deviceId, regId, city, state, country, postal_code, address, playlist_name, playlist_id;
    private Bitmap displayImage;

    public Bitmap getDisplayImage() {
        return displayImage;
    }

    public void setDisplayImage(Bitmap displayImage) {
        this.displayImage = displayImage;
    }

    public String getPlaylist_name() {
        return playlist_name;
    }

    public void setPlaylist_name(String playlist_name) {
        this.playlist_name = playlist_name;
    }

    public String getPlaylist_id() {
        return playlist_id;
    }

    public void setPlaylist_id(String playlist_id) {
        this.playlist_id = playlist_id;
    }

    public Beacon(String name, String type, String deviceId, String regId, String city, String state, String country, String postal_code, String address) {
        this.name = name;
        if(name.equals(null)){
            name="";
        }
        this.type = type;
        this.deviceId = deviceId;
        this.regId = regId;
        this.city = city;
        if(city.equals("null")){
            this.city="";
        }
        this.state = state;
        if(state.equals("null")){
            this.state = "";
        }
        this.country = country;
        if(country .equals("null")){
            this.country = "";
        }
        this.postal_code = postal_code;
        if(postal_code.equals("null")){
            this.postal_code = "";
        }
        this.address = address;
        if(address.equals("null")){
            this.address = "";
        }
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegId() {
        return regId;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public String getAddress() {
        return address;
    }

    public String getDeviceId() {
        return deviceId;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public Beacon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public boolean isQRBeacon(){
        if(getType().equals("QR_BEACON"))
        {
            return true;
        }
        return false;
    }
    public boolean isARBeacon(){
        if(getType().equals("AR_BEACON"))
        {
            return true;
        }
        return false;
    }
}
