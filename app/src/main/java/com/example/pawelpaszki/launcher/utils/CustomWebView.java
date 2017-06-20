package com.example.pawelpaszki.launcher.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by PawelPaszki on 02/06/2017.
 * Last edited on 19/06/2017
 */

public class CustomWebView extends WebViewClient {

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.contains(".jpg") || url.contains(".png") | url.contains(".ico")){
            view.loadUrl(url);
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url=request.getUrl().toString();
        if(url.contains(".jpg") || url.contains(".png") | url.contains(".ico")){
            view.loadUrl(url);
            return true;
        }
        return false;
    }

}