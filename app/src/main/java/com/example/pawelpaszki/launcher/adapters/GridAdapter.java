package com.example.pawelpaszki.launcher.adapters;

/**
 * Created by PawelPaszki on 03/05/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.AppDetail;
import com.example.pawelpaszki.launcher.AppsListActivity;
import com.example.pawelpaszki.launcher.R;
import com.example.pawelpaszki.launcher.utils.BitMapFilter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.util.List;

public class GridAdapter extends BaseAdapter{
    private final Bitmap bgIcon;
    List<AppDetail> apps;
    Context context;
    PackageManager manager;
    private static LayoutInflater inflater=null;
    public GridAdapter(AppsListActivity appsListActivity, List<AppDetail> apps, PackageManager manager) {
        // TODO Auto-generated constructor stub
        this.apps=apps;
        context=appsListActivity;
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.imageviewbg);
        bgIcon = icon.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bgIcon);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
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
        int noOfCols = SharedPrefs.getNumberOfColumns(context);
        if(noOfCols == 0) {
            noOfCols = 4;
        }
        if (convertView == null) {
            LayoutInflater li = inflater;
            v = li.inflate(R.layout.apps_list, null);
        } else {
            v = convertView;
        }
        ImageView imageView=(ImageView) v.findViewById(R.id.item_app_icon);
        TextView textView=(TextView) v.findViewById(R.id.item_app_label);
        LinearLayout.LayoutParams margins = new LinearLayout.LayoutParams(imageView.getLayoutParams());
        margins.topMargin = 80 / noOfCols;
        margins.bottomMargin = 80 / noOfCols;
        margins.leftMargin = noOfCols;
        margins.rightMargin = noOfCols;

        if(noOfCols >= 5 || !SharedPrefs.getShowAppNames(context)) {
            textView.setVisibility(View.GONE);
            imageView.setLayoutParams(margins);
        } else {
            String text = (String) apps.get(position).getLabel();
            textView.setText(text);
        }



//        if(text.length() > 14) {
//            text = text.substring(0,11) + "...";
//        }

        String path = context.getFilesDir().getAbsolutePath();
        Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getLabel());
//        // set foreground
//        if(icon != null) {
//            imageView.setImageDrawable(RoundedBitmapDrawableFactory.create(v.getResources(), icon));//Bitmap.createScaledBitmap(icon, icon.getWidth(), (icon.getHeight() / 6), false)
//        } else {
//            Bitmap immutableBmp= ((BitmapDrawable) apps.get(position).getIcon()).getBitmap();
//            Bitmap mutableBitmap=immutableBmp.copy(Bitmap.Config.ARGB_8888, true);
//            Bitmap iconToSet = BitMapFilter.applyEdgeColors(context.getResources(), mutableBitmap);
//            //app.setIcon(RoundedBitmapDrawableFactory.create(this.getResources(),iconToSet));
//            IconLoader.saveIcon(context, iconToSet, (String) apps.get(position).getLabel());
//            imageView.setImageDrawable(RoundedBitmapDrawableFactory.create(v.getResources(), iconToSet));
//        }
//        Bitmap bitmap = BitmapFactory.decodeResource(v.getResources(), R.mipmap.imageviewbg);
        //imageView.setImageDrawable(apps.get(position).getIcon());
        imageView.setImageDrawable(new BitmapDrawable(v.getResources(), icon));


//        Bitmap bgIcon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getLabel());
//        if(bgIcon != null) {
//            //imageView.setImageDrawable(RoundedBitmapDrawableFactory.create(v.getResources(), icon));//Bitmap.createScaledBitmap(icon, icon.getWidth(), (icon.getHeight() / 6), false)
//        } else {
//            Bitmap immutableBmp= ((BitmapDrawable) apps.get(position).getIcon()).getBitmap();
//            Bitmap mutableBitmap=immutableBmp.copy(Bitmap.Config.ARGB_8888, true);
//            Bitmap iconToSet = BitMapFilter.getShadow(context.getResources(), mutableBitmap);
//            //app.setIcon(RoundedBitmapDrawableFactory.create(this.getResources(),iconToSet));
//            IconLoader.saveIcon(context, iconToSet, (String) apps.get(position).getLabel());
//            //imageView.setImageDrawable(RoundedBitmapDrawableFactory.create(v.getResources(), iconToSet));
//        }
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(v.getResources(), bgIcon);
        roundedBitmapDrawable.setCornerRadius(30f);
        imageView.setBackground(roundedBitmapDrawable);
        // end set
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = manager.getLaunchIntentForPackage(apps.get(position).getName().toString());
                SharedPrefs.increaseNumberOfActivityStarts(apps.get(position).getLabel().toString(), context);
                context.startActivity(i);
            }
        });

        return v;
    }

}

