package com.reveldigital.revelmanagervirtualbeacon.Interface;

import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Avery Knight on 12/13/2016.
 */

public interface IResponder {
    public void getResults(String results);
    public void deviceCreationStatus(String tf);
    public void getRegKeyResults(String results);
    public void getCompressedFile(File file);
    public void getVuforiaResponse(ArrayList<String> duplicates, String name);
    public void getVuforiaUploadResults(String results);
    public void getModDeviceStatus(Boolean bool);
}
