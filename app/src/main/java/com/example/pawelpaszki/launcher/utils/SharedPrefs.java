package com.example.pawelpaszki.launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by PawelPaszki on 10/05/2017.
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
        editor.commit();
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
        editor.commit();
    }

    public static void setNumberOfColumns(int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "noOfColumns";
        editor.putInt(key, value);
        editor.commit();
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
        editor.commit();
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
        editor.commit();
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
        editor.commit();
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
        editor.commit();
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
        editor.commit();
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
        editor.commit();
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
        editor.commit();
    }

    public static void saveWidgetsIds(Context context, ArrayList<Integer> widgetsIds){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null){
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < widgetsIds.size(); i++) {
                if(i + 1 < widgetsIds.size()) {
                    sb.append(widgetsIds.get(i) + ",");
                } else {
                    sb.append(widgetsIds.get(i));
                }
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("widgetsids", sb.toString());
            editor.commit();
        }
    }

    public static void clearWidgetsIds(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null){
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("widgetsids").commit();
        }
    }

    public static ArrayList<Integer> getWidgetsIds(Context context){
        ArrayList<Integer> widgetsIds = new ArrayList<Integer>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs != null) {
            String concatenatedString = prefs.getString("widgetsids", "");
            if (concatenatedString.length() == 0) {
                return widgetsIds;
            } else {
                String[] ids = concatenatedString.split(",");
                for(int i = 0; i < ids.length; i++) {
                    try {
                        widgetsIds.add(Integer.parseInt(ids[i]));
                    } catch (Exception e) {
                        return new ArrayList<Integer>();
                    }

                }
            }
        }
        return widgetsIds;
    }


}
