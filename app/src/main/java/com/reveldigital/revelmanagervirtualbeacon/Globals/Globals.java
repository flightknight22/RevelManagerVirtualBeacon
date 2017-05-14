package com.reveldigital.revelmanagervirtualbeacon.Globals;

import com.google.firebase.database.DataSnapshot;
import com.reveldigital.revelmanagervirtualbeacon.Classes.*;

import java.util.ArrayList;

/**
 * Created by Avery Knight on 12/13/2016.
 */

public class Globals {
    public static String deviceID="deviceID";
    public static String isMade="isMade";

    public static Boolean hasKeys = false;


    public static String vuforiaApiKey_Access;//"7b4e9eeae5246e63aca1ca1c4ee04e78ec4dddac";
    public static String vuforiaApiKey_Secret;//"bbc18df2651954c8e3c2ddad5208da46babc10d6";

    public static String vuforiaApiKey_Access_cleint;//"d903db9e786a7c75dab1fb009679a2d3c8a8da0f";
    public static String vuforiaApiKey_Secret_client;//"dbb4065df1359a18f99132833c2667d588843e8c";

    public static String sharedPreferenceName = "prefs";
    public static String apiKey = "apiKey";
    public static String apiKeyList = "apiList";

    public static ArrayList<Beacon> beaconFullList;
    public static ArrayList<VuforiaImage> vuforiaImagesFullList;

    public static DataSnapshot snapshot;

    public static Boolean doneloading = false;

    public static int restCallsLeft = 0;

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
    public static Beacon pop(String ID){
        //finds beacon by device id
        //returns a beacon that matches the ID
        //returns null if beacon not found
        for(Beacon beacon:beaconFullList){
            if(beacon.getDeviceId().equals(ID)){
                beaconFullList.remove(beacon);
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
    public static VuforiaImage findVuforiaImageById(String id){
        for(VuforiaImage image:vuforiaImagesFullList){
            if(image.getId().equals(id)){
                return image;
            }
        }
        return null;
    }
    public static VuforiaImage findVuforiaImageByName(String id){
        for(VuforiaImage image:vuforiaImagesFullList){
            if(image.getName().equals(id)){
                return image;
            }
        }
        return null;
    }
    public static ArrayList<String> getVuforiaKeys() {
        if (vuforiaImagesFullList!=null) {
            ArrayList<String> list = new ArrayList<>();
            for (VuforiaImage image : vuforiaImagesFullList) {
                list.add(image.getId());
            }
            return list;
        }else{
            return null;
        }
    }

}
