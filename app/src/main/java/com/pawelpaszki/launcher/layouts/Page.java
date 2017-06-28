package com.pawelpaszki.launcher.layouts;

import com.pawelpaszki.launcher.R;

/**
 * Created by PawelPaszki on 27/06/2017.
 */

public enum Page {

    FIRST(R.drawable.init_one, "Swipe from left to right to show widget controls and settings button."),
    SECOND(R.drawable.init_two, ""),
    THIRD(R.drawable.init_three, "Swipe left or right to access all visible applications from bottom drawer"),
    FOURTH(R.drawable.init_four, ""),
    FIFTH(R.drawable.init_five, "Swipe from right to left to see grid view of all your visible applications"),
    SIXTH(R.drawable.init_six, ""),
    SEVENTH(R.drawable.init_seven, "Press settings button from home view to customize your launcher"),
    EIGHTH(R.drawable.init_eight, "");
    private int mDrawable;
    private String mDescription;

    Page(int drawableIndex, String description) {
        mDrawable = drawableIndex;
        mDescription = description;
    }

    public int getDrawable() {
        return mDrawable;
    }

    public String getDescription() {
        return mDescription;
    }
}
