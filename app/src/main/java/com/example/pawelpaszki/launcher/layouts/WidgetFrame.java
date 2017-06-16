package com.example.pawelpaszki.launcher.layouts;

import android.appwidget.AppWidgetHost;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Created by PawelPaszki on 16/06/2017.
 */

public class WidgetFrame extends FrameLayout {
    private String TAG;

    private AppWidgetHost appWidgetHost;
    public WidgetFrame(@NonNull Context context) {
        super(context);
    }

    public WidgetFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WidgetFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setWidgetView(WidgetInfo launcherInfo) {
        if (launcherInfo.hostView == null) {
            return;
        }
        removeAllViews();
        addView(launcherInfo.hostView);

    }

    public AppWidgetHost getAppWidgetHost() {
        return appWidgetHost;
    }

    public void setAppWidgetHost(AppWidgetHost appWidgetHost) {
        this.appWidgetHost = appWidgetHost;
    }
}
