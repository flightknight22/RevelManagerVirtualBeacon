package com.reveldigital.revelmanagervirtualbeacon.Activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.Interface.IResponder;
import com.reveldigital.revelmanagervirtualbeacon.R;
import com.reveldigital.revelmanagervirtualbeacon.adapters.virtualBeaconAdapter;
import com.reveldigital.revelmanagervirtualbeacon.Beacon.Rest.Call.getDevices;
import com.reveldigital.revelmanagervirtualbeacon.vuforia.Get_Targets;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IResponder {
    private ArrayList<Beacon> beaconList;
    private RecyclerView recyclerView;
    private virtualBeaconAdapter mAdapter;
    AlertDialog.Builder builder;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(Globals.sharedPreferenceName, MODE_PRIVATE);
        beaconList = Globals.beaconFullList;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new virtualBeaconAdapter(beaconList,this);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
        if(Globals.vuforiaApiKey_Access!=null && Globals.vuforiaApiKey_Secret!=null){
            new Get_Targets().execute();
        }


    }
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String restoredText = prefs.getString(Globals.apiKey, "");
        if(restoredText!=null) {
            //new getDevices(MainActivity.this, getApplicationContext(), restoredText).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }
    public void createBeacon(){
        Intent intent = new Intent(this,create_edit_beacons.class);
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
                builder=new AlertDialog.Builder(this);
                builder.setView(layout);
                final EditText settingApiKey = (EditText) layout.findViewById(R.id.settingsApiKey);
                String restoredText = prefs.getString(Globals.apiKey, "");
                settingApiKey.setText(restoredText);
                builder.setPositiveButton("Save Changes?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String apiKey = settingApiKey.getText().toString();
                        if(apiKey!=null){
                            new getDevices(MainActivity.this,getApplicationContext(),apiKey).execute();
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
                new getDevices(MainActivity.this,getApplicationContext(),apiKey).execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void getResults(String results) {
        if(results!=null){
            mAdapter.swap();
        }
    }
}
