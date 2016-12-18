package com.reveldigital.revelmanagervirtualbeacon.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import com.reveldigital.api.Device;
import com.reveldigital.api.Location;
import com.reveldigital.api.RequestException;
import com.reveldigital.api.service.DeviceService;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.IResponderRegKey;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.deviceCreated;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.get_reg_key;
import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.reveldigital.revelmanagervirtualbeacon.Classes.vuforia_image;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;
import com.reveldigital.revelmanagervirtualbeacon.R;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.create_qr_code;
import com.reveldigital.revelmanagervirtualbeacon.vuforia.Get_Target_Name;
import com.reveldigital.revelmanagervirtualbeacon.vuforia.Post_Img_Target;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import id.zelory.compressor.Compressor;
import id.zelory.compressor.FileUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class create_edit_beacons extends AppCompatActivity implements IResponder, IResponderRegKey, deviceCreated {
    Button button;
    ImageView imageView;
    EditText name, city, state, country, postal_code, address;
    Spinner spinner;
    ViewSwitcher viewSwitcher;
    Beacon beacon;
    String apiKey;
    AlertDialog.Builder builder;
    ViewSwitcher buttonProgress;
    String noSchedule = "No Schedule Found For This Beacon. Please Schedule A Playlist On Revel Digital Online";
    boolean videoIsBig = true;
    boolean isNewAr = false;
    String imageID;

    private static final int PICK_IMAGE_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private File actualImage;
    private File compressedImage;
    private String picturelocation;

    private String mErrorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_beacons);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getPermission();

        buttonProgress = (ViewSwitcher) findViewById(R.id.buttonProgress);
        button = (Button) findViewById(R.id.save_edit);
        imageView = (ImageView) findViewById(R.id.imageView2);
        name = (EditText) findViewById(R.id.name);
        city = (EditText) findViewById(R.id.city);
        state = (EditText) findViewById(R.id.state);
        country = (EditText) findViewById(R.id.country);
        postal_code = (EditText) findViewById(R.id.postal_code);
        address = (EditText) findViewById(R.id.address);
        spinner = (Spinner) findViewById(R.id.type);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.dialogSwitch);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    isNewAr = false;
                    imageView.setImageResource(R.drawable.revel_qr);
                } else {
                    isNewAr = true;
                    if (beacon == null) {
                        imageView.setImageResource(R.drawable.ad_hawk_mascot_add);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        SharedPreferences prefs = getSharedPreferences(Globals.sharedPreferenceName, 0);
        apiKey = prefs.getString(Globals.apiKey, null);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra(Globals.isMade, false)) {
                beacon = Globals.findBeaconByID(intent.getStringExtra(Globals.deviceID));
                if (beacon != null) {
                    name.setText(beacon.getName());
                    city.setText(beacon.getCity());
                    state.setText(beacon.getState());
                    country.setText(beacon.getCountry());
                    postal_code.setText(beacon.getPostal_code());
                    address.setText(beacon.getAddress());
                    if (beacon.getType().equals("AR_BEACON")) {
                        spinner.setSelection(1);
                    } else {
                        spinner.setSelection(0);
                    }
                    spinner.setEnabled(false);
                    if (beacon.getType().equals("AR_BEACON")) {
                        imageView.setImageResource(R.drawable.ad_hawk_mascot);
                    } else {
                        if (beacon.getPlaylist_name() != null) {
                            if (beacon.getDisplayImage() == null) {
                                new create_qr_code(create_edit_beacons.this, this, beacon.getRegId(), apiKey, beacon.getPlaylist_id()).execute();
                                viewSwitcher.showNext();
                            } else {
                                imageView.setImageBitmap(beacon.getDisplayImage());
                            }
                        } else {
                            imageView.setImageResource(R.drawable.revel_qr);
                            Toast.makeText(this, noSchedule, Toast.LENGTH_LONG).show();
                            imageView.setColorFilter(Color.argb(150, 200, 200, 200));
                        }
                    }
                }
            } else {
                button.setText("CREATE BEACON");
            }
        }


    }


    public void imageClick(View v) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        View layout;
        if (beacon != null) {
            imageViewAnimation(imageView);
            if (beacon.isQRBeacon()) {

                if (beacon.getDisplayImage() != null) {
                    layout = inflater.inflate(R.layout.save_share_dialog, null);
                    builder = new AlertDialog.Builder(this);
                    builder.setView(layout);

                    builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.create().show();
                } else {
                    layout = inflater.inflate(R.layout.no_schedule_dialog, null);
                    VideoView videoView = (VideoView) layout.findViewById(R.id.videoView);
                    videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.demo));
                    videoView.setMediaController(new MediaController(this));
                    videoView.requestFocus();
                    videoView.start();
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setVolume(0f, 0f);
                            mp.setLooping(true);
                        }
                    });
                    builder = new AlertDialog.Builder(this);
                    builder.setView(layout);
                    builder.create().show();
                }

            } else {
                imageViewAnimation(imageView);
                layout = inflater.inflate(R.layout.pick_image_dialog, null);
                TextView pickText = (TextView) findViewById(R.id.pickText);
                if (pickText != null) {
                    pickText.setText("Select An Image Location To Change Current Associated Image");
                }
                builder = new AlertDialog.Builder(this);
                builder.setView(layout);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (vuforia_image image : Globals.vuforiaImagesFullList) {
                            Toast.makeText(create_edit_beacons.this, image.getName() + " : " + image.getId(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.create().show();
            }
        } else {
            if (isNewAr) {
                imageViewAnimation(imageView);
                layout = inflater.inflate(R.layout.pick_image_dialog, null);
                builder = new AlertDialog.Builder(this);
                builder.setView(layout);

                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                builder.create().show();
            } else {
                layout = inflater.inflate(R.layout.no_schedule_dialog, null);
                TextView noSchedule = (TextView) layout.findViewById(R.id.noScheduleText);
                if (noSchedule != null) {
                    noSchedule.setText("You'll Have To Schedule A Playlist On The Revel Digital Website(www.reveldigital.com) Before The QR Code Will Appear");
                }
                VideoView videoView = (VideoView) layout.findViewById(R.id.videoView);
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.demo));
                videoView.setMediaController(new MediaController(this));
                videoView.requestFocus();
                videoView.start();
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setVolume(0f, 0f);
                        mp.setLooping(true);
                    }
                });
                builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                builder.create().show();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void shareImage(View v) {
        if (beacon.getDisplayImage() != null && beacon.getType().equals("QR_BEACON")) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            beacon.getDisplayImage().compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File f = new File(Environment.getExternalStorageDirectory() + File.separator + beacon.getName() + "_" + beacon.getPlaylist_name() + ".jpg");
            try {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/" + beacon.getName() + "_" + beacon.getPlaylist_name() + ".jpg"));
            startActivity(Intent.createChooser(share, "Share Image"));
        }
    }

    public void saveImage(View v) {
        if (canWriteToStorage()) {
            Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show();
            MediaStore.Images.Media.insertImage(getContentResolver(), beacon.getDisplayImage(), "QR CODE FOR " + beacon.getName(), "Playlist: " + beacon.getPlaylist_name());
        } else {
            Toast.makeText(this, "This App Does Not Have Permission To Save Image. Please Give This App Permission In Phone Settings To Use This Option", Toast.LENGTH_LONG).show();
        }
    }

    public void getPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        0);

            }
        }
    }

    public boolean canWriteToStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public void imageViewAnimation(ImageView image) {
        final float growTo = 1.0f;
        final long duration = 1000;

        ScaleAnimation shrink = new ScaleAnimation(0.8f, 1, 0.8f, 1,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(duration / 2);

        ScaleAnimation grow = new ScaleAnimation(1, growTo, 1, growTo,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        grow.setDuration(duration / 3);
        grow.setStartOffset(duration / 2);
        AnimationSet growAndShrink = new AnimationSet(true);
        growAndShrink.setInterpolator(new LinearInterpolator());
        growAndShrink.addAnimation(shrink);
        growAndShrink.addAnimation(grow);
        image.startAnimation(growAndShrink);
    }

    public void ScaleVideo() {

    }

    public void getAllTargets(View v) {
        for (vuforia_image image : Globals.vuforiaImagesFullList) {
            if (image.getName() == null) {
                new Get_Target_Name(image.getId()).execute();
            }
        }
    }

    @Override
    public void getResults(String results) {
        viewSwitcher.showNext();
        if (results!=null && results.equals("success")) {
            imageView.setImageBitmap(beacon.getDisplayImage());
        } else {
            Toast.makeText(this, "There Was A Problem Getting The QR Code", Toast.LENGTH_SHORT).show();
            imageView.setColorFilter(Color.argb(150, 200, 200, 200));
        }
    }

    public void chooseImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void compressImage() {
        if (actualImage == null) {
        } else {


            // Compress image using RxJava in background thread
            Compressor.getDefault(this)
                    .compressToFileAsObservable(actualImage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<File>() {
                        @Override
                        public void call(File file) {
                            compressedImage = file;
                            if (compressedImage != null) {
                                imageView.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
                                picturelocation = storeImage(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
                                if(picturelocation!=null) {
                                    if(name.getText().toString()!=null && name.getText().toString().length()>0){

                                    }else{
                                        name.setText("Test");
                                    }

                                } else{
                                    Toast.makeText(create_edit_beacons.this, "There Was A Problem With Storing The Picture", Toast.LENGTH_SHORT).show();
                                }

                            }

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d("ErrorCreate",throwable.getMessage());
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Failed To Get Image", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                actualImage = FileUtil.from(this, data.getData());
                compressImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Failed To Get Image", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                actualImage = FileUtil.from(this, data.getData());
                compressImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    public void dispatchTakePictureIntent(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void registerBeacon(String activationCode) {
        String macAdd = "QR_BEACON";
        if(isNewAr){
            macAdd = "AR_BEACON";
        }
        Log.d("macadd",macAdd);
        final Device savedDevice = new Device()
                .setMacAddress(macAdd)
                .setName(name.getText().toString())
                .setTags(null)
                .setTimezone("");

        Location loc = new Location();
        loc.setAddress(address.getText().toString())
                .setCity(city.getText().toString())
                .setState(state.getText().toString())
                .setPostalCode(postal_code.getText().toString())
                .setCountry(country.getText().toString());

        try {
            loc.setLatitude(Double.parseDouble("0"));
            loc.setLongitude(Double.parseDouble("0"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            loc.setLatitude(0);
            loc.setLongitude(0);
        }

        savedDevice.setLocation(loc);

        com.reveldigital.api.Beacon beacon = new com.reveldigital.api.Beacon()
                .setWebsite("")
                .setEmail("")
                .setPhone("");
        try {
            beacon.setCalibration(1.0d);
        } catch (Exception ignored) {
        }
        try {
            beacon.setDistance(5.0d);
        } catch (Exception ignored) {
        }
        savedDevice.setBeacon(beacon);
        RegistrationTask regTask = new RegistrationTask(apiKey, activationCode, create_edit_beacons.this);
        regTask.execute(savedDevice);
    }

    @Override
    public void getRegKeyResults(String results) {
        if(results!=null){
            Log.d("regkeyresult",results);
            registerBeacon(results);
        }else {
            Log.d("regkeyresult","No result");
        }
    }

    @Override
    public void deviceCreationStatus(String regKey) {
        buttonProgress.showNext();
        if(regKey!=null){
            if(isNewAr){
                new Post_Img_Target(picturelocation, regKey).execute();
                Toast.makeText(this, "Device Created", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Device Created", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Encountered An Error. Device Was Not Created", Toast.LENGTH_SHORT).show();
        }
    }

    private class RegistrationTask extends AsyncTask<Device, Void, String> {
        private String apiKey;
        private DeviceService client;
        private String tmp;
        deviceCreated responder;
        RegistrationTask(String ApiKey, String activationCode, deviceCreated responder) {
            this.tmp = activationCode;
            this.apiKey = ApiKey;
            this.responder = responder;
        }

        @Override
        protected String doInBackground(final Device... params) {
            String status;
            client = new DeviceService.Builder()
                    .setApiKey(apiKey)
                    .build();


            try {
                Device device = client.createDevice(tmp, params[0]);
                if (device != null) {
                    status = device.getRegistrationKey();;
                } else {
                    status = null;
                }
            } catch (RequestException e) {
                mErrorMsg = e.getStatusMessage();
                status = null;
            } catch (Exception e) {
                mErrorMsg = e.getMessage();
                status = null;
            }
            return status;
        }

        @Override
        protected void onPostExecute(String regKey) {
            super.onPostExecute(regKey);
            responder.deviceCreationStatus(regKey);
        }
    }

    public void save_edit_beacon(View v){
        Log.d("regkeyresult","button");
        if(beacon!=null){
            updateBeacon();
        } else{
            if(name.getText().toString()==null || name.getText().toString().equals("")){
                Toast.makeText(this, "Please Enter A Name For Your Beacon And Try Again.", Toast.LENGTH_SHORT).show();

            }else if(isNewAr && picturelocation==null){
                Toast.makeText(this, "Please Select A Picture Before Creating A Image Beacon.", Toast.LENGTH_SHORT).show();
            }
            else {
                buttonProgress.showNext();
                new get_reg_key(create_edit_beacons.this).execute();
            }
        }
    }

    public void updateBeacon() {
        Device updatedDevice;

        updatedDevice = new Device()
                .setMacAddress(beacon.getType())
                .setName(name.getText().toString())
                .setTags("")
                .setTimezone("0");

        updatedDevice.setName(name.getText().toString())
                .setTags("")
                .setTimezone("0");

        Location loc = new Location();
        loc.setAddress(address.getText().toString())
                .setCity(city.getText().toString())
                .setState(state.getText().toString())
                .setPostalCode(postal_code.getText().toString())
                .setCountry(country.getText().toString());

        loc.setLatitude(0.0d);
        loc.setLongitude(0.0d);

        updatedDevice.setLocation(loc);

        com.reveldigital.api.Beacon beacon = new com.reveldigital.api.Beacon()
                .setWebsite("Updated")
                .setEmail("Updated")
                .setPhone("Updated");

        updatedDevice.setBeacon(beacon);
        ModificationTask editTask = new ModificationTask(apiKey);
        editTask.execute(updatedDevice);
    }
    public class ModificationTask extends AsyncTask<Device, Void, Boolean> {
        private String apiKey;
        private DeviceService client;

        ModificationTask(String ApiKey) {
            this.apiKey = ApiKey;
        }

        @Override
        protected Boolean doInBackground(final Device... params) {
            boolean status = false;
            client = new DeviceService.Builder()
                    .setApiKey(apiKey)
                    .build();
            try {
                Device tmp = params[0];
                Device device = client.updateDevice(tmp);
                if (device != null) {
                    status = true;
                } else {
                    status = false;
                }
            } catch (RequestException e) {
                mErrorMsg = e.getStatusMessage();
                status = false;
            } catch (Exception e) {
                mErrorMsg = e.getMessage();
                status = false;
            }
            return status;
        }
    }
    private String storeImage(Bitmap image) {
        String TAG = "Picture";
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return pictureFile.toString();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return null;
    }
    
    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this. 
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }
    public void broadcastIntent(View view){
        Intent intent = new Intent();
        intent.setAction("com.tutorialspoint.CUSTOM_INTENT");
        sendBroadcast(intent);
    }
}