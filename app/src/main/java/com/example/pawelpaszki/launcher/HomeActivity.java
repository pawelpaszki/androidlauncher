package com.example.pawelpaszki.launcher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.services.MyIntentService;
import com.example.pawelpaszki.launcher.utils.AppsSorter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.RoundBitmapGenerator;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;
import com.example.pawelpaszki.launcher.utils.MissedCallsCountRetriever;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private TextView homeNotifications;
    private LinearLayout dock;
    private LinearLayout topContainer;
    private boolean topContainerEnabled;
    private int i = 0;
    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("message received", "msg");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dock.removeAllViews();
                    loadCarousel();
                }
            }, 1000);

        }
    };
    private AppWidgetHost appWidgetHost;


    @Override
    protected void onStart() {
        registerReceiver(smsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(smsReceiver != null) {
            unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        boolean firstLaunch = SharedPrefs.getIsFirstLaunch(this);
        if(firstLaunch) {
            WallpaperManager myWallpaperManager
                    = WallpaperManager.getInstance(getApplicationContext());
            try {
                myWallpaperManager.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.background));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            SharedPrefs.setIsFirstLaunch(false,this);
        }


        /////////////// load/ process icons //////////////
        //startIntentService();

        // comment out if loading icons from local storage
        loadCarousel();

        detector = new GestureDetectorCompat(this, new MyGestureListener());
        RelativeLayout home = (RelativeLayout) findViewById(R.id.home_container);
        home.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });

        home.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(topContainerEnabled) {
                    topContainer.setVisibility(View.GONE);
                    topContainer.setBackgroundColor(0x00000000);
                } else {
                    //topContainer.setBackgroundColor(0xffffffff);
                    topContainer.setVisibility(View.VISIBLE);
                    topContainer.setBackgroundColor(0xFF000000);

                }
                topContainerEnabled =!topContainerEnabled;
                return false;
            }
        });

        context = this;
    }

    public Context getContext() {
        return context;
    }

    private void loadCarousel() {
        Log.i("carousel loaded", "true");
        dock = (LinearLayout) findViewById(R.id.dock_list);
        manager = getPackageManager();
        dockerApps = new ArrayList<AppDetail>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;



        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width/5, width/5);
        topContainer = (LinearLayout) findViewById(R.id.top_home_container);
        final RelativeLayout.LayoutParams topContainerParams = new RelativeLayout.LayoutParams(topContainer.getLayoutParams());
        topContainerParams.height = height - width/5 - getSoftButtonsBarHeight();
        topContainer.setLayoutParams(topContainerParams);


        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setLabel(ri.loadLabel(manager));
            app.setName(ri.activityInfo.packageName);
            app.setIcon(ri.activityInfo.loadIcon(manager));
            app.setNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getLabel().toString(), this));
            if(SharedPrefs.getAppVisible(this, (String) ri.loadLabel(manager))) {
                dockerApps.add(app);
                //Log.i("no of runs", "label: " + app.getLabel() + ": " + " package name: " + String.valueOf(ri.activityInfo.packageName) + String.valueOf(app.getNumberOfStarts()));
            }
        }
        AppsSorter.sortApps(this, dockerApps, "most used", true);
        for(j = 0; j < dockerApps.size(); j++) {

            View view = LayoutInflater.from(this).inflate(R.layout.dock_item,null);
            homeNotifications = (TextView) view.findViewById(R.id.home_notifications);
            if(dockerApps.get(j).getLabel().toString().equalsIgnoreCase("Messaging")) {
                SharedPrefs.setMessagingPackageName(this, (String) dockerApps.get(j).getName());
                view.setTag("Messaging");
                int messageCount = MissedCallsCountRetriever.getUnreadMessagesCount(this);
                if(messageCount > 0) {
                    homeNotifications.setText(String.valueOf(messageCount));
                    homeNotifications.setVisibility(View.VISIBLE);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) homeNotifications.getLayoutParams();
                    params.rightMargin = width / 36;
                    params.bottomMargin = width / 36;
                } else {
                    homeNotifications.setVisibility(View.GONE);
                }
            } else if(dockerApps.get(j).getLabel().toString().equalsIgnoreCase("Phone")) {
                view.setTag("Phone");
                if(MissedCallsCountRetriever.getMissedCallsCount(this) > 0) {
                    homeNotifications.setVisibility(View.VISIBLE);
                    homeNotifications.setText(MissedCallsCountRetriever.getMissedCallsCount(this));
                } else {
                    homeNotifications.setVisibility(View.GONE);
                }
            } else {
                view.setTag((String) dockerApps.get(j).getName());
            }

            ImageView iv = (ImageView) view.findViewById(R.id.dock_app_icon);

            final TextView tv = (TextView) view.findViewById(R.id.dock_app_name);
            String path = this.getFilesDir().getAbsolutePath();

            ////////////// load icon from storage ////////////
            //Bitmap icon = IconLoader.loadImageFromStorage(path, (String) dockerApps.get(j).getLabel());
            Bitmap icon = IconLoader.loadImageFromStorage(path, (String) dockerApps.get(j).getLabel());
            if(icon == null) {
                icon  = ((BitmapDrawable) dockerApps.get(j).getIcon()).getBitmap();
            } else {
                // rounded??
                //icon = RoundBitmapGenerator.getCircleBitmap(icon);
            }
            iv.setImageDrawable(new BitmapDrawable(this.getResources(), icon));
            iv.setLayoutParams(layoutParams);
            tv.setText(dockerApps.get(j).getLabel());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent;
                        if(v.getTag().toString().equalsIgnoreCase("Phone")) {
                            intent = new Intent(Intent.ACTION_DIAL);
                        } else {
                            if(v.getTag().toString().equals("Messaging")) {
                                intent = manager.getLaunchIntentForPackage(SharedPrefs.getMessagingPackageName(HomeActivity.this));
                            } else {
                                intent = manager.getLaunchIntentForPackage(v.getTag().toString());
                            }
                        }

                        // Log.i("name", v.getTag().toString());
                        SharedPrefs.increaseNumberOfActivityStarts(((TextView)v.findViewById(R.id.dock_app_name)).getText().toString(), context);
                        SharedPrefs.setHomeReloadRequired(true, HomeActivity.this);

                        if(intent != null) {
                            context.startActivity(intent);
                        } else {
                            context.startActivity(new Intent(v.getTag().toString()));
                        }
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this,"This application cannot be opened" ,
                        Toast.LENGTH_LONG).show();
                        recreate();
                    }


                }
            });
            dock.addView(view);

        }
    }

    private int getSoftButtonsBarHeight() {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
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
        Intent intent = new Intent(HomeActivity.this, AppsListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }


    @Override
    protected void onResume() {
        Log.i("onResume","home activity");
        dock = (LinearLayout) findViewById(R.id.dock_list);
        int dockCount = dock.getChildCount();
        if(dockCount > 0) {
            ((HorizontalScrollView) dock.getParent()).scrollTo(0,0);
        }
        //Log.i("carousel items", "carousel items: " + dock.getChildCount());

        if(SharedPrefs.getHomeReloadRequired(this) || (SharedPrefs.getVisibleCount(this) > 0 && dock.getChildCount() != SharedPrefs.getVisibleCount(this))) {
            SharedPrefs.setHomeReloadRequired(false, this);
            SharedPrefs.setVisibleCount(dock.getChildCount(), this);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    dock.removeAllViews();
                    loadCarousel();
                }
            }, 1);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb = (ProgressBar) findViewById(R.id.progress);
                        pb.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.progress_text);
                        textView.setVisibility(View.VISIBLE);
                    }
                });

             }

            //Log.i("MyResultreceiver", Thread.currentThread().getName());

            if (resultCode == 18 && resultData != null) {

                final ArrayList<String> apps = resultData.getStringArrayList("apps");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Log.i("MyHandler", Thread.currentThread().getName());

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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

}
