package com.reveldigital.revelmanagervirtualbeacon.Classes;

/**
 * Created by averyknight on 1/18/17.
 */

public class VuforiaKey {
    String aKey, sKey;

    public VuforiaKey() {
    }

    public VuforiaKey(String aKey, String sKey) {
        this.aKey = aKey;
        this.sKey = sKey;
    }

    public String getaKey() {
        return aKey;
    }

    public void setaKey(String aKey) {
        this.aKey = aKey;
    }

    public String getsKey() {
        return sKey;
    }

    public void setsKey(String sKey) {
        this.sKey = sKey;
    }
}
