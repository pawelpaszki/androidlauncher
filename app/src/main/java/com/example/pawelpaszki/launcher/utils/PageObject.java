package com.example.pawelpaszki.launcher.utils;

import com.example.pawelpaszki.launcher.R;

/**
 * Created by PawelPaszki on 12/06/2017.
 */

public enum PageObject {

    RED(1, R.layout.widget_page),
    BLUE(2, R.layout.widget_page),
    GREEN(3, R.layout.widget_page);

    private int mTitleResId;
    private int mLayoutResId;

    PageObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }
}
