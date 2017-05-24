package com.example.pawelpaszki.launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    public static void setReverseListOrderFlag(int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "reverseList";
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getReverseListOrderFlag(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = "reverseList";
        return prefs.getInt(key, 0);
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
}
