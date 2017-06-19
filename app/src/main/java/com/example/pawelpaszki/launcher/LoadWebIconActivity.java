package com.example.pawelpaszki.launcher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.utils.CustomWebView;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.ImageDownloader;
import com.example.pawelpaszki.launcher.utils.MissedCallsCountRetriever;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.R.attr.src;

public class LoadWebIconActivity extends AppCompatActivity {

    private WebView webview;
    private String appLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_web_icon);
        appLabel = getIntent().getExtras().getString("label");
//        Window window = getWindow();
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(Color.TRANSPARENT);
//        window.setNavigationBarColor(Color.TRANSPARENT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.select_web_icon_toolbar);
        toolbar.setTitle("Set Web Icon");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        webview = (WebView) findViewById(R.id.load_icon_web_view);
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) webview
                .getLayoutParams();

        layoutParams.setMargins(0, actionBarHeight, 0, 0);

        webview.setLayoutParams(layoutParams);
        webview.setWebViewClient(new CustomWebView());
        webview.loadUrl("https://images.google.com/");
        webview.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if(event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    WebView webView = (WebView) v;

                    switch(keyCode)
                    {
                        case KeyEvent.KEYCODE_BACK:
                            if(webView.canGoBack())
                            {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }

                return false;
            }
        });
        webview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i("long press", String.valueOf(webview.getHitTestResult().getType()));
                if(webview.getHitTestResult().getType() == 5) {
                    Log.i("extra", webview.getHitTestResult().getExtra());
                        try {
                            new ImageDownloader(LoadWebIconActivity.this, appLabel).execute(webview.getHitTestResult().getExtra());
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    goBack();
                                }
                            }, 500);



                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                } else if (webview.getHitTestResult().getType() == 8) {
                    Toast.makeText(LoadWebIconActivity.this, "Can't select this image. Please try different one",
                            Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.i("about to download", url);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            goBack();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void goBack() {
        Intent intent = new Intent(LoadWebIconActivity.this,ChangeIconsActivity.class);

        intent.putExtra("option", "web");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }
}
