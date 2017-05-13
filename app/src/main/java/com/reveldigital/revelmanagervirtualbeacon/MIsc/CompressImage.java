package com.reveldigital.revelmanagervirtualbeacon.MIsc;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;

import java.io.File;

import id.zelory.compressor.Compressor;

/**
 * Created by averyknight on 12/23/16.
 */

public class CompressImage extends AsyncTask<File, File, File>{
    File actualImage;
    Context context;
    IResponder r;
    public CompressImage(File actualImage, Context context, IResponder r){
        this.actualImage = actualImage;
        this.context = context;
        this.r = r;
    }

    @Override
    protected File doInBackground(File... params) {
        File compressedImage = new Compressor.Builder(context)
                .setMaxWidth(1920)
                .setMaxHeight(1080)
                .setQuality(80)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .build()
                .compressToFile(actualImage);

        if (compressedImage != null) {
            return compressedImage;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        r.getCompressedFile(file);
    }
}
