package com.reveldigital.revelmanagervirtualbeacon.Classes;

/**
 * Created by Avery Knight on 12/14/2016.
 */

public class vuforia_image {
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

    private String name, id;

    public vuforia_image(String id) {
        this.id = id;
    }
}
