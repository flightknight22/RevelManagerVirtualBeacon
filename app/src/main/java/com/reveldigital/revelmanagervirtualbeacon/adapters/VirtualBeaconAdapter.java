package com.reveldigital.revelmanagervirtualbeacon.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.reveldigital.revelmanagervirtualbeacon.Classes.Beacon;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.R;
import com.reveldigital.revelmanagervirtualbeacon.Activities.CreateEditBeaconsActivity;

import java.util.ArrayList;

/**
 * Created by Avery Knight on 11/28/2016.
 */

public class VirtualBeaconAdapter extends RecyclerView.Adapter<VirtualBeaconAdapter.MyViewHolder> {
    private ArrayList<Beacon> beaconList;
    private Context context;
    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView beaconName, type;
        public ImageView imageView;
        public CardView container;
        public MyViewHolder(View view) {
            super(view);
            beaconName = (TextView) view.findViewById(R.id.name);
            type= (TextView) view.findViewById(R.id.type);
            imageView = (ImageView) view.findViewById(R.id.imageView);
            container = (CardView) view.findViewById(R.id.cardview);
        }
    }
    public VirtualBeaconAdapter(ArrayList<Beacon> beaconList, Context context){
        this.beaconList = beaconList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final Beacon beacon = beaconList.get(position);
        holder.beaconName.setText(beacon.getName());

        if(beacon.getType().equals("AR_BEACON")){
            holder.type.setText("Image Beacon");
            holder.imageView.setImageResource(R.drawable.ad_hawk_mascot);
        }else{
            holder.type.setText("QR Code Beacon");
            holder.imageView.setImageResource(R.drawable.revel_qr);
        }
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,CreateEditBeaconsActivity.class);
                intent.putExtra(Globals.isMade,true);
                intent.putExtra(Globals.deviceID,beacon.getDeviceId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(beaconList!=null && !beaconList.isEmpty()) {
            return beaconList.size();
        } else {
            return 0;
        }
    }
    public void swap(){
        this.beaconList = Globals.beaconFullList;
        notifyDataSetChanged();
    }
}

