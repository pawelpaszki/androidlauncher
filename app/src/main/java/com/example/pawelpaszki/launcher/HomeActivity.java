package com.example.pawelpaszki.launcher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
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

import com.example.pawelpaszki.launcher.layouts.WidgetFrame;
import com.example.pawelpaszki.launcher.layouts.WidgetInfo;
import com.example.pawelpaszki.launcher.services.MyIntentService;
import com.example.pawelpaszki.launcher.utils.AppsSorter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.MissedCallsCountRetriever;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

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
    private Handler pinPageHandler;
    private int currentWidgetPage;
    private boolean widgetControlsInvisible = true;

    ///////////
    private AppWidgetManager mAppWidgetManager;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPWIDGET   = 9;
    ///////////

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
    private WidgetFrame newWidgetPage;
    private ArrayList<Integer> widgetIds = new ArrayList<Integer>();
    private int startScrollX;
    private int endScrollX;


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
        if(((LinearLayout) scrollView.getChildAt(0)).getChildCount() > 0) {
            ((WidgetFrame) widgetContainer.getChildAt(currentWidgetPage)).getAppWidgetHost().startListening();
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
        if(((LinearLayout) scrollView.getChildAt(0)).getChildCount() > 0) {
            ((WidgetFrame) widgetContainer.getChildAt(currentWidgetPage)).getAppWidgetHost().stopListening();
        }
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("oncreate", "home activity created");
        setContentView(R.layout.activity_home);
        mAppWidgetManager = AppWidgetManager.getInstance(this);
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
                    startScrollX = (int) event.getX();
                    Log.i("start scroll x: ", String.valueOf(startScrollX));
                    Log.i("start scroll: ", String.valueOf(startScrollY));
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    int childCount = ((LinearLayout) scrollView.getChildAt(0)).getChildCount();
                    singleScrollHeight = scrollView.getChildAt(0).getHeight() / childCount;
                    endScrollY = scrollView.getScrollY();
                    endScrollX = (int) event.getX();
                    Log.i("end scroll x: ", String.valueOf(endScrollX));
                    Log.i("end scroll: ", String.valueOf(endScrollY));
                    Log.i("y", String.valueOf(Math.abs(startScrollY-endScrollY)));
                    Log.i("x", String.valueOf(endScrollX - startScrollX));
                    if(Math.abs(startScrollY-endScrollY) < 20) {
                        if(endScrollX - startScrollX > 100) {
                            showWidgetControlButtonsOnSwipeRight();
                        } else if (startScrollX - endScrollX > 100){
                            onSwipeLeft();
                        }
                        return true;
                    }
//                    Log.i("scrollview height", String.valueOf(scrollView.getChildAt(0).getHeight()));
//                    Log.i("single scroll height y", String.valueOf(singleScrollHeight));
//                    Log.i("linear layout height", String.valueOf(topContainer.getHeight()));
//                    Log.i("child count", String.valueOf(childCount));
//                    Log.i("single page height", String.valueOf(topContainer.getChildAt(0).getHeight()));
                    ((WidgetFrame) widgetContainer.getChildAt(currentWidgetPage)).getAppWidgetHost().stopListening();
                    if(startScrollY < endScrollY) {
                        if(startScrollY < singleScrollHeight) {

                            startScrollY = 1;
                            currentWidgetPage = startScrollY;
                        } else {
                            startScrollY = scrollView.getScrollY() / singleScrollHeight + 1;
                            if(startScrollY > childCount) {
                                startScrollY = scrollView.getScrollY() / singleScrollHeight;
                                startScrollY--;
                                currentWidgetPage = startScrollY;

                            }
                        }
                    } else {
                        startScrollY = scrollView.getScrollY() / singleScrollHeight;
                        currentWidgetPage = startScrollY;
                    }
                    Log.i("start scroll: ", String.valueOf(startScrollY));
                    if(!isWidgetPinned) {
                        scrollView.postDelayed(new Runnable() {
                            public void run() {
                                scrollView.smoothScrollTo(0, startScrollY * singleScrollHeight);
                            }
                        },100);
                        ((WidgetFrame) widgetContainer.getChildAt(currentWidgetPage)).getAppWidgetHost().startListening();
                    }

                    if(!isWidgetPinned) {
                        return false;
                    }
                }
                if(isWidgetPinned) {
//                    pinPage.setVisibility(View.VISIBLE);
//                    if(pinPageHandler != null) {
//                        pinPageHandler.removeCallbacksAndMessages(null);
//                    }
//                    pinPageHandler.postDelayed(new Runnable()
//                    {
//                        @Override
//                        public void run()
//                        {
//                            if(isWidgetPinned) {
//                                pinPage.setVisibility(View.GONE);
//                            }
//                        }
//                    }, 3000);
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

        ///// add saved widgets later //////////

        addPage = (FloatingActionButton) findViewById(R.id.add_page_button);
        addPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(widgetContainer.getChildCount() <= 10) {
                    Log.i("addPage dimensions", addPage.getHeight() + ":" + addPage.getWidth());
                    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    newWidgetPage = (WidgetFrame) inflater.inflate(R.layout.widget_layout, widgetContainer, false);
                    newWidgetPage.getLayoutParams().height = topContainerHeight;
                    newWidgetPage.getLayoutParams().width = topContainerWidth;
                    newWidgetPage.setTag(new Random().nextInt(Integer.MAX_VALUE));
                    newWidgetPage.setAppWidgetHost(new AppWidgetHost(context, Integer.parseInt(newWidgetPage.getTag().toString())));
                    onClickSelectWidget();
                    if(((LinearLayout) scrollView.getChildAt(0)).getChildCount() == 0)  {
                        currentWidgetPage = 0;
                    }

                    newWidgetPage.getAppWidgetHost().startListening();

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
                    ((WidgetFrame) widgetContainer.getChildAt(viewToRemoveIndex)).getAppWidgetHost().stopListening();
                    widgetContainer.removeViewAt(viewToRemoveIndex);
                    widgetIds.remove(viewToRemoveIndex);

                    if(widgetContainer.getChildCount() == 0) {
                        removePage.setVisibility(View.GONE);
                        SharedPrefs.clearWidgetsIds(context);
                    } else {
                        final int scrollTo = viewToRemoveIndex;

                        if(widgetContainer.getChildCount() == viewToRemoveIndex) {
                            scrollView.postDelayed(new Runnable() {
                                public void run() {
                                    scrollView.smoothScrollTo(0, (scrollTo-1) * singleScrollHeight);
                                }
                            },20);
                            ((WidgetFrame) widgetContainer.getChildAt(scrollTo-1)).getAppWidgetHost().startListening();
                            currentWidgetPage = scrollTo - 1;
                        } else {
                            currentWidgetPage = scrollTo;
                            scrollView.postDelayed(new Runnable() {
                                public void run() {
                                    scrollView.smoothScrollTo(0, (scrollTo) * singleScrollHeight);
                                }
                            },20);
                            ((WidgetFrame) widgetContainer.getChildAt(currentWidgetPage)).getAppWidgetHost().startListening();
                        }
                        SharedPrefs.saveWidgetsIds(context,widgetIds);
                    }
                    if (widgetContainer.getChildCount() == 1) {
                        pinPage.setVisibility(View.GONE);
                    }
                    if(widgetContainer.getChildCount() == 9) {
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
                    scrollView.setVerticalScrollBarEnabled(false);
                    addPage.setVisibility(View.GONE);
                    removePage.setVisibility(View.GONE);
                    pinPage.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.unlock));
                    pinPage.setVisibility(View.GONE);
                } else {
                    scrollView.setVerticalScrollBarEnabled(true);
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
            } else {
                topContainer.setVisibility(View.VISIBLE);
                topContainer.setEnabled(true);
            }
            topContainerEnabled =!topContainerEnabled;
            return false;
            }
        });
        ArrayList<Integer> widgetDetails = SharedPrefs.getWidgetsIds(context);
        if(widgetContainer.getChildCount() != widgetDetails.size()) {
            loadSavedWidgets(widgetDetails);
        }
        if(widgetContainer.getChildCount() == 0) {
            removePage.setVisibility(View.GONE);
        }
    }

    private void onClickSelectWidget() {
        int appWidgetId = newWidgetPage.getAppWidgetHost().allocateAppWidgetId();

        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            //TODO do nothing??
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_APPWIDGET:
                    addAppWidget(data);
                    break;
                case REQUEST_CREATE_APPWIDGET:
                    completeAddAppWidget(data);
                    break;
            }
        }
    }

    private void loadSavedWidgets(ArrayList<Integer> ids) {
        widgetIds = ids;
        for(int i = 0; i < ids.size(); i++) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            newWidgetPage = (WidgetFrame) inflater.inflate(R.layout.widget_layout, widgetContainer, false);
            newWidgetPage.getLayoutParams().height = topContainerHeight;
            newWidgetPage.getLayoutParams().width = topContainerWidth;
            newWidgetPage.setTag(new Random().nextInt(Integer.MAX_VALUE));
            newWidgetPage.setAppWidgetHost(new AppWidgetHost(context, Integer.parseInt(newWidgetPage.getTag().toString())));

            newWidgetPage.getAppWidgetHost().startListening();
            int appWidgetId = ids.get(i);
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            WidgetInfo launcherInfo = new WidgetInfo(appWidgetId);
            launcherInfo.hostView = newWidgetPage.getAppWidgetHost().createView(this, appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setTag(launcherInfo);
            newWidgetPage.setWidgetView(launcherInfo);
            widgetContainer.addView(newWidgetPage);
        }
        currentWidgetPage = 0;
    }

    private void completeAddAppWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        Log.i("widget id", String.valueOf(appWidgetId));

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        WidgetInfo launcherInfo = new WidgetInfo(appWidgetId);
        launcherInfo.hostView = newWidgetPage.getAppWidgetHost().createView(this, appWidgetId, appWidgetInfo);
        launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
        launcherInfo.hostView.setTag(launcherInfo);
        newWidgetPage.setWidgetView(launcherInfo);
        widgetIds.add(appWidgetId);
        SharedPrefs.saveWidgetsIds(context, widgetIds);
        widgetContainer.addView(newWidgetPage);
        if(widgetContainer.getChildCount() == 10) {
            addPage.setVisibility(View.GONE);
        }
        if (widgetContainer.getChildCount() == 1) {
            removePage.setVisibility(View.VISIBLE);
        }
        if (widgetContainer.getChildCount() > 1) {
            pinPage.setVisibility(View.VISIBLE);
        }
    }

    private void addAppWidget(Intent data) {
        // TODO: catch bad widget exception when sent
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        if (appWidget.configure != null) {
            // Launch over to configure widget, if needed
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidget.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
            onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
        }
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
                            showWidgetControlButtonsOnSwipeRight();
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

    private void showWidgetControlButtonsOnSwipeRight() {
        Log.i("widgetcontrolsinvisible", String.valueOf(widgetControlsInvisible));
        if(widgetControlsInvisible) {
            Log.i("widgetContainer", String.valueOf(widgetContainer.getChildCount()));
            if(widgetContainer.getChildCount() > 0 &&  !isWidgetPinned) {
                removePage.setVisibility(View.VISIBLE);
            }
            if(widgetContainer.getChildCount() > 1) {
                pinPage.setVisibility(View.VISIBLE);
            }
            if(widgetContainer.getChildCount() < 10 && !isWidgetPinned) {
                addPage.setVisibility(View.VISIBLE);
            }
        } else {
            pinPage.setVisibility(View.GONE);
            addPage.setVisibility(View.GONE);
            removePage.setVisibility(View.GONE);
        }
        widgetControlsInvisible = !widgetControlsInvisible;
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
            if (resultCode == 18 && resultData != null) {

                final ArrayList<String> apps = resultData.getStringArrayList("apps");
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
