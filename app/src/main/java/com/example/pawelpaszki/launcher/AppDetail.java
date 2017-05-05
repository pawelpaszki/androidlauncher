package com.example.pawelpaszki.launcher;

/**
 * Created by PawelPaszki on 02/05/2017.
 */

import android.graphics.drawable.Drawable;

public class AppDetail {
    private CharSequence label;
    private CharSequence name;
    private Drawable icon;

    public CharSequence getLabel() {
        return this.label;
    }

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public CharSequence getName() {
        return this.name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
