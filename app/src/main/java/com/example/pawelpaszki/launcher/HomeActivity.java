package com.example.pawelpaszki.launcher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.pawelpaszki.launcher.services.MyIntentService;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends Activity {

    private Handler handler = new Handler();
    private List<AppDetail> dockerApps;
    private ProgressBar pb;
    private TextView textView;
    private MyResultReceiver myResultReceiver;
    private GestureDetectorCompat detector;
    private PackageManager manager;
    private Context context;
    private int j;
    private HorizontalScrollView container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(getApplicationContext());
        try {
            myWallpaperManager.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.background));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        startIntentService();
        detector = new GestureDetectorCompat(this, new MyGestureListener());
        RelativeLayout home = (RelativeLayout) findViewById(R.id.home_container);
        home.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
        context = this;
    }

    public Context getContext() {
        return context;
    }

    private void loadCarousel() {
        LinearLayout dock = (LinearLayout) findViewById(R.id.dock_list);
        manager = getPackageManager();
        dockerApps = new ArrayList<AppDetail>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setLabel(ri.loadLabel(manager));
            app.setName(ri.activityInfo.packageName);
            app.setIcon(ri.activityInfo.loadIcon(manager));
            app.setNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getLabel().toString(), this));
            if(SharedPrefs.getAppVisible(this, (String) ri.loadLabel(manager))) {
                dockerApps.add(app);
            }
        }
        for(j = 0; j < dockerApps.size(); j++) {
            View view = LayoutInflater.from(this).inflate(R.layout.dock_item,null);
            view.setTag((String) dockerApps.get(j).getName());
            ImageView iv = (ImageView) view.findViewById(R.id.dock_app_icon);
            final TextView tv = (TextView) view.findViewById(R.id.dock_app_name);
            String path = this.getFilesDir().getAbsolutePath();
            Bitmap icon = IconLoader.loadImageFromStorage(path, (String) dockerApps.get(j).getLabel());
            iv.setImageDrawable(new BitmapDrawable(this.getResources(), icon));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = manager.getLaunchIntentForPackage(v.getTag().toString());
                    Log.i("name", v.getTag().toString());
                    SharedPrefs.increaseNumberOfActivityStarts(((TextView)v.findViewById(R.id.dock_app_name)).getText().toString(), context);
                    if(intent != null) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(v.getTag().toString()));
                    }

                }
            });
            dock.addView(view);

        }

        container = (HorizontalScrollView) findViewById(R.id.carousel_container);
        container.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                Log.i("scroll: ", String.valueOf(container.getScrollX()));
                Log.i("ScrollWidth",Integer.toString(container.getChildAt(0).getMeasuredWidth()));
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            if(event1 != null && event2 != null) {
                float diffY = event2.getY() - event1.getY();
                float diffX = event2.getX() - event1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {

                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {

                        } else {

                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    private void onSwipeLeft() {
        Intent i = new Intent(HomeActivity.this, AppsListActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startIntentService() {

        myResultReceiver = new MyResultReceiver(null);

        Intent intent = new Intent(this, MyIntentService.class);
        intent.putExtra("receiver", myResultReceiver);
        startService(intent);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private class MyResultReceiver extends ResultReceiver {

        public MyResultReceiver(Handler handler) {
            super(handler);
        }


        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == 1 && resultData != null) {
                pb = (ProgressBar) findViewById(R.id.progress);
                pb.setVisibility(View.VISIBLE);
                textView = (TextView) findViewById(R.id.progress_text);
                textView.setVisibility(View.VISIBLE);
             }

            Log.i("MyResultreceiver", Thread.currentThread().getName());

            if (resultCode == 18 && resultData != null) {

                final ArrayList<String> apps = resultData.getStringArrayList("apps");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("MyHandler", Thread.currentThread().getName());

                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        loadCarousel();
                    }
                });
            }
        }
    }

    public void showApps(View v){
        //startIntentService();
        Intent i = new Intent(this, AppsListActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

}
