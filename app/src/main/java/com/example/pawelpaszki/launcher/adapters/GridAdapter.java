package com.example.pawelpaszki.launcher.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.MissedCallsCountRetriever;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.util.List;

/**
 * Created by PawelPaszki on 03/05/2017.
 * Last edited on 19/06/2017
 */

public class GridAdapter extends BaseAdapter{
    private final Bitmap mBgIcon;
    private int mIconSide;
    private List<AppDetail> mApps;
    private Context mContext;
    private PackageManager mPackageManager;
    private LinearLayout mUninstallPackage;
    private static LayoutInflater sInflater =null;
    public GridAdapter(AppsListActivity appsListActivity, List<AppDetail> mApps, PackageManager mPackageManager, int mIconSide, LinearLayout mUninstallPackage) {
        // TODO Auto-generated constructor stub
        this.mApps = mApps;
        mContext =appsListActivity;
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.imageviewbg);
        mBgIcon = icon.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mBgIcon);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.mPackageManager = mPackageManager;
        sInflater = ( LayoutInflater ) mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mIconSide = mIconSide;
        this.mUninstallPackage = mUninstallPackage;
    }



    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public Object getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v;
        int noOfCols = SharedPrefs.getNumberOfColumns(mContext);
        if(noOfCols == 0) {
            noOfCols = 4;
        }
        if (convertView == null) {
            LayoutInflater li = sInflater;
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
        String text = (String) mApps.get(position).getLabel();
        if(noOfCols >= 5 || !SharedPrefs.getShowAppNames(mContext)) {
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
            v.setTag("Messaging");
            int messageCount = MissedCallsCountRetriever.getUnreadMessagesCount(mContext);
            if(messageCount > 0) {
                messagesCount.setText(String.valueOf(messageCount));
                messagesCount.setVisibility(View.VISIBLE);
            } else {
                messagesCount.setVisibility(View.GONE);
            }
        } else if (text.equalsIgnoreCase("Phone")) {
            v.setTag("Phone");
            if(MissedCallsCountRetriever.getMissedCallsCount(mContext) > 0) {
                messagesCount.setVisibility(View.VISIBLE);
                messagesCount.setText(MissedCallsCountRetriever.getMissedCallsCount(mContext));
            } else {
                messagesCount.setVisibility(View.GONE);
            }
        } else {
            messagesCount.setVisibility(View.GONE);
            v.setTag(mApps.get(position).getName());
        }

        String path = mContext.getFilesDir().getAbsolutePath();
        //Bitmap icon  = ((BitmapDrawable) mApps.get(position).getIcon()).getBitmap();
        /////////////// load from storage /////////////
        Bitmap icon = IconLoader.loadImageFromStorage(path, (String) mApps.get(position).getLabel());
        if(icon == null) {
            icon  = ((BitmapDrawable) mApps.get(position).getIcon()).getBitmap();
        }
//        else {
            // rounded??
            //icon = RoundBitmapGenerator.getCircleBitmap(icon);
//        }
        if(icon.getWidth() != mIconSide || icon.getHeight() != mIconSide) {
            icon = Bitmap.createScaledBitmap(icon, mIconSide, mIconSide, false);
        }
//        // set foreground
//        if(icon != null) {
//            imageView.setImageDrawable(RoundedBitmapDrawableFactory.create(v.getResources(), icon));//Bitmap.createScaledBitmap(icon, icon.getWidth(), (icon.getHeight() / 6), false)
//        } else {
//            Bitmap immutableBmp= ((BitmapDrawable) mApps.get(position).getIcon()).getBitmap();
//            Bitmap mutableBitmap=immutableBmp.copy(Bitmap.Config.ARGB_8888, true);
//            Bitmap iconToSet = BitMapFilter.applyEdgeColors(mContext.getResources(), mutableBitmap);
//            //app.setIcon(RoundedBitmapDrawableFactory.create(this.getResources(),iconToSet));
//            IconLoader.saveIcon(mContext, iconToSet, (String) mApps.get(position).getLabel());
//            imageView.setImageDrawable(RoundedBitmapDrawableFactory.create(v.getResources(), iconToSet));
//        }
//        Bitmap bitmap = BitmapFactory.decodeResource(v.getResources(), R.mipmap.imageviewbg);
        //imageView.setImageDrawable(mApps.get(position).getIcon());
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




//        Bitmap mBgIcon = IconLoader.loadImageFromStorage(path, (String) mApps.get(position).getLabel());
//        if(mBgIcon != null) {
//            //imageView.setImageDrawable(RoundedBitmapDrawableFactory.create(v.getResources(), icon));//Bitmap.createScaledBitmap(icon, icon.getWidth(), (icon.getHeight() / 6), false)
//        } else {
//            Bitmap immutableBmp= ((BitmapDrawable) mApps.get(position).getIcon()).getBitmap();
//            Bitmap mutableBitmap=immutableBmp.copy(Bitmap.Config.ARGB_8888, true);
//            Bitmap iconToSet = BitMapFilter.getShadow(mContext.getResources(), mutableBitmap);
//            //app.setIcon(RoundedBitmapDrawableFactory.create(this.getResources(),iconToSet));
//            IconLoader.saveIcon(mContext, iconToSet, (String) mApps.get(position).getLabel());
//            //imageView.setImageDrawable(RoundedBitmapDrawableFactory.create(v.getResources(), iconToSet));
//        }
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(v.getResources(), mBgIcon);
        roundedBitmapDrawable.setCornerRadius(30f);
        imageView.setBackground(roundedBitmapDrawable);
        // end set
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent i;
                    if (mApps.get(position).getLabel().toString().equalsIgnoreCase("Phone")) {
                        i = new Intent(Intent.ACTION_DIAL);
                    } else {
                        i = mPackageManager.getLaunchIntentForPackage(mApps.get(position).getName().toString());
                    }
                    SharedPrefs.increaseNumberOfActivityStarts(mApps.get(position).getLabel().toString(), mContext);
                    SharedPrefs.setHomeReloadRequired(true, mContext);
                    mContext.startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(mContext,"This application cannot be opened" ,
                            Toast.LENGTH_LONG).show();
                    ((AppsListActivity) mContext).recreate();
                }
            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mUninstallPackage.setVisibility(View.VISIBLE);
                ((AppsListActivity) mContext).highlightView(v.getTag().toString());
                return false;
            }
        });

        return v;
    }

}

