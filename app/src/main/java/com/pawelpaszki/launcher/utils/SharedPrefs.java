package com.pawelpaszki.launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Created by PawelPaszki on 10/05/2017.
 * Last edited on 27/06/2017
 */

public class SharedPrefs {

    public static String getSortingMethod(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("sortingMethod", "name");
    }

    public static void setSortingMethod(Context context, String sortValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("sortingMethod", sortValue);
        editor.apply();
    }

    public static int getNumberOfActivityStarts(String name, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = name + "_starts";
        return prefs.getInt(key, 0);
    }

    public static void increaseNumberOfActivityStarts(String name, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = name + "_starts";
        editor.putInt(key, (getNumberOfActivityStarts(name, context)) + 1);
        editor.apply();
    }

    public static void setNumberOfColumns(int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "noOfColumns";
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getNumberOfColumns(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "noOfColumns";
        return prefs.getInt(key, 0);
    }

    public static void setShowAppNames(boolean value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "showAppNames";
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getShowAppNames(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "showAppNames";
        return prefs.getBoolean(key, true);
    }

    public static void setAppVisible(boolean visible, String name, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "visible" + name;
        editor.putBoolean(key, visible);
        editor.apply();
    }

    public static boolean getAppVisible(Context context, String name) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "visible" + name;
        return prefs.getBoolean(key, true);
    }

    public static void setIsFirstLaunch(boolean firstLaunch, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "firstLaunch";
        editor.putBoolean(key, firstLaunch);
        editor.apply();
    }

    public static boolean getIsFirstLaunch(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "firstLaunch";
        return prefs.getBoolean(key, true);
    }

    public static void setHomeReloadRequired(boolean homeReloadRequired, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "homeReloadRequired";
        editor.putBoolean(key, homeReloadRequired);
        editor.apply();
    }

    public static boolean getHomeReloadRequired(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "homeReloadRequired";
        return prefs.getBoolean(key, false);
    }

    public static void setVisibleCount(int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "visibleCount";
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getVisibleCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "visibleCount";
        return prefs.getInt(key, 0);
    }

    public static void setNonDefaultIconsCount(int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "nonDefault";
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getNonDefaultIconsCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "nonDefault";
        return prefs.getInt(key, 0);
    }

    public static String getMessagingPackageName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("messaging", "");
    }

    public static void setMessagingPackageName(Context context, String name) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("messaging", name);
        editor.apply();
    }

    public static void saveWidgetsIds(Context context, ArrayList<Integer> widgetsIds){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null){
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < widgetsIds.size(); i++) {
                if(i + 1 < widgetsIds.size()) {
                    sb.append(widgetsIds.get(i)).append(",");
                } else {
                    sb.append(widgetsIds.get(i));
                }
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("widgetsids", sb.toString());
            editor.apply();
        }
    }

    public static void clearWidgetsIds(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null){
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("widgetsids").apply();
        }
    }

    public static ArrayList<Integer> getWidgetsIds(Context context){
        ArrayList<Integer> widgetsIds = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            String concatenatedString = prefs.getString("widgetsids", "");
            if (concatenatedString.length() == 0) {
                return widgetsIds;
            } else {
                String[] ids = concatenatedString.split(",");
                for (String id : ids) {
                    try {
                        widgetsIds.add(Integer.parseInt(id));
                    } catch (Exception e) {
                        return new ArrayList<>();
                    }

                }
            }
        }
        return widgetsIds;
    }

    public static void setCurrentWidgetPage(Context context, int pageNumber) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "currentWidgetPage";
        editor.putInt(key, pageNumber);
        editor.apply();
    }

    public static int getCurrentWidgetPage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "currentWidgetPage";
        return prefs.getInt(key, 0);
    }

    public static void setWidgetPinned(boolean isWidgetPinned, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "widgetPinned";
        editor.putBoolean(key, isWidgetPinned);
        editor.apply();
    }

    public static boolean getWidgetPinned(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "widgetPinned";
        return prefs.getBoolean(key, false);
    }

    public static void setNumberOfApps(int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "noOfApps";
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getNumberOfApps(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "noOfApps";
        return prefs.getInt(key, 0);
    }

    public static void setWidgetHeight(Context context, int height, int widgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "widgetheight" + widgetId;
        editor.putInt(key, height);
        editor.apply();
    }

    public static int getWidgetHeight(Context context, int widgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "widgetheight" + widgetId;
        return prefs.getInt(key, 400);
    }

    public static void setWidgetWidth(Context context, int width, int widgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "widgetwidth" + widgetId;
        editor.putInt(key, width);
        editor.apply();
    }

    public static int getWidgetWidth(Context context, int widgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "widgetwidth" + widgetId;
        return prefs.getInt(key, 600);
    }

    public static void setControlsInVisible(boolean controlsInVisible, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "controlsInVisible";
        editor.putBoolean(key, controlsInVisible);
        editor.apply();
    }

    public static boolean getControlsVisible(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "controlsInVisible";
        return prefs.getBoolean(key, false);
    }

    public static void setSafeModeOn(boolean isSafeModeOn, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "safeModeOn";
        editor.putBoolean(key, isSafeModeOn);
        editor.apply();
    }

    public static boolean getSafeModeOn(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "safeModeOn";
        return prefs.getBoolean(key, false);
    }

    public static String getPassphrase(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("passphrase", "");
    }

    public static void setPasshrase(Context context, String sortValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("passphrase", sortValue);
        editor.apply();
    }

    public static void setHomeRecreateRequired(boolean homeRecreateRequired, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "homeRecreateRequired";
        editor.putBoolean(key, homeRecreateRequired);
        editor.apply();
    }

    public static boolean getHomeRecreateRequired(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "homeRecreateRequired";
        return prefs.getBoolean(key, false);
    }

    public static void saveScreenWidth(Context context, int width) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "screenwidth";
        editor.putInt(key, width);
        editor.apply();
    }

    public static int getScreenWidth(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "screenwidth";
        return prefs.getInt(key, 0);
    }
}
