package com.reveldigital.revelmanagervirtualbeacon.Globals;

import com.reveldigital.revelmanagervirtualbeacon.Classes.*;

import java.util.ArrayList;

/**
 * Created by Avery Knight on 12/13/2016.
 */

public class Globals {
    public static String deviceID="deviceID";
    public static String isMade="isMade";


    public static String vuforiaApiKey_Access="1177e3135728ded67ab7bddfbeb4263d71f53143";
    public static String vuforiaApiKey_Secret="117b2e1a5f9c5b121044ee46270f474317f9ae69";

    public static String sharedPreferenceName = "prefs";
    public static String apiKey = "apiKey";

    public static ArrayList<Beacon> beaconFullList;
    public static ArrayList<vuforia_image> vuforiaImagesFullList;

    public static Beacon findBeaconByID(String ID){
        //finds beacon by device id
        //returns a beacon that matches the ID
        //returns null if beacon not found
        for(Beacon beacon:beaconFullList){
            if(beacon.getDeviceId().equals(ID)){
                return beacon;
            }
        }
        return null;
    }
    public static Beacon findBeaconByRegID(String ID){
        //finds beacon by registration id
        //returns a beacon that matches the ID
        //returns null if beacon not found
        for(Beacon beacon:beaconFullList){
            if(beacon.getRegId().equals(ID)){
                return beacon;
            }
        }
        return null;
    }
    public static vuforia_image findVuforiaImageById(String id){
        for(vuforia_image image:vuforiaImagesFullList){
            if(image.getId().equals(id)){
                return image;
            }
        }
        return null;
    }
}
