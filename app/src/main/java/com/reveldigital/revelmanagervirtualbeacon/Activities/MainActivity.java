package com.reveldigital.revelmanagervirtualbeacon.Activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponderString;
import com.reveldigital.revelmanagervirtualbeacon.R;
import com.reveldigital.revelmanagervirtualbeacon.Adapters.VirtualBeaconAdapter;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.GetRevelDevices;
import com.reveldigital.revelmanagervirtualbeacon.Vuforia.GetVuforiaTargetIDs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements IResponderString {
    private ArrayList<Beacon> beaconList;
    private RecyclerView recyclerView;
    private VirtualBeaconAdapter mAdapter;
    private BroadcastReceiver listener;
    AlertDialog.Builder builder;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(Globals.sharedPreferenceName, MODE_PRIVATE);
        beaconList = Globals.beaconFullList;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new VirtualBeaconAdapter(beaconList,this);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);



    }



    @Override
    protected void onResume() {
        super.onResume();
        String restoredText = prefs.getString(Globals.apiKey, "");
        if (restoredText != null) {
            //new GetRevelDevices(MainActivity.this, getApplicationContext(), restoredText).execute();
        }
        if (listener == null) {

            listener = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        String message = bundle.getString("Message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                    String apiKey = prefs.getString(Globals.apiKey, null);
                    new GetRevelDevices(MainActivity.this,getApplicationContext(),apiKey).execute();
                    new GetVuforiaTargetIDs().execute();

                }
            };
            registerReceiver(listener, new IntentFilter("broadcast.image.reco"));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }
    public void createBeacon(){
        Intent intent = new Intent(this, CreateEditBeaconsActivity.class);
        intent.putExtra(Globals.isMade,false);
        startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.activity_settings, null);
        switch (item.getItemId()) {
            case R.id.action_add_beacon:
                createBeacon();
                return true;
            case R.id.settings:
                builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                final AutoCompleteTextView settingApiKey = (AutoCompleteTextView) layout.findViewById(R.id.settingsApiKey);
                String restoredText = prefs.getString(Globals.apiKey, "");
                final Set<String> apiKeys = prefs.getStringSet(Globals.apiKeyList, new HashSet<String>());
                settingApiKey.setText(restoredText);
                if (apiKeys!=null) {
                    Log.d("APIKEYLIST","Set Adapter");
                    Object[] arr = apiKeys.toArray();
                   for (int i = 0; i < arr.length; i++) {
                       Log.d("APIKEYLIST", arr[i].toString());
                   }
                    ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, apiKeys.toArray());
                    settingApiKey.setAdapter(adapter);
                    settingApiKey.setThreshold(1);
                }
                builder.setPositiveButton("Save Changes?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor editor = getSharedPreferences(Globals.sharedPreferenceName, MODE_PRIVATE).edit();
                        String apiKey = settingApiKey.getText().toString().replaceAll("\\s+","");
                        editor.putString(Globals.apiKey, apiKey);
                        if(apiKeys!=null && !apiKeys.contains(apiKey)){
                            apiKeys.add(apiKey);
                            editor.putStringSet(Globals.apiKeyList, apiKeys);
                            Log.d("APIKEYLIST","items in list: "+apiKeys.size());
                        }
                        editor.commit();
                        if(apiKey!=null){
                            new GetRevelDevices(MainActivity.this,getApplicationContext(),apiKey).execute();
                        }
                    }
                })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
                return true;
            case R.id.refresh:
                String apiKey = prefs.getString(Globals.apiKey, null);
                if(Globals.doneloading) {
                    new GetRevelDevices(MainActivity.this, getApplicationContext(), apiKey).execute();
                } else {
                    Toast.makeText(this, "The Device Is Already Refreshing.", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void getStringResults(String results) {
        if(results!=null){
            mAdapter.swap();
        }
    }
}
