package com.example.pawelpaszki.launcher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.CallLog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.services.MyIntentService;
import com.example.pawelpaszki.launcher.utils.AppsSorter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;
import com.example.pawelpaszki.launcher.utils.MissedCallsCountRetriever;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private RelativeLayout topContainer;
    private boolean topContainerEnabled = true;
    private int i = 0;
    private FloatingActionButton addPage;
    private FloatingActionButton removePage;
    private FloatingActionButton pinPage;
    private boolean isWidgetPinned;
    private int topContainerWidth;
    private int topContainerHeight;
    private ScrollView scrollView;
    private LinearLayout widgetContainer;
    private int singleScrollHeight;
    private int startScrollY;
    private int endScrollY;

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
            }, 500);

        }
    };
    private ContentObserver missedCallObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dock.removeAllViews();
                    loadCarousel();
                }
            }, 500);
        }
    };
    private Handler pinPageHandler;

    @Override
    protected void onStart() {
        smsReceiver = new BroadcastReceiver() {
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
                }, 500);

            }
        };
        registerReceiver(smsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));
        getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, missedCallObserver);
        Log.i("on start", "on start");
        int unreadMessages = MissedCallsCountRetriever.getUnreadMessagesCount(context);
        if(dock != null) {
            for(int i = 0; i < dock.getChildCount(); i++) {
                if(dock.getChildAt(i).getTag().toString().equals("Messaging")) {
                    if(! ((TextView)((FrameLayout)((LinearLayout)(dock.getChildAt(i))).getChildAt(0)).getChildAt(1)).getText().equals("") && ! (((TextView)((FrameLayout)((LinearLayout)(dock.getChildAt(i))).getChildAt(0)).getChildAt(1)).getText()== null)) {
                        if( Integer.parseInt(((TextView)((FrameLayout)((LinearLayout)(dock.getChildAt(i))).getChildAt(0)).getChildAt(1)).getText().toString()) != unreadMessages) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dock.removeAllViews();
                                    loadCarousel();
                                }
                            }, 500);
                        }
                    }
                }
            }
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(smsReceiver != null) {
            unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
        if(missedCallObserver != null) {
            getApplicationContext().getContentResolver().unregisterContentObserver(missedCallObserver);
        }
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        pinPageHandler = new Handler();
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
                e.printStackTrace();
            }
            SharedPrefs.setIsFirstLaunch(false,this);
        }
        context = this;


        /////////////// load/ process icons //////////////
        //startIntentService();

        // comment out if loading icons from local storage
        loadCarousel();

        scrollView = (ScrollView) findViewById(R.id.widgets_scroll);

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if(((LinearLayout) scrollView.getChildAt(0)).getChildCount() > 1) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startScrollY = scrollView.getScrollY();
//                    Log.i("start scroll: ", String.valueOf(startScrollY));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    int childCount = ((LinearLayout) scrollView.getChildAt(0)).getChildCount();
                    singleScrollHeight = scrollView.getChildAt(0).getHeight() / childCount;
                    endScrollY = scrollView.getScrollY();
//                    Log.i("end scroll: ", String.valueOf(endScrollY));
//                    Log.i("scrollview height", String.valueOf(scrollView.getChildAt(0).getHeight()));
//                    Log.i("scrollview y", String.valueOf(scrollView.getScrollY()));
//                    Log.i("single scroll height y", String.valueOf(singleScrollHeight));
//                    Log.i("linear layout height", String.valueOf(topContainer.getHeight()));
//                    Log.i("single page height", String.valueOf(topContainer.getChildAt(0).getHeight()));
                    if(startScrollY < endScrollY) {
                        if(startScrollY < singleScrollHeight) {
                            startScrollY = 1;
                        } else {
                            startScrollY = scrollView.getScrollY() / singleScrollHeight + 1;
                            if(startScrollY > childCount) {
                                startScrollY--;
                            }
                        }
                    } else {
                        startScrollY = scrollView.getScrollY() / singleScrollHeight;
                    }
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.smoothScrollTo(0, startScrollY * singleScrollHeight);
                        }
                    });
                    if(!isWidgetPinned) {
                        return true;
                    }
                }
                if(isWidgetPinned) {
                    pinPage.setVisibility(View.VISIBLE);
                    if(pinPageHandler != null) {
                        pinPageHandler.removeCallbacksAndMessages(null);
                    }
                    pinPageHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(isWidgetPinned) {
                                pinPage.setVisibility(View.GONE);
                            }
                        }
                    }, 3000);
                    detector.onTouchEvent(event);
                }
            } else {
                detector.onTouchEvent(event);
            }

            return isWidgetPinned;
            }
        });

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        widgetContainer = (LinearLayout) findViewById (R.id.widget_container);

