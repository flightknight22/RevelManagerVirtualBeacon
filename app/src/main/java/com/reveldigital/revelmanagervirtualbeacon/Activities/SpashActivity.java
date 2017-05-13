package com.reveldigital.revelmanagervirtualbeacon.Activities;

/**
 * Created by Avery Knight on 12/13/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.GetRevelDevices;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;

import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponderString;
import com.reveldigital.revelmanagervirtualbeacon.R;
import com.reveldigital.revelmanagervirtualbeacon.Vuforia.GetVuforiaTargetIDs;

public class SpashActivity extends AppCompatActivity implements IResponderString {
    EditText editText;
    ViewSwitcher viewSwitcher;
    String apiKey;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static final String ANONYMOUS = "anonymous";
    private String mUsername;
    String TAG = "Splash";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        editText = (EditText) findViewById(R.id.login);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
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
                    //Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mUsername = ANONYMOUS;
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Globals.snapshot = snapshot;
            }

        });
        Thread myThread = new Thread(){
            @Override
            public void run() {
                try {
                    // Initialize Firebase Auth



                    SharedPreferences prefs = getSharedPreferences(Globals.sharedPreferenceName, 0);
                    apiKey = prefs.getString(Globals.apiKey,null);
                    if(apiKey!=null){
                        editText.setText(apiKey);
                        if(isNetworkAvailable()){
                            new GetRevelDevices(SpashActivity.this, getApplicationContext(), apiKey).execute();
                            if(Globals.vuforiaApiKey_Access!=null && Globals.vuforiaApiKey_Secret!=null){
                                new GetVuforiaTargetIDs().execute();
                            }
                        } else {
                            Toast.makeText(SpashActivity.this, "No Active Internet Connection! Please Check Connectivity And Try Again.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        viewSwitcher.showNext();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void getStringResults(String results) {
        if(results!=null) {
            goToMain();
        }else {
            Toast.makeText(this, "Something Went Wrong. Please Check API KEY And Try Again.", Toast.LENGTH_SHORT).show();
            viewSwitcher.showNext();
        }
    }

    public void goToMain(){
        editText.setText(apiKey);
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
    public void checkApi(View v){
        viewSwitcher.showNext();
        SharedPreferences.Editor editor = getSharedPreferences(Globals.sharedPreferenceName, MODE_PRIVATE).edit();
        String collectedApiKey = editText.getText().toString();
        if(collectedApiKey!=null && collectedApiKey!=""){
            collectedApiKey = collectedApiKey.replaceAll("\\s+","");
            editor.putString(Globals.apiKey, collectedApiKey);
            editor.commit();
            new GetRevelDevices(SpashActivity.this, getApplicationContext(), collectedApiKey).execute();
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
                            Toast.makeText(SpashActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}