package com.example.pawelpaszki.launcher.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pawelpaszki.launcher.utils.PageObject;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by PawelPaszki on 09/06/2017.
 */

public class WidgetPagerAdapter extends PagerAdapter {

    private Context mContext;
    public WidgetPagerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        final PageObject modelObject = PageObject.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final ViewGroup layout = (ViewGroup) inflater.inflate(modelObject.getLayoutResId(), collection, false);
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int value = Integer.parseInt(((TextView) layout.getChildAt(0)).getText().toString());
                ((TextView) layout.getChildAt(0)).setText(String.valueOf(value + 1));
                return false;
            }
        });
        ((TextView) layout.getChildAt(0)).setText(String.valueOf(modelObject.getTitleResId()));
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return PageObject.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        PageObject customPagerEnum = PageObject.values()[position];

        return mContext.getString(customPagerEnum.getTitleResId());
    }

}
