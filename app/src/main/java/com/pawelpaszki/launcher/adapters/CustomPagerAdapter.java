package com.pawelpaszki.launcher.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pawelpaszki.launcher.R;
import com.pawelpaszki.launcher.layouts.Page;

/**
 * Created by PawelPaszki on 27/06/2017.
 */

public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;

    public CustomPagerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        Page page = Page.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.page_layout, collection, false);
        ((ImageView)layout.getChildAt(0)).setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), page.getDrawable()));
        ((TextView)layout.getChildAt(1)).setText(page.getDescription());
        layout.setTag(position);
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return Page.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }




}
