package com.reveldigital.revelmanagervirtualbeacon.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import android.support.annotation.NonNull;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.reveldigital.api.Device;
import com.reveldigital.api.Location;
import com.reveldigital.api.RequestException;
import com.reveldigital.api.service.DeviceService;
import com.reveldigital.api.util.Base64;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.GetRevelRegistrationKey;
import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.reveldigital.revelmanagervirtualbeacon.Classes.ImageDBEntry;
import com.reveldigital.revelmanagervirtualbeacon.Classes.VuforiaImage;
import com.reveldigital.revelmanagervirtualbeacon.Classes.VuforiaKey;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;
import com.reveldigital.revelmanagervirtualbeacon.R;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.CreateQRCode;
import com.reveldigital.revelmanagervirtualbeacon.Adapters.ImageAdapter;
import com.reveldigital.revelmanagervirtualbeacon.MIsc.CompressImage;
import com.reveldigital.revelmanagervirtualbeacon.Vuforia.GetVuforiaTargetName;
import com.reveldigital.revelmanagervirtualbeacon.Vuforia.UploadImageToVuforia;


import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import id.zelory.compressor.Compressor;
import id.zelory.compressor.FileUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.FirebaseDatabase;
import com.reveldigital.revelmanagervirtualbeacon.Vuforia.DeleteVuforiaTarget;


public class CreateEditBeaconsActivity extends AppCompatActivity implements IResponder {
    Button button;
    ImageView imageView;
    EditText name, city, state, country, postal_code, address, imageName;
    Spinner spinner;
    ViewSwitcher viewSwitcher, buttonImageGeneral;
    Beacon beacon;
    String apiKey;
    AlertDialog.Builder builder;
    ViewSwitcher buttonProgress;
    String noSchedule = "No Schedule Found For This Beacon. Please Schedule A Playlist On Revel Digital Online";
    TextView pickText, workingText;
    LinearLayout imageNameContainer;
    LayoutInflater inflater;
    View layout;
    Uri picUri;

    DataSnapshot db_snapshot;