//        final FrameLayout widgetPage = (FrameLayout) inflater.inflate(R.layout.widget_layout, widgetContainer, false);
//        widgetPage.getLayoutParams().height = topContainerHeight;
//        widgetPage.getLayoutParams().width = topContainerWidth;
//        widgetPage.setTag(new Random().nextLong());
//        TextView testTV = new TextView(this);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        String currentDateandTime = "textview created at: " + sdf.format(new Date());
//
//        testTV.setText(currentDateandTime);
//        testTV.setTextSize(30);
//        testTV.setLayoutParams(new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT));
//        widgetPage.addView(testTV);
//        widgetContainer.addView(widgetPage);

        ///// add saved widgets later //////////

        addPage = (FloatingActionButton) findViewById(R.id.add_page_button);
        addPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(widgetContainer.getChildCount() <= 10) {
                    Log.i("addPage dimensions", addPage.getHeight() + ":" + addPage.getWidth());
                    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    FrameLayout widgetPage = (FrameLayout) inflater.inflate(R.layout.widget_layout, widgetContainer, false);
                    widgetPage.getLayoutParams().height = topContainerHeight;
                    widgetPage.getLayoutParams().width = topContainerWidth;
                    widgetPage.setTag(new Random().nextLong());
                    TextView testTV = new TextView(context);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String currentDateandTime = "textview created at: " + sdf.format(new Date());

                    testTV.setText(currentDateandTime);
                    testTV.setTextSize(30);
                    testTV.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    widgetPage.addView(testTV);
                    widgetContainer.addView(widgetPage);
                    if(widgetContainer.getChildCount() == 10) {
                        addPage.setVisibility(View.GONE);
                    }
                    if (widgetContainer.getChildCount() == 1) {
                        removePage.setVisibility(View.VISIBLE);
                    }
                    if (widgetContainer.getChildCount() > 1) {
                        pinPage.setVisibility(View.VISIBLE);
                    }

                } else {
                    Toast.makeText(HomeActivity.this,"Only 10 widget pages allowed" ,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        removePage = (FloatingActionButton) findViewById(R.id.remove_page_button);
        removePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(widgetContainer.getChildCount() > 0) {
                    int viewToRemoveIndex;
                    if (scrollView.getScrollY() == 0) {
                        viewToRemoveIndex = 0;
                    } else {
                        viewToRemoveIndex = scrollView.getScrollY() / singleScrollHeight;
                    }

                    widgetContainer.removeViewAt(viewToRemoveIndex);
                    if (widgetContainer.getChildCount() == 1) {
                        pinPage.setVisibility(View.GONE);
                    } else if( widgetContainer.getChildCount() == 0) {
                        removePage.setVisibility(View.GONE);
                    } else if(widgetContainer.getChildCount() == 9) {
                        addPage.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        pinPage = (FloatingActionButton) findViewById(R.id.pin_page);
        pinPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isWidgetPinned = !isWidgetPinned;
                if(isWidgetPinned) {
                    addPage.setVisibility(View.GONE);
                    removePage.setVisibility(View.GONE);
                    pinPage.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.unlock));
                    pinPage.setVisibility(View.GONE);
                } else {
                    if( widgetContainer.getChildCount() < 10) {
                        addPage.setVisibility(View.VISIBLE);
                    }
                    if(widgetContainer.getChildCount() > 0) {
                        removePage.setVisibility(View.VISIBLE);
                        pinPage.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.lock));
                        pinPage.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

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
                topContainer.setVisibility(View.INVISIBLE);
                topContainer.setEnabled(false);
                //topContainer.setBackgroundColor(0x00000000);
            } else {
                //topContainer.setBackgroundColor(0xffffffff);
                topContainer.setVisibility(View.VISIBLE);
                topContainer.setEnabled(true);
                //topContainer.setBackgroundColor(0xFF000000);

            }
            topContainerEnabled =!topContainerEnabled;
            return false;
            }
        });
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
        topContainer = (RelativeLayout) findViewById(R.id.top_container);
        final RelativeLayout.LayoutParams topContainerParams = new RelativeLayout.LayoutParams(topContainer.getLayoutParams());
        topContainerWidth = width;
        topContainerHeight = height - width/5 - getSoftButtonsBarHeight() * 2;
        topContainerParams.height = topContainerHeight;
        topContainerParams.topMargin = getSoftButtonsBarHeight();
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
