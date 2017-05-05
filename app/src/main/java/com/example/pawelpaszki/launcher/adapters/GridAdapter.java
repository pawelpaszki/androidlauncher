package com.example.pawelpaszki.launcher.adapters;

/**
 * Created by PawelPaszki on 03/05/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PictureDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.AppDetail;
import com.example.pawelpaszki.launcher.AppsListActivity;
import com.example.pawelpaszki.launcher.R;

import java.util.List;

public class GridAdapter extends BaseAdapter{
    List<AppDetail> apps;
    Context context;
    PackageManager manager;
    private static LayoutInflater inflater=null;
    public GridAdapter(AppsListActivity mainActivity, List<AppDetail> apps, PackageManager manager) {
        // TODO Auto-generated constructor stub
        this.apps=apps;
        context=mainActivity;
        this.manager = manager;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public Object getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView textView;
        ImageView imageView;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            LayoutInflater li = inflater;
            v = li.inflate(R.layout.apps_list, null);
        } else {
            v = convertView;
        }
        TextView textView=(TextView) v.findViewById(R.id.item_app_label);
        ImageView imageView=(ImageView) v.findViewById(R.id.item_app_icon);

        String text = (String) apps.get(position).getLabel();
        if(text.length() > 14) {
            text = text.substring(0,11) + "...";
        }
        textView.setText(text);
        imageView.setImageDrawable(apps.get(position).getIcon());
        Bitmap bitmap = BitmapFactory.decodeResource(v.getResources(), R.mipmap.imageviewbg);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(v.getResources(), bitmap);
        roundedBitmapDrawable.setCornerRadius(30f);
        imageView.setBackground(roundedBitmapDrawable);
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = manager.getLaunchIntentForPackage(apps.get(position).getName().toString());
                context.startActivity(i);
            }
        });

        return v;
    }

}

