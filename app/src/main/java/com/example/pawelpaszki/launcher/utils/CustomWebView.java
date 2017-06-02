package com.example.pawelpaszki.launcher.utils;

/**
 * Created by PawelPaszki on 02/06/2017.
 */

import java.io.File;

        import android.annotation.SuppressLint;
        import android.app.DownloadManager;
        import android.content.Context;
        import android.net.Uri;
        import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;
        import android.webkit.WebViewClient;

public class CustomWebView extends WebViewClient {

    private Context context;

    public CustomWebView(Context context) {
        this.context = context;
    }



    @SuppressLint("NewApi")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.contains(".jpg") || url.contains(".png") | url.contains(".ico")){
            view.loadUrl(url);
            return true;
        }
        return false;
    }

    public String getFileName(String url) {
        String filenameWithoutExtension = "";
        filenameWithoutExtension = String.valueOf(System.currentTimeMillis()
                + ".jpg");
        return filenameWithoutExtension;
    }


}