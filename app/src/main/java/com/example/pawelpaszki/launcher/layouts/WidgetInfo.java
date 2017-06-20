package com.example.pawelpaszki.launcher.layouts;

import android.appwidget.AppWidgetHostView;
import android.widget.FrameLayout;

/**
 * Created by PawelPaszki on 16/06/2017.
 */

public class WidgetInfo {

    public AppWidgetHostView hostView;
    private final int appWidgetId;

    public WidgetInfo(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

}