    boolean videoIsBig = true;
    boolean isNewAr = false;
    String imageID;
    public static final String ANONYMOUS = "anonymous";
    private String mUsername;
    private static final int PICK_IMAGE_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    private File actualImage;
    private File compressedImage;
    private File thumbcompressedImage;
    private String picturelocation;
    private String mErrorMsg;
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG;
    private boolean isPreviousImage = false;
    String vuforiaKey = null;
    VuforiaImage vuforiaImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_beacons);

        TAG = "CREATE BEACON ACTIVITY";

        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername = ANONYMOUS;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    signInAnonymously();
                }
                
            }
        };

        getPermission();

        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

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
        pickText = (TextView) findViewById(R.id.pickText);
        workingText = (TextView) findViewById(R.id.textView3);
        buttonImageGeneral = (ViewSwitcher) findViewById(R.id.imageSelectInformation);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.dialogSwitch);
        imageNameContainer = (LinearLayout) findViewById(R.id.imageNameContainer);
        imageName = (EditText) findViewById(R.id.imageName);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    isNewAr = false;
                    imageView.setImageResource(R.drawable.revel_qr);
                    imageNameContainer.setVisibility(View.GONE);
                } else {
                    isNewAr = true;
                    if (beacon == null) {
                        imageView.setImageResource(R.drawable.ad_hawk_mascot_add);
                        imageNameContainer.setVisibility(View.VISIBLE);

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
                        if (beacon.getPlaylist_name()!= null) {

                            if (beacon.getDisplayImage() == null) {
                                new CreateQRCode(CreateEditBeaconsActivity.this, this, beacon.getRegId(), apiKey, beacon.getPlaylist_id()).execute();
                                viewSwitcher.showNext();
                            } else {
                                imageView.setImageBitmap(beacon.getDisplayImage());
                            }
                        } else {
                            imageView.setImageResource(R.drawable.revel_qr);
                            if(Globals.doneloading) {
                                Toast.makeText(this, noSchedule, Toast.LENGTH_LONG).show();
                            }
                                imageView.setColorFilter(Color.argb(150, 200, 200, 200));

                        }
                    }
                }
            } else {
                button.setText("CREATE BEACON");
            }
        }
        if(!Globals.doneloading && beacon!=null){
            Toast.makeText(this, "Device Information Is Still Loading. More Information May Be Available Shortly.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void pickFromVuforia(View v){
        if(Globals.getVuforiaKeys()!=null) {
            showImageAlertDialog(Globals.getVuforiaKeys(), null);
        } else{
            Toast.makeText(this, "Image Database Does Not Contain Any Images", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInAnonymously() {
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(CreateEditBeaconsActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void imageClick(View v) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if (beacon != null) {

            if (beacon.isQRBeacon()) {
                imageViewAnimation(imageView);
                if (beacon.getDisplayImage() != null) {
                    layout = inflater.inflate(R.layout.save_share_dialog, null);
                    builder = new AlertDialog.Builder(this);
                    builder.setView(layout);
                    builder.create().show();
                } else {
                    showNoScheduleDialog("");
                }

            } else {
                /**
                imageViewAnimation(imageView);
                buttonImageGeneral.showNext();
                if (pickText != null) {
                    pickText.setText("Select An Image Location To Change Current Associated Image");
                }
                 **/ //// TODO: 1/16/17 implement 

            }
        } else {
            if (isNewAr) {
                imageViewAnimation(imageView);
                buttonImageGeneral.showNext();

                pickText.setText("Select An Image Location Add Image");

            } else {
                showNoScheduleDialog("");
            }
        }

    }

    public void showImageAlertDialog(final ArrayList<String> keys, final String currentKey) {
        final ArrayList<String> keysList = keys;
        layout = inflater.inflate(R.layout.image_grid_dialog, null);
        builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        final AlertDialog alert = builder.create();
        alert.show();
        alert.setCancelable(false);
        Button b = (Button) layout.findViewById(R.id.button6);
        TextView t = (TextView) layout.findViewById(R.id.gridviewTitle);
        if(currentKey==null){
            t.setText("Previous Registered Images");
            alert.setCancelable(true);
        }
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentKey!=null){
                    workingText.setText("Creating Registration Key...");
                    new GetRevelRegistrationKey(CreateEditBeaconsActivity.this).execute();
                }
                alert.cancel();
            }
        });

        GridView gridview = (GridView) layout.findViewById(R.id.gridview);


        gridview.setNumColumns(2);
        gridview.setPadding(3,3,3,3);
        gridview.setAdapter(new ImageAdapter(this, keysList, 550));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                vuforiaImage = Globals.findVuforiaImageById(keys.get(position));
                imageView.setImageBitmap(vuforiaImage.getImage());
                alert.cancel();
                if(beacon!=null){

                } else {
                    isPreviousImage = true;

                    if(currentKey!=null){ //if just selceting an image
                        new DeleteVuforiaTarget(vuforiaKey).execute();
                        workingText.setText("Creating Revel Device...");
                        new GetRevelRegistrationKey(CreateEditBeaconsActivity.this).execute();
                    } else {
                        buttonImageGeneral.showNext();
                        imageNameContainer.setVisibility(View.GONE);
                    }
                }
                //refreshMain("Device Created Using Previous Image");
                //finish();

            }
        });





    }

    public void showNoScheduleDialog(String text){
        layout = inflater.inflate(R.layout.no_schedule_dialog, null);
        builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.create().show();

        final VideoView videoView = (VideoView) layout.findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.demo));
        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                mp.setVolume(0f, 0f);
                mp.setLooping(true);
            }
        });
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
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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

    public void getAllTargets(View v) {
        for (VuforiaImage image : Globals.vuforiaImagesFullList) {
            if (image.getName() == null) {
                new GetVuforiaTargetName(image.getId()).execute();
            }
        }
    }

    public void dispatchTakePictureIntent(View v) {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            File file=getOutputMediaFile();
            picUri = Uri.fromFile(file); // create
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,picUri); // set the image file

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
        else {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
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
                            thumbcompressedImage = file;
                            if (thumbcompressedImage != null) {
                                new CompressImage(actualImage, CreateEditBeaconsActivity.this, CreateEditBeaconsActivity.this).execute();
                            } else {
                                Toast.makeText(CreateEditBeaconsActivity.this, "There Was A Problem With Storing The Picture", Toast.LENGTH_SHORT).show();
                            }


                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d("ErrorCreate", throwable.getMessage());
                        }
                    });
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


    //Edit Beacon Methods

    public void updateBeacon() {
            buttonProgress.showNext();
            workingText.setText("Modifying Device");
            Location loc = new Location();
            loc.setAddress(address.getText().toString())
                    .setCity(city.getText().toString())
                    .setState(state.getText().toString())
                    .setPostalCode(postal_code.getText().toString())
                    .setCountry(country.getText().toString());

            loc.setLatitude(0.0d);
            loc.setLongitude(0.0d);



            ModificationTask editTask = new ModificationTask(apiKey, beacon.getDeviceId(), loc, name.getText().toString(), this);
            editTask.execute();
    }

    
    public class ModificationTask extends AsyncTask<Device, Void, Boolean> {
        private String apiKey;
        private DeviceService client;
        private String id;
        private Location loc;
        private String name;
        IResponder r;

        ModificationTask(String ApiKey, String id, Location loc, String name, IResponder r) {
            this.apiKey = ApiKey;
            this.id = id;
            this.loc = loc;
            this.name = name;
            this.r = r;

        }

        @Override
        protected Boolean doInBackground(final Device... params) {
            boolean status = false;
            client = new DeviceService.Builder()
                    .setApiKey(apiKey)
                    .build();



            try {
                Device tmp=client.getDevice(id);
                //Device tmp = params[0];
                tmp.setName(name);
                tmp.setLocation(loc);
                Device device = client.updateDevice(tmp);
                status = device != null;
            } catch (RequestException e) {
                mErrorMsg = e.getStatusMessage();

                status = false;
            } catch (Exception e) {
                e.printStackTrace();

                status = false;
            }
            return status;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            r.getModDeviceStatus(aBoolean);
        }
    }

    @Override
    public void getModDeviceStatus(Boolean bool) {
        if(bool){
            refreshMain("Device Modified");
            finish();
        } else {
            buttonProgress.showNext();
            Toast.makeText(this, "There Was An Issue. The Device Was Not Modified", Toast.LENGTH_SHORT).show();
        }
    }

    //Register Beacon Methods

    public void save_edit_beacon(View v){

        if (beacon != null) {
            if(picturelocation!=null || isPreviousImage){
                if (isPreviousImage != true) {
                    if (imageName != null && imageName.getText().toString().length() > 0) {
                        buttonProgress.showNext();
                        workingText.setText("Uploading Image...");
                        new UploadImageToVuforia(picturelocation, imageName.getText().toString(), CreateEditBeaconsActivity.this).execute();
                    } else {
                        Toast.makeText(this, "Please Enter A Name For Your Image And Try Again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    buttonProgress.showNext();
                    workingText.setText("Creating Registration Key...");
                    String key;
                    for(DataSnapshot snapshot:Globals.snapshot.child(Globals.vuforiaApiKey_Access_cleint).getChildren()) {

                        if (snapshot.child("name").getValue(String.class).equals(vuforiaImage.getName())) {
                            key = snapshot.getKey();
                            try {
                                String modifiedJSON = new JSONObject(snapshot.child("jsonObjectString").getValue(String.class)).put(beacon.getRegId(), apiKey).toString();
                                writeModifiedJSONToFirebase(key, modifiedJSON);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "There Was An Issue Writing The Metadata. Please Delete The Device On The Revel Digital Website And Try Again.", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                }
            }
            updateBeacon();
        } else {
            if (name.getText().toString() == null || name.getText().toString().equals("")) {
                Toast.makeText(this, "Please Enter A Name For Your Beacon And Try Again.", Toast.LENGTH_SHORT).show();
                name.setBackgroundColor(getResources().getColor(R.color.cardview_shadow_end_color));
            } else {
                if (isNewAr) {
                    if(picturelocation!=null || isPreviousImage) {
                        if (isPreviousImage != true) {
                            if (imageName != null && imageName.getText().toString().length() > 0) {
                                buttonProgress.showNext();
                                workingText.setText("Uploading Image...");
                                new UploadImageToVuforia(picturelocation, imageName.getText().toString(), CreateEditBeaconsActivity.this).execute();
                            } else {
                                Toast.makeText(this, "Please Enter A Name For Your Image And Try Again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            buttonProgress.showNext();
                            workingText.setText("Creating Registration Key...");
                            new GetRevelRegistrationKey(CreateEditBeaconsActivity.this).execute();
                        }
                    } else{
                        Toast.makeText(this, "Please Select A Image And Try Again", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    buttonProgress.showNext();
                    workingText.setText("Creating Registration Key...");
                    new GetRevelRegistrationKey(CreateEditBeaconsActivity.this).execute();
                }

            }
        }
    }

    private void registerBeacon(String activationCode) {
        String macAdd = "QR_BEACON";
        if(isNewAr){
            macAdd = "AR_BEACON";
        }

        final Device savedDevice = new Device()
                .setMacAddress(macAdd)
                .setName(name.getText().toString())
                .setTags(null)
                .setTimezone("Dateline Standard Time");

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
                .setWebsite(Globals.vuforiaApiKey_Access_cleint)
                .setEmail(Globals.vuforiaApiKey_Secret_client)
                .setPhone("");
        savedDevice.setBeacon(beacon);
        RegistrationTask regTask = new RegistrationTask(apiKey, activationCode, CreateEditBeaconsActivity.this);
        regTask.execute(savedDevice);
    }

    private class RegistrationTask extends AsyncTask<Device, Void, String> {
        private String apiKey;
        private DeviceService client;
        private String tmp;
        IResponder responder;
        RegistrationTask(String ApiKey, String activationCode, IResponder responder) {
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
                    status = device.getRegistrationKey();
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

    @Override
    public void getResults(String results) {
        viewSwitcher.showNext();
        if (results!=null && results.equals("success")) {
            imageView.setImageBitmap(beacon.getDisplayImage());
        } else {
            if(Globals.doneloading) {
                Toast.makeText(this, "There Was A Problem Getting The QR Code", Toast.LENGTH_SHORT).show();
                imageView.setColorFilter(Color.argb(150, 200, 200, 200));
            }
        }
    }

    @Override
    public void getRegKeyResults(String results) {
        if(results!=null){
            workingText.setText("Creating Revel Device...");
            registerBeacon(results);
        }
    }

    @Override
    public void getCompressedFile(File file) {
        if(file!=null) {
            compressedImage = file;
            ImageDBEntry image = new ImageDBEntry();
            image.setName("Unique Name");
            try {
                byte[] imageBytes = FileUtils.readFileToByteArray(thumbcompressedImage);
                image.setEncodedImage(Base64.encodeBytes(imageBytes));
                picturelocation = storeImage(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
            } catch (Exception ignored) {

            }
            viewSwitcher.showNext();
            buttonImageGeneral.showNext();
            imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        }
    }

    @Override
    public void getVuforiaResponse(ArrayList<String> duplicates, String name) {
        vuforiaKey = name;
        if(duplicates!=null) {
            if(duplicates.get(0).equals("-1")) {


                workingText.setText("Creating Registration Key...");
                new GetRevelRegistrationKey(CreateEditBeaconsActivity.this).execute();

            }
            else {

                workingText.setText("Found Duplicates...");
                showImageAlertDialog(duplicates, name);
            }
        } else{
            buttonProgress.showNext();
            Toast.makeText(this, "There Was An Issue Uploading The Image. Please Try Again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void deviceCreationStatus(String regKey) {
        workingText.setText("Finishing...");
        if(regKey!=null){
            if(isNewAr) {

                if(!isPreviousImage) {
                    workingText.setText("Writing Metadata...");
                    writeNewToFirebase(regKey);
                    refreshMain("Device Created");
                } else{
                    workingText.setText("Writing Metadata...");
                    String key;

                        //String modifiedJSON = vuforiaImage.getJsonObject();
                        for(DataSnapshot snapshot:Globals.snapshot.child(Globals.vuforiaApiKey_Access_cleint).getChildren()) {

                            if (snapshot.hasChild("name") && snapshot.child("name").getValue(String.class).equals(vuforiaImage.getName())) {
                                key = snapshot.getKey();
                                try {
                                    String modifiedJSON = new JSONObject(snapshot.child("jsonObjectString").getValue(String.class)).put(regKey, apiKey).toString();

                                    writeModifiedJSONToFirebase(key, modifiedJSON);
                                    refreshMain("Device Created Using Previous Image");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "There Was An Issue Writing The Metadata. Please Delete The Device On The Revel Digital Website And Try Again.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                finish();

                            }
                        }




                }
                finish();
            } else {
                refreshMain("Device Created");
                finish();
            }
        } else {
            if(vuforiaKey!=null && isNewAr) {
                workingText.setText("Rolling Back Changes...");
                new DeleteVuforiaTarget(vuforiaKey).execute();
            }
            buttonProgress.showNext();
            Toast.makeText(this, "Encountered An Error. Device Was Not Created", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void getVuforiaUploadResults(String results) {
        if(results!=null){
            if(results.equals("TargetNameExist")){
                Toast.makeText(this, "Image Name Already Exists. Please Enter A Different Image Name.", Toast.LENGTH_LONG).show();
                buttonProgress.showNext();
            }else {

                workingText.setText("Checking For Duplicate Images...");
            }
        } else {
            Toast.makeText(this, "There Was An Issue Uploading The Picture", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isPreviousImage = false;
        imageNameContainer.setVisibility(View.VISIBLE);
        if(resultCode != RESULT_CANCELED) {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {

                if (data == null) {
                    Toast.makeText(this, "Failed To Get Image", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {

                    actualImage = FileUtil.from(this, data.getData());
                    viewSwitcher.showNext();
                    compressImage();
                } catch (Exception e) {
                    viewSwitcher.showNext();
                    e.printStackTrace();
                }
            }

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (data == null) {
                        Toast.makeText(this, "Failed To Get Image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {

                        actualImage = FileUtil.from(this, data.getData());
                        viewSwitcher.showNext();
                        compressImage();
                    } catch (Exception e) {
                        viewSwitcher.showNext();
                        e.printStackTrace();
                    }
                }else{
                    try {
                        Uri uri=picUri;
                        actualImage = new File(uri.getPath());
                        viewSwitcher.showNext();
                        compressImage();
                    } catch (Exception e) {
                        viewSwitcher.showNext();
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public void writeNewToFirebase(String regKey){
        ImageDBEntry image = new ImageDBEntry();
        JSONObject jsnObject = new JSONObject();
        try {
            jsnObject.put(regKey, apiKey);

        } catch (Exception e){

            e.printStackTrace();
        }

        if(compressedImage!=null) {
            try {
                byte[] imageBytes = FileUtils.readFileToByteArray(compressedImage);
                String encodedImage = Base64.encodeBytes(imageBytes);
                image.setEncodedImage(encodedImage);
                image.setJsonObjectString(jsnObject.toString());
                image.setName(imageName.getText().toString());
                image.setKey(vuforiaKey);
            } catch (Exception ignored){}

        }
        if(image!=null) {
            VuforiaKey keyObj = new VuforiaKey(Globals.vuforiaApiKey_Access, Globals.vuforiaApiKey_Secret);
            if(!Globals.hasKeys) {
                mFirebaseDatabaseReference.child(Globals.vuforiaApiKey_Access_cleint).push().setValue(keyObj);
            }
            mFirebaseDatabaseReference.child(Globals.vuforiaApiKey_Access_cleint)
                    .push().setValue(image);
        }
    }

    public void writeModifiedJSONToFirebase(String nodeName, String jsonString){
        mFirebaseDatabaseReference.child(Globals.vuforiaApiKey_Access_cleint).child(nodeName).child("jsonObjectString").setValue(jsonString);

    }

    public void refreshMain(String message){
        Intent newDevice = new Intent("broadcast.image.reco");
        newDevice.putExtra("Message", message);
        sendBroadcast(newDevice);
    }

    public void stopImageSlecetion(View v){
        buttonImageGeneral.showNext();
    }
}