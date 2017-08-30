package com.pawelpaszki.launcher.layouts;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * Created by PawelPaszki on 23/06/2017.
 */

public class CustomViewGroup extends ViewGroup{

    private boolean mLocked;
    public CustomViewGroup(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.v("customViewGroup", "**********Intercepted");
        return mLocked;
    }

//    public boolean isLocked() {
//        return locked;
//    }
//
//    public void setLocked(boolean locked) {
//        this.locked = locked;
//    }
}