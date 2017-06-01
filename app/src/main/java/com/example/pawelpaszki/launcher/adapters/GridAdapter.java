package com.example.pawelpaszki.launcher.adapters;

/**
 * Created by PawelPaszki on 03/05/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.AppDetail;
import com.example.pawelpaszki.launcher.AppsListActivity;
import com.example.pawelpaszki.launcher.R;
import com.example.pawelpaszki.launcher.utils.BitMapFilter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.MissedCallsCountRetriever;
import com.example.pawelpaszki.launcher.utils.RoundBitmapGenerator;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.util.List;

public class GridAdapter extends BaseAdapter{
    private final Bitmap bgIcon;
    private int iconSide;
    List<AppDetail> apps;
    Context context;
    PackageManager manager;
    private static LayoutInflater inflater=null;
    public GridAdapter(AppsListActivity appsListActivity, List<AppDetail> apps, PackageManager manager, int iconSide) {
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
        this.iconSide = iconSide;
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
        FrameLayout.LayoutParams margins = new FrameLayout.LayoutParams(imageView.getLayoutParams());
        margins.topMargin = 80 / noOfCols;
        margins.bottomMargin = 80 / noOfCols;
        margins.leftMargin = noOfCols;
        margins.rightMargin = noOfCols;
        String text = (String) apps.get(position).getLabel();
        if(noOfCols >= 5 || !SharedPrefs.getShowAppNames(context)) {
            textView.setVisibility(View.GONE);
            imageView.setLayoutParams(margins);
            textView.setLayoutParams(margins);
        } else {
            if(noOfCols <= 3) {
                imageView.setLayoutParams(margins);
            }

            textView.setText(text);
        }
        TextView messagesCount = (TextView) v.findViewById(R.id.notifications);
        if(text.equalsIgnoreCase("Messaging")) {
            int messageCount = MissedCallsCountRetriever.getUnreadMessagesCount(context);
            if(messageCount > 0) {
                messagesCount.setText(String.valueOf(messageCount));
                messagesCount.setVisibility(View.VISIBLE);
            } else {
                messagesCount.setVisibility(View.GONE);
            }
        } else {
            messagesCount.setVisibility(View.GONE);
        }
//        if(text.length() > 14) {
//            text = text.substring(0,11) + "...";
//        }

        String path = context.getFilesDir().getAbsolutePath();
        //Bitmap icon  = ((BitmapDrawable) apps.get(position).getIcon()).getBitmap();
        /////////////// load from storage /////////////
        Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getLabel());
        if(icon == null) {
            icon  = ((BitmapDrawable) apps.get(position).getIcon()).getBitmap();
        } else {
            icon = RoundBitmapGenerator.getCircleBitmap(icon);
        }
        if(icon.getWidth() != iconSide || icon.getHeight() != iconSide) {
            icon = Bitmap.createScaledBitmap(icon, iconSide, iconSide, false);
        }
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
        float fontSize = 11f;
        if(noOfCols <=3) {
            float ratio = 0f;
            switch(noOfCols) {
                case 1:
                    ratio = 3f;
                    fontSize = 24f;
                    break;
                case 2:
                    ratio = 1.6f;
                    fontSize = 18f;
                    break;
                case 3:
                    ratio = 1.3f;
                    fontSize = 14f;
                    break;
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,fontSize);
            messagesCount.setTextSize(TypedValue.COMPLEX_UNIT_SP,fontSize);
            imageView.setImageDrawable(new BitmapDrawable(v.getResources(), Bitmap.createScaledBitmap(icon, (int) (icon.getWidth() * ratio), (int) (icon.getHeight() * ratio), true)));
        } else {
            imageView.setImageDrawable(new BitmapDrawable(v.getResources(), icon));
        }




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
                Intent i;
                if(apps.get(position).getLabel().toString().equalsIgnoreCase("Phone")) {
                    i = new Intent(Intent.ACTION_DIAL);
                } else {
                    i = manager.getLaunchIntentForPackage(apps.get(position).getName().toString());
                }
                SharedPrefs.increaseNumberOfActivityStarts(apps.get(position).getLabel().toString(), context);
                SharedPrefs.setHomeReloadRequired(true, context);
                context.startActivity(i);
            }
        });

        return v;
    }

}

