package com.example.pawelpaszki.launcher;

import android.graphics.drawable.Drawable;

/**
 * Created by PawelPaszki on 02/05/2017.
 * Used to keep information about apps
 */

public class AppDetail {
    private CharSequence mLabel;
    private CharSequence mName;
    private Drawable mIcon;
    private int mNumberOfStarts;

    public CharSequence getmLabel() {
        return this.mLabel;
    }

    void setmLabel(CharSequence mLabel) {
        this.mLabel = mLabel;
    }

    public CharSequence getmName() {
        return this.mName;
    }

    void setmName(CharSequence mName) {
        this.mName = mName;
    }

    public Drawable getmIcon() {
        return this.mIcon;
    }

    void setmIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public int getmNumberOfStarts() {
        return mNumberOfStarts;
    }

    void setmNumberOfStarts(int mNumberOfStarts) {
        this.mNumberOfStarts = mNumberOfStarts;
    }
}
