package com.reveldigital.revelmanagervirtualbeacon.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.reveldigital.revelmanagervirtualbeacon.Classes.VuforiaImage;
import com.reveldigital.revelmanagervirtualbeacon.Globals.Globals;
import com.reveldigital.revelmanagervirtualbeacon.R;

import java.util.ArrayList;

/**
 * Created by averyknight on 12/23/16.
 */

public class ImageAdapter extends BaseAdapter
{
    private Context context;
    private LayoutInflater layoutInflator;
    private ArrayList<String> keys;
    private int width;


    public ImageAdapter(Context c, ArrayList<String> keys, int width){
      context = c;
      layoutInflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.keys = keys;
        this.width = width;
     }

     public int getCount() {
          return keys.size();
     }

     public Object getItem(int position) {
          return null;
     }

     public long getItemId(int position) {
          return 0;
     }

     public View getView(int position, View grid, ViewGroup parent){
         VuforiaImage image = Globals.findVuforiaImageById(keys.get(position));
         ImageView imageView;
         TextView textView;

         if(grid == null) {
             grid = layoutInflator.inflate(R.layout.image, null);
             imageView =  (ImageView)grid.findViewById(R.id.gridviewImage);
             textView = (TextView) grid.findViewById(R.id.gridviewImagename);
             grid.setTag(imageView);
             //grid.setTag(textView);
             if(image!=null && image.getName()!=null && image.getName().length()>0){
                 textView.setText(image.getName());
             }else {
                 textView.setText("Not Loaded");
             }
             if(image!=null && image.getImage()!=null){
                 imageView.setImageBitmap(image.getImage());
             } else {
                 imageView.setImageResource(R.drawable.ad_hawk_mascot);
             }
             imageView.getLayoutParams().width = width;
             imageView.getLayoutParams().height = width;
          }else{
             imageView = (ImageView)grid.getTag();
          }
         return grid;

      }

  }
