package com.example.pawelpaszki.launcher.utils;

/**
 * Created by PawelPaszki on 02/06/2017.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebView extends WebViewClient {

    private Context context;

    public CustomWebView(Context context) {
        this.context = context;
    }


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