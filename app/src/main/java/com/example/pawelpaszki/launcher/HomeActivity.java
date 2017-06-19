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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.layouts.WidgetFrame;
import com.example.pawelpaszki.launcher.layouts.WidgetInfo;
import com.example.pawelpaszki.launcher.utils.AppsSorter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.MissedCallsCountRetriever;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeActivity extends Activity {

//    private ProgressBar mProgressBar;
//    private TextView mProgressTextView;
    private GestureDetectorCompat mGestureDetector;
    private PackageManager mPackageManager;
    private Context mContext;
    private LinearLayout mDockLayout;
    private RelativeLayout mTopContainer;
    private boolean mTopContainerEnabled = true;
    private FloatingActionButton mAddPage;
    private FloatingActionButton mRemovePage;
    private FloatingActionButton mPinPage;
    private boolean mIsWidgetPinned;
    private int mTopContainerWidth;
    private int mTopContainerHeight;
    private ScrollView mWidgetScrollView;
    private LinearLayout mWidgetContainer;
    private int mSingleScrollHeight;
    private int mStartScrollY;
    private int mEndScrollY;
    private int mCurrentWidgetPage;
    private boolean mWidgetControlsInvisible = true;

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
                    mDockLayout.removeAllViews();
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
                    mDockLayout.removeAllViews();
                    loadCarousel();
                }
            }, 500);
        }
    };
    private WidgetFrame newWidgetPage;
    private ArrayList<Integer> widgetIds = new ArrayList<>();
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
                        mDockLayout.removeAllViews();
                        loadCarousel();
                    }
                }, 500);

            }
        };
        registerReceiver(smsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));
        getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, missedCallObserver);
        Log.i("on start", "on start");
        int unreadMessages = MissedCallsCountRetriever.getUnreadMessagesCount(mContext);
        if(mDockLayout != null) {
            for(int i = 0; i < mDockLayout.getChildCount(); i++) {
                if(mDockLayout.getChildAt(i).getTag().toString().equals("Messaging")) {
                    if(! ((TextView)((FrameLayout)((LinearLayout)(mDockLayout.getChildAt(i))).getChildAt(0)).getChildAt(1)).getText().equals("") && ! (((TextView)((FrameLayout)((LinearLayout)(mDockLayout.getChildAt(i))).getChildAt(0)).getChildAt(1)).getText()== null)) {
                        if( Integer.parseInt(((TextView)((FrameLayout)((LinearLayout)(mDockLayout.getChildAt(i))).getChildAt(0)).getChildAt(1)).getText().toString()) != unreadMessages) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mDockLayout.removeAllViews();
                                    loadCarousel();
                                }
                            }, 500);
                        }
                    }
                }
            }
        }
        if(((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount() > 0) {
            ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().startListening();
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
        if(((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount() > 0) {
            ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().stopListening();
        }
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("oncreate", "home activity created");
        setContentView(R.layout.activity_home);
        mAppWidgetManager = AppWidgetManager.getInstance(this);
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
        mContext = this;


        /////////////// load/ process icons //////////////
        //startIntentService();

        // comment out if loading icons from local storage
        loadCarousel();

        mWidgetScrollView = (ScrollView) findViewById(R.id.widgets_scroll);

        mWidgetScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            if(((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount() > 1) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mStartScrollY = mWidgetScrollView.getScrollY();
                    startScrollX = (int) event.getX();
                    Log.i("start scroll x: ", String.valueOf(startScrollX));
                    Log.i("start scroll: ", String.valueOf(mStartScrollY));
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    int childCount = ((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount();
                    mSingleScrollHeight = mWidgetScrollView.getChildAt(0).getHeight() / childCount;
                    mEndScrollY = mWidgetScrollView.getScrollY();
                    endScrollX = (int) event.getX();
                    Log.i("end scroll x: ", String.valueOf(endScrollX));
                    Log.i("end scroll: ", String.valueOf(mEndScrollY));
                    Log.i("y", String.valueOf(Math.abs(mStartScrollY - mEndScrollY)));
                    Log.i("x", String.valueOf(endScrollX - startScrollX));
                    if(Math.abs(mStartScrollY - mEndScrollY) < 20) {
                        if(endScrollX - startScrollX > 100) {
                            showWidgetControlButtonsOnSwipeRight();
                        } else if (startScrollX - endScrollX > 100){
                            onSwipeLeft();
                        }
                        return true;
                    }
//                    Log.i("scrollview height", String.valueOf(mWidgetScrollView.getChildAt(0).getHeight()));
//                    Log.i("single scroll height y", String.valueOf(mSingleScrollHeight));
//                    Log.i("linear layout height", String.valueOf(mTopContainer.getHeight()));
//                    Log.i("child count", String.valueOf(childCount));
//                    Log.i("single page height", String.valueOf(mTopContainer.getChildAt(0).getHeight()));
                    ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().stopListening();
                    if(mStartScrollY < mEndScrollY) {
                        if(mStartScrollY < mSingleScrollHeight) {

                            mStartScrollY = 1;
                            mCurrentWidgetPage = mStartScrollY;
                        } else {
                            mStartScrollY = mWidgetScrollView.getScrollY() / mSingleScrollHeight + 1;
                            if(mStartScrollY > childCount) {
                                mStartScrollY = mWidgetScrollView.getScrollY() / mSingleScrollHeight;
                                mStartScrollY--;
                                mCurrentWidgetPage = mStartScrollY;

                            }
                        }
                    } else {
                        mStartScrollY = mWidgetScrollView.getScrollY() / mSingleScrollHeight;
                        mCurrentWidgetPage = mStartScrollY;
                    }
                    Log.i("start scroll: ", String.valueOf(mStartScrollY));
                    if(!mIsWidgetPinned) {
                        mWidgetScrollView.postDelayed(new Runnable() {
                            public void run() {
                                mWidgetScrollView.smoothScrollTo(0, mStartScrollY * mSingleScrollHeight);
                            }
                        },100);
                        ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().startListening();
                    }

                    if(!mIsWidgetPinned) {
                        return false;
                    }
                }
                if(mIsWidgetPinned) {
                    mGestureDetector.onTouchEvent(event);
                }
            } else {
                mGestureDetector.onTouchEvent(event);
            }

            return mIsWidgetPinned;
            }
        });

        mWidgetContainer = (LinearLayout) findViewById (R.id.widget_container);

        ///// add saved widgets later //////////

        mAddPage = (FloatingActionButton) findViewById(R.id.add_page_button);
        mAddPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWidgetContainer.getChildCount() <= 10) {
                    Log.i("addPage dimensions", mAddPage.getHeight() + ":" + mAddPage.getWidth());
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    newWidgetPage = (WidgetFrame) inflater.inflate(R.layout.widget_layout, mWidgetContainer, false);
                    newWidgetPage.getLayoutParams().height = mTopContainerHeight;
                    newWidgetPage.getLayoutParams().width = mTopContainerWidth;
                    newWidgetPage.setTag(new Random().nextInt(Integer.MAX_VALUE));
                    newWidgetPage.setAppWidgetHost(new AppWidgetHost(mContext, Integer.parseInt(newWidgetPage.getTag().toString())));
                    onClickSelectWidget();
                    if(((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount() == 0)  {
                        mCurrentWidgetPage = 0;
                    }

                    newWidgetPage.getAppWidgetHost().startListening();

                } else {
                    Toast.makeText(HomeActivity.this,"Only 10 widget pages allowed" ,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mRemovePage = (FloatingActionButton) findViewById(R.id.remove_page_button);
        mRemovePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWidgetContainer.getChildCount() > 0) {
                    int viewToRemoveIndex;
                    if (mWidgetScrollView.getScrollY() == 0) {
                        viewToRemoveIndex = 0;
                    } else {
                        viewToRemoveIndex = mWidgetScrollView.getScrollY() / mSingleScrollHeight;
                    }
                    ((WidgetFrame) mWidgetContainer.getChildAt(viewToRemoveIndex)).getAppWidgetHost().stopListening();
                    ((WidgetFrame) mWidgetContainer.getChildAt(viewToRemoveIndex)).getAppWidgetHost().deleteAppWidgetId(widgetIds.get(viewToRemoveIndex));
                    mWidgetContainer.removeViewAt(viewToRemoveIndex);
                    widgetIds.remove(viewToRemoveIndex);

                    if(mWidgetContainer.getChildCount() == 0) {
                        mRemovePage.setVisibility(View.GONE);
                        SharedPrefs.clearWidgetsIds(mContext);
                    } else {
                        final int scrollTo = viewToRemoveIndex;

                        if(mWidgetContainer.getChildCount() == viewToRemoveIndex) {
                            mWidgetScrollView.postDelayed(new Runnable() {
                                public void run() {
                                    mWidgetScrollView.smoothScrollTo(0, (scrollTo-1) * mSingleScrollHeight);
                                }
                            },20);
                            ((WidgetFrame) mWidgetContainer.getChildAt(scrollTo-1)).getAppWidgetHost().startListening();
                            mCurrentWidgetPage = scrollTo - 1;
                        } else {
                            mCurrentWidgetPage = scrollTo;
                            mWidgetScrollView.postDelayed(new Runnable() {
                                public void run() {
                                    mWidgetScrollView.smoothScrollTo(0, (scrollTo) * mSingleScrollHeight);
                                }
                            },20);
                            ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().startListening();
                        }
                        SharedPrefs.saveWidgetsIds(mContext,widgetIds);
                    }
                    if (mWidgetContainer.getChildCount() == 1) {
                        mPinPage.setVisibility(View.GONE);
                    }
                    if(mWidgetContainer.getChildCount() == 9) {
                        mAddPage.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mPinPage = (FloatingActionButton) findViewById(R.id.pin_page);
        mPinPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsWidgetPinned = !mIsWidgetPinned;
                if(mIsWidgetPinned) {
                    mWidgetScrollView.setVerticalScrollBarEnabled(false);
                    mAddPage.setVisibility(View.GONE);
                    mRemovePage.setVisibility(View.GONE);
                    mPinPage.setImageDrawable(ContextCompat.getDrawable(getmContext(), R.drawable.unlock));
                    mPinPage.setVisibility(View.GONE);
                } else {
                    mWidgetScrollView.setVerticalScrollBarEnabled(true);
                    if( mWidgetContainer.getChildCount() < 10) {
                        mAddPage.setVisibility(View.VISIBLE);
                    }
                    if(mWidgetContainer.getChildCount() > 0) {
                        mRemovePage.setVisibility(View.VISIBLE);
                        mPinPage.setImageDrawable(ContextCompat.getDrawable(getmContext(), R.drawable.lock));
                        mPinPage.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mGestureDetector = new GestureDetectorCompat(this, new MyGestureListener());
        RelativeLayout home = (RelativeLayout) findViewById(R.id.home_container);
        home.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return false;
            }
        });

        home.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
            if(mTopContainerEnabled) {
                mTopContainer.setVisibility(View.INVISIBLE);
                mTopContainer.setEnabled(false);
            } else {
                mTopContainer.setVisibility(View.VISIBLE);
                mTopContainer.setEnabled(true);
            }
            mTopContainerEnabled =!mTopContainerEnabled;
            return false;
            }
        });
        ArrayList<Integer> widgetDetails = SharedPrefs.getWidgetsIds(mContext);
        if(mWidgetContainer.getChildCount() != widgetDetails.size()) {
            loadSavedWidgets(widgetDetails);
        }
        if(mWidgetContainer.getChildCount() == 0) {
            mRemovePage.setVisibility(View.GONE);
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
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            newWidgetPage = (WidgetFrame) inflater.inflate(R.layout.widget_layout, mWidgetContainer, false);
            newWidgetPage.getLayoutParams().height = mTopContainerHeight;
            newWidgetPage.getLayoutParams().width = mTopContainerWidth;
            newWidgetPage.setTag(new Random().nextInt(Integer.MAX_VALUE));
            newWidgetPage.setAppWidgetHost(new AppWidgetHost(mContext, Integer.parseInt(newWidgetPage.getTag().toString())));

            newWidgetPage.getAppWidgetHost().startListening();
            int appWidgetId = ids.get(i);
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            WidgetInfo launcherInfo = new WidgetInfo(appWidgetId);
            launcherInfo.hostView = newWidgetPage.getAppWidgetHost().createView(this, appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setTag(launcherInfo);
            newWidgetPage.setWidgetView(launcherInfo);
            mWidgetContainer.addView(newWidgetPage);
        }
        mCurrentWidgetPage = 0;
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
        SharedPrefs.saveWidgetsIds(mContext, widgetIds);
        mWidgetContainer.addView(newWidgetPage);
        if(mWidgetContainer.getChildCount() == 10) {
            mAddPage.setVisibility(View.GONE);
        }
        if (mWidgetContainer.getChildCount() == 1) {
            mRemovePage.setVisibility(View.VISIBLE);
        }
        if (mWidgetContainer.getChildCount() > 1) {
            mPinPage.setVisibility(View.VISIBLE);
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


    public Context getmContext() {
        return mContext;
    }

    private void loadCarousel() {
        Log.i("carousel loaded", "true");
        mDockLayout = (LinearLayout) findViewById(R.id.dock_list);
        mPackageManager = getPackageManager();
        List<AppDetail> dockerApps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width/5, width/5);
        mTopContainer = (RelativeLayout) findViewById(R.id.top_container);
        final RelativeLayout.LayoutParams topContainerParams = new RelativeLayout.LayoutParams(mTopContainer.getLayoutParams());
        mTopContainerWidth = width;
        mTopContainerHeight = height - width/5 - getSoftButtonsBarHeight() * 2;
        topContainerParams.height = mTopContainerHeight;
        topContainerParams.topMargin = getSoftButtonsBarHeight();
        mTopContainer.setLayoutParams(topContainerParams);

        List<ResolveInfo> availableActivities = mPackageManager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setLabel(ri.loadLabel(mPackageManager));
            app.setName(ri.activityInfo.packageName);
            app.setIcon(ri.activityInfo.loadIcon(mPackageManager));
            app.setNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getLabel().toString(), this));
            if(SharedPrefs.getAppVisible(this, (String) ri.loadLabel(mPackageManager))) {
                dockerApps.add(app);
                //Log.i("no of runs", "label: " + app.getLabel() + ": " + " package name: " + String.valueOf(ri.activityInfo.packageName) + String.valueOf(app.getNumberOfStarts()));
            }
        }
        AppsSorter.sortApps(this, dockerApps, "most used", true);
        int j;
        for(j = 0; j < dockerApps.size(); j++) {
            View view = LayoutInflater.from(this).inflate(R.layout.dock_item,null);
            TextView homeNotifications = (TextView) view.findViewById(R.id.home_notifications);
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
                view.setTag(dockerApps.get(j).getName());
            }

            ImageView iv = (ImageView) view.findViewById(R.id.dock_app_icon);

            final TextView tv = (TextView) view.findViewById(R.id.dock_app_name);
            String path = this.getFilesDir().getAbsolutePath();

            ////////////// load icon from storage ////////////
            //Bitmap icon = IconLoader.loadImageFromStorage(path, (String) dockerApps.get(j).getLabel());
            Bitmap icon = IconLoader.loadImageFromStorage(path, (String) dockerApps.get(j).getLabel());
            if(icon == null) {
                icon = ((BitmapDrawable) dockerApps.get(j).getIcon()).getBitmap();
            }
//            else {
//                // rounded??
//                //icon = RoundBitmapGenerator.getCircleBitmap(icon);
//            }
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
                                intent = mPackageManager.getLaunchIntentForPackage(SharedPrefs.getMessagingPackageName(HomeActivity.this));
                            } else {
                                intent = mPackageManager.getLaunchIntentForPackage(v.getTag().toString());
                            }
                        }
                        // Log.i("name", v.getTag().toString());
                        SharedPrefs.increaseNumberOfActivityStarts(((TextView)v.findViewById(R.id.dock_app_name)).getText().toString(), mContext);
                        SharedPrefs.setHomeReloadRequired(true, HomeActivity.this);

                        if(intent != null) {
                            mContext.startActivity(intent);
                        } else {
                            mContext.startActivity(new Intent(v.getTag().toString()));
                        }
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this,"This application cannot be opened" ,
                                Toast.LENGTH_LONG).show();
                        recreate();
                    }
                }
            });
            mDockLayout.addView(view);

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
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

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
                }
                return true;
            }
            return false;
        }
    }

    private void showWidgetControlButtonsOnSwipeRight() {
        Log.i("widgetcontrolsinvisible", String.valueOf(mWidgetControlsInvisible));
        if(mWidgetControlsInvisible) {
            Log.i("mWidgetContainer", String.valueOf(mWidgetContainer.getChildCount()));
            if(mWidgetContainer.getChildCount() > 0 &&  !mIsWidgetPinned) {
                mRemovePage.setVisibility(View.VISIBLE);
            }
            if(mWidgetContainer.getChildCount() > 1) {
                mPinPage.setVisibility(View.VISIBLE);
            }
            if(mWidgetContainer.getChildCount() < 10 && !mIsWidgetPinned) {
                mAddPage.setVisibility(View.VISIBLE);
            }
        } else {
            mPinPage.setVisibility(View.GONE);
            mAddPage.setVisibility(View.GONE);
            mRemovePage.setVisibility(View.GONE);
        }
        mWidgetControlsInvisible = !mWidgetControlsInvisible;
    }

    private void onSwipeLeft() {
        Intent intent = new Intent(HomeActivity.this, AppsListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        Log.i("onResume","home activity");
        mDockLayout = (LinearLayout) findViewById(R.id.dock_list);
        int dockCount = mDockLayout.getChildCount();
        if(dockCount > 0) {
            ((HorizontalScrollView) mDockLayout.getParent()).scrollTo(0,0);
        }
        if(SharedPrefs.getHomeReloadRequired(this) || (SharedPrefs.getVisibleCount(this) > 0 && mDockLayout.getChildCount() != SharedPrefs.getVisibleCount(this))) {
            SharedPrefs.setHomeReloadRequired(false, this);
            SharedPrefs.setVisibleCount(mDockLayout.getChildCount(), this);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mDockLayout.removeAllViews();
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

    //not used currently
//    public void startIntentService() {
//        MyResultReceiver myResultReceiver = new MyResultReceiver(null);
//        Intent intent = new Intent(this, MyIntentService.class);
//        intent.putExtra("receiver", myResultReceiver);
//        startService(intent);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
//currently not used
//    private class MyResultReceiver extends ResultReceiver {
//
//        public MyResultReceiver(Handler handler) {
//            super(handler);
//        }
//        @Override
//        protected void onReceiveResult(int resultCode, Bundle resultData) {
//            super.onReceiveResult(resultCode, resultData);
//            if (resultCode == 1 && resultData != null) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mProgressBar = (ProgressBar) findViewById(R.id.progress);
//                        mProgressBar.setVisibility(View.VISIBLE);
//                        mProgressTextView = (TextView) findViewById(R.id.progress_text);
//                        mProgressTextView.setVisibility(View.VISIBLE);
//                    }
//                });
//
//            }
//            if (resultCode == 18 && resultData != null) {
//
//                final ArrayList<String> apps = resultData.getStringArrayList("apps");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mProgressBar.setVisibility(View.GONE);
//                        mProgressTextView.setVisibility(View.GONE);
//                        loadCarousel();
//                    }
//                });
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
