package com.pawelpaszki.launcher.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pawelpaszki.launcher.AppDetail;
import com.pawelpaszki.launcher.R;

import java.util.ArrayList;

/**
 * Created by PawelPaszki on 22/05/2017.
 */

public class AppIconAdapter extends ArrayAdapter<AppDetail> {
    public AppIconAdapter(@NonNull Context context, ArrayList<AppDetail> apps) {
        super(context, 0, apps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        AppDetail app = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dock_item, parent, false);
        }
        // Lookup view for data population
        TextView iconName = (TextView) convertView.findViewById(R.id.dock_app_name);
        ImageView appIcon = (ImageView) convertView.findViewById(R.id.dock_app_icon);
        // Populate the data into the template view using the data object
        appIcon.setImageDrawable(app.getmIcon());
        iconName.setText(app.getmName());
        // Return the completed view to render on screen
        return convertView;
    }
}
