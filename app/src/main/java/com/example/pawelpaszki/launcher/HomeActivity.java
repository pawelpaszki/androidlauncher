package com.example.pawelpaszki.launcher;

import android.animation.ObjectAnimator;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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

import static com.example.pawelpaszki.launcher.utils.SharedPrefs.getCurrentWidgetPage;

/**
 * Last Edited by PawelPaszki on 19/06/2017.
 */

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
    private boolean mAllScrollsDisabled = false;

    ///////////
    private AppWidgetManager mAppWidgetManager;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPWIDGET   = 9;
    ///////////

    private BroadcastReceiver mPackageUpdateReceiver = new BroadcastReceiver() {
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
            }, 50);
        }
    };

    private BroadcastReceiver mSmsReceiver = new BroadcastReceiver() {
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
    private RelativeLayout.LayoutParams mResizeRightLayoutParams;
    private RelativeLayout.LayoutParams mResizeDownLayoutParams;
    private String mResizeButtonTag;
    private int mResizeStartX;
    private int mResizeStartY;
    private int mCurrentWidgetMinHeight;
    private int mCurrentWidgetMinWidth;
    private Button mResizeDown;
    private Button mResizeRight;
    private int mCurrentResizeDownLeftMargin;
    private int mCurrentResizeDownTopMargin;
    private int mCurrentResizeRightLeftMargin;
    private int mCurrentResizeRightTopMargin;
    private WidgetInfo launcherInfo;
    private int mCurrentWidgetWidth;
    private int mCurrentWidgetHeight;
    private FloatingActionButton mGoToSettings;
    private FloatingActionButton mHideControls;
    private ArrayList<FloatingActionButton> mControls = new ArrayList<>();

    @Override
    protected void onStart() {
        mSmsReceiver = new BroadcastReceiver() {
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
        registerReceiver(mSmsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));
        getApplicationContext().getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, missedCallObserver);
        Log.i("on start", "on start");

        mPackageUpdateReceiver = new BroadcastReceiver() {
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
                }, 50);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        registerReceiver(mPackageUpdateReceiver, intentFilter);

        int unreadMessages = MissedCallsCountRetriever.getUnreadMessagesCount(mContext);
        if(mDockLayout != null) {
            for(int i = 0; i < mDockLayout.getChildCount(); i++) {
                if(mDockLayout.getChildAt(i).getTag().toString().equals("Messaging")) {
                    int unreadMsgCount = 0;
                    String unreadMessagesCount = ((TextView)((FrameLayout)((LinearLayout)(mDockLayout.getChildAt(i))).getChildAt(0)).getChildAt(1)).getText().toString();
                    try {
                        unreadMsgCount = Integer.parseInt(unreadMessagesCount);
                    } catch (Exception ignored) {

                    }
                    if( unreadMsgCount != unreadMessages) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDockLayout.removeAllViews();
                                loadCarousel();
                            }
                        }, 50);
                    }
                }
            }
        }
        if(((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount() > 0) {
            mCurrentWidgetPage = SharedPrefs.getCurrentWidgetPage(mContext);
            mWidgetScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mSingleScrollHeight = mWidgetScrollView.getHeight();
                    mWidgetScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.i("mSingleScrollHeight", String.valueOf(mSingleScrollHeight));
                    mWidgetScrollView.postDelayed(new Runnable() {
                        public void run() {
                            mWidgetScrollView.smoothScrollTo(0, mCurrentWidgetPage * mSingleScrollHeight);
                            Log.i("current page 235", String.valueOf(mCurrentWidgetPage));
                        }
                    },10);
                    ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().startListening();
                }
            });

        }
        super.onStart();
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

        mIsWidgetPinned = SharedPrefs.getWidgetPinned(mContext);

        /////////////// load/ process icons //////////////
        //startIntentService();

        // comment out if loading icons from local storage
        loadCarousel();

        mWidgetScrollView = (ScrollView) findViewById(R.id.widgets_scroll);

        mWidgetScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(!mAllScrollsDisabled) {
                    Log.i("controls invisible", String.valueOf(mWidgetControlsInvisible));
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
                            if(Math.abs(mStartScrollY - mEndScrollY) < 50) {
                                if(endScrollX - startScrollX > 100) {
                                    if(mWidgetControlsInvisible) {
                                        mWidgetControlsInvisible = false;
                                        showWidgetControlButtonsOnSwipeRight();
                                    }
                                } else if (startScrollX - endScrollX > 400 || (mIsWidgetPinned && startScrollX - endScrollX > 100)){
                                    onSwipeLeft();
                                }
                                return true;
                            }
                            ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().stopListening();
                            if(mStartScrollY < mEndScrollY) {
                                if(mStartScrollY < mSingleScrollHeight) {

                                    mStartScrollY = 1;
                                    mCurrentWidgetPage = mStartScrollY;
                                } else {
                                    mStartScrollY = mWidgetScrollView.getScrollY() / mSingleScrollHeight + 1;
                                    if(mStartScrollY > childCount) {
                                        mStartScrollY = mWidgetScrollView.getScrollY() / mSingleScrollHeight;
                                        //mStartScrollY--;
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
                                        Log.i("current page 331", String.valueOf(mStartScrollY));
                                    }
                                },10);
                                ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().startListening();
                                SharedPrefs.setCurrentWidgetPage(mContext,mCurrentWidgetPage);
                                return true;
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
                }
            return mIsWidgetPinned || mAllScrollsDisabled;
            }
        });

        mWidgetContainer = (LinearLayout) findViewById (R.id.widget_container);

        ///// add saved widgets later //////////
        mGoToSettings = (FloatingActionButton) findViewById(R.id.go_to_settings);
        mGoToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, SettingsActivity.class);
                startActivity(i);
                //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });

        mHideControls = (FloatingActionButton) findViewById(R.id.hide_controls);

        mHideControls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWidgetControlsInvisible = true;
                showWidgetControlButtonsOnSwipeRight();
            }
        });

        mAddPage = (FloatingActionButton) findViewById(R.id.add_page_button);
        mAddPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWidgetContainer.getChildCount() <= 10) {
//            int height = appWidgetInfo.minHeight;
//            int width = appWidgetInfo.minWidth;
//            final float scale = mContext.getResources().getDisplayMetrics().density;
//            int dpHeight = (int) (height * scale + 0.5f);
//            int dpWidth = (int) (width * scale + 0.5f);
//            Log.i("height", height + ":" + dpHeight);
//            Log.i("width", width + ":" + dpWidth);
//            launcherInfo.hostView.setLayoutParams(new FrameLayout.LayoutParams(dpWidth,dpHeight));
                    Log.i("addPage dimensions", mAddPage.getHeight() + ":" + mAddPage.getWidth());
                    Log.i("container width", String.valueOf(mWidgetContainer.getWidth()));
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    newWidgetPage = (WidgetFrame) inflater.inflate(R.layout.widget_layout, mWidgetContainer, false);
//                    newWidgetPage.getLayoutParams().height = mTopContainerHeight;
//                    newWidgetPage.getLayoutParams().width = mTopContainerWidth;
                    newWidgetPage.setTag(new Random().nextInt(Integer.MAX_VALUE));
                    newWidgetPage.setAppWidgetHost(new AppWidgetHost(mContext, Integer.parseInt(newWidgetPage.getTag().toString())));
                    onClickSelectWidget();
                    if(((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount() == 0)  {
                        mCurrentWidgetPage = 0;
                    }

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
                        viewToRemoveIndex = (mWidgetScrollView.getScrollY() + 2) / mSingleScrollHeight;
                    }
                    ((WidgetFrame) mWidgetContainer.getChildAt(viewToRemoveIndex)).getAppWidgetHost().stopListening();
                    ((WidgetFrame) mWidgetContainer.getChildAt(viewToRemoveIndex)).getAppWidgetHost().deleteAppWidgetId(widgetIds.get(viewToRemoveIndex));
                    Log.i("removed index 404", String.valueOf(viewToRemoveIndex));
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
                                    Log.i("current page 419", String.valueOf(scrollTo-1));
                                }
                            },10);
                            ((WidgetFrame) mWidgetContainer.getChildAt(scrollTo-1)).getAppWidgetHost().startListening();
                            mCurrentWidgetPage = scrollTo - 1;
                        } else {
                            mCurrentWidgetPage = scrollTo;
                            mWidgetScrollView.postDelayed(new Runnable() {
                                public void run() {
                                    mWidgetScrollView.smoothScrollTo(0, scrollTo * mSingleScrollHeight);
                                    Log.i("current page 428", String.valueOf(scrollTo));
                                }
                            },10);
                            ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().startListening();
                        }
                        SharedPrefs.setCurrentWidgetPage(mContext,mCurrentWidgetPage);
                        SharedPrefs.saveWidgetsIds(mContext,widgetIds);
                    }
                    if (mWidgetContainer.getChildCount() == 1) {
                        mPinPage.setEnabled(false);
                    }
                    if(mWidgetContainer.getChildCount() == 9) {
                        mAddPage.setEnabled(true);
                    }
                }
            }
        });

        mPinPage = (FloatingActionButton) findViewById(R.id.pin_page);
        mPinPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsWidgetPinned = !mIsWidgetPinned;
                SharedPrefs.setWidgetPinned(mIsWidgetPinned, mContext);
                if(mIsWidgetPinned) {
                    mWidgetScrollView.setVerticalScrollBarEnabled(false);
                    mAddPage.setEnabled(false);
                    mRemovePage.setEnabled(false);
                    mPinPage.setImageDrawable(ContextCompat.getDrawable(getmContext(), R.drawable.unlock));
                } else {
                    mWidgetScrollView.setVerticalScrollBarEnabled(true);
                    if( mWidgetContainer.getChildCount() < 10) {
                        mAddPage.setEnabled(true);
                    }
                    if(mWidgetContainer.getChildCount() > 0) {
                        mRemovePage.setEnabled(true);
                        mPinPage.setImageDrawable(ContextCompat.getDrawable(getmContext(), R.drawable.lock));
                    }
                }
            }
        });
        mControls.add(mAddPage);
        mControls.add(mRemovePage);
        mControls.add(mPinPage);
        mControls.add(mGoToSettings);
        mControls.add(mHideControls);

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
            newWidgetPage.setTag(new Random().nextInt(Integer.MAX_VALUE));
            newWidgetPage.setAppWidgetHost(new AppWidgetHost(mContext, Integer.parseInt(newWidgetPage.getTag().toString())));

            int appWidgetId = ids.get(i);
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

            WidgetInfo launcherInfo = new WidgetInfo(appWidgetId);
            launcherInfo.hostView = newWidgetPage.getAppWidgetHost().createView(this, appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            launcherInfo.hostView.setTag(launcherInfo);
            mCurrentWidgetWidth = SharedPrefs.getWidgetWidth(mContext, appWidgetId);
            mCurrentWidgetHeight = SharedPrefs.getWidgetHeight(mContext, appWidgetId);
            launcherInfo.hostView.setLayoutParams(new FrameLayout.LayoutParams(mCurrentWidgetWidth,mCurrentWidgetHeight));
            FrameLayout.LayoutParams widgetParams = (FrameLayout.LayoutParams) launcherInfo.hostView.getLayoutParams();
            widgetParams.setMargins((mTopContainerWidth - mCurrentWidgetWidth) / 2, (mTopContainerHeight - mCurrentWidgetHeight) / 2, (mTopContainerWidth - mCurrentWidgetWidth) / 2, (mTopContainerHeight - mCurrentWidgetHeight) / 2);
            launcherInfo.hostView.setLayoutParams(widgetParams);
            newWidgetPage.setWidgetView(launcherInfo);

            mWidgetContainer.addView(newWidgetPage);
            Log.i("top container height", String.valueOf(mTopContainerHeight));
            Log.i("widget dimensions", launcherInfo.hostView.getWidth() + ":" + launcherInfo.hostView.getHeight());
        }
        mCurrentWidgetPage = getCurrentWidgetPage(mContext);
        if(mCurrentWidgetPage != 0) {
            mSingleScrollHeight = mWidgetScrollView.getChildAt(0).getHeight();
            mWidgetScrollView.postDelayed(new Runnable() {
                public void run() {
                    mWidgetScrollView.smoothScrollTo(0, mCurrentWidgetPage * mSingleScrollHeight);
                    Log.i("current widget 559", String.valueOf(mCurrentWidgetPage));
                }
            },10);
        }
        ((WidgetFrame) mWidgetContainer.getChildAt(mCurrentWidgetPage)).getAppWidgetHost().startListening();
    }

    private void completeAddAppWidget(Intent data) {
        Bundle extras = data.getExtras();
        final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        Log.i("widget id", String.valueOf(appWidgetId));

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        launcherInfo = new WidgetInfo(appWidgetId);
        launcherInfo.hostView = newWidgetPage.getAppWidgetHost().createView(this, appWidgetId, appWidgetInfo);
        launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
        launcherInfo.hostView.setTag(launcherInfo);
        launcherInfo.hostView.setLayoutParams(new FrameLayout.LayoutParams(appWidgetInfo.minWidth, appWidgetInfo.minHeight));

        newWidgetPage.setWidgetView(launcherInfo);
        widgetIds.add(appWidgetId);
        SharedPrefs.saveWidgetsIds(mContext, widgetIds);
        mWidgetContainer.addView(newWidgetPage);
        mWidgetScrollView.post(new Runnable() {
            public void run() {
                mWidgetScrollView.smoothScrollTo(0, (mWidgetContainer.getChildCount() -1) * mSingleScrollHeight);
                Log.i("current page 586", String.valueOf((mWidgetContainer.getChildCount() -1)));
            }
        });

        mCurrentWidgetPage = mWidgetContainer.getChildCount() - 1;

        SharedPrefs.setCurrentWidgetPage(mContext, mCurrentWidgetPage);
        newWidgetPage.getAppWidgetHost().startListening();

        mResizeDown = (Button) findViewById(R.id.resize_widget_down);
        mResizeDown.setVisibility(View.VISIBLE);

        mResizeRight = (Button) findViewById(R.id.resize_widget_right);
        mResizeRight.setVisibility(View.VISIBLE);

        mCurrentWidgetMinHeight = appWidgetInfo.minHeight;
        mCurrentWidgetMinWidth = appWidgetInfo.minWidth;
        mCurrentWidgetHeight = appWidgetInfo.minHeight;
        mCurrentWidgetWidth = appWidgetInfo.minWidth;

        mResizeDownLayoutParams = new RelativeLayout.LayoutParams(mResizeDown.getLayoutParams());
        mResizeDownLayoutParams.height = 60;
        mResizeDownLayoutParams.width = 60;
        mCurrentResizeDownTopMargin = mCurrentWidgetMinHeight - 30;
        mResizeDownLayoutParams.topMargin = mCurrentResizeDownTopMargin;
        mCurrentResizeDownLeftMargin = mCurrentWidgetMinWidth / 2 - 30;
        mResizeDownLayoutParams.leftMargin = mCurrentResizeDownLeftMargin;
        mResizeDown.setLayoutParams(mResizeDownLayoutParams);
        mResizeDown.setOnTouchListener(new MyTouchListener());

        mResizeRightLayoutParams = new RelativeLayout.LayoutParams(mResizeRight.getLayoutParams());
        mResizeRightLayoutParams.height = 60;
        mResizeRightLayoutParams.width = 60;
        mCurrentResizeRightLeftMargin = mCurrentWidgetMinWidth - 30;
        mCurrentResizeRightTopMargin = mCurrentWidgetMinHeight / 2 - 30;
        mResizeRightLayoutParams.topMargin = mCurrentResizeRightTopMargin;
        mResizeRightLayoutParams.leftMargin = mCurrentResizeRightLeftMargin;
        mResizeRight.setLayoutParams(mResizeRightLayoutParams);
        mResizeRight.setOnTouchListener(new MyTouchListener());
        mAddPage.setVisibility(View.GONE);
        mRemovePage.setVisibility(View.GONE);
        mPinPage.setVisibility(View.GONE);
        mAllScrollsDisabled = true;
        final FloatingActionButton addWidget = (FloatingActionButton) findViewById(R.id.complete_add_widget);
        if(mWidgetContainer.getChildCount() > 0) {
            FrameLayout.LayoutParams widgetParams = (FrameLayout.LayoutParams) launcherInfo.hostView.getLayoutParams();
            widgetParams.setMargins(0, 0, 0, mTopContainerHeight - mCurrentWidgetMinHeight);
            launcherInfo.hostView.setLayoutParams(widgetParams);
            mWidgetScrollView.post(new Runnable() {
                public void run() {
                    mWidgetScrollView.smoothScrollTo(0, (mWidgetContainer.getChildCount() -1)* mTopContainerHeight);
                    Log.i("current page 635", String.valueOf(mWidgetContainer.getChildCount() - 1));
                }
            });
        }
        addWidget.setVisibility(View.VISIBLE);
        addWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout.LayoutParams widgetParams = (FrameLayout.LayoutParams) launcherInfo.hostView.getLayoutParams();
                if(mCurrentWidgetHeight % 2 == 1) {
                    mCurrentWidgetHeight++;
                }
                if(mCurrentWidgetWidth % 2 == 1) {
                    mCurrentWidgetWidth++;
                }
                widgetParams.setMargins((mTopContainerWidth - mCurrentWidgetWidth) / 2, (mTopContainerHeight - mCurrentWidgetHeight) / 2, (mTopContainerWidth - mCurrentWidgetWidth) / 2, (mTopContainerHeight - mCurrentWidgetHeight) / 2);
                launcherInfo.hostView.setLayoutParams(widgetParams);
                addWidget.setVisibility(View.GONE);
                mResizeDown.setVisibility(View.GONE);
                mResizeRight.setVisibility(View.GONE);
                if(mWidgetContainer.getChildCount() < 10) {
                    mAddPage.setEnabled(true);
                } else {
                    mAddPage.setEnabled(false);
                }
                mRemovePage.setEnabled(true);
                if (mWidgetContainer.getChildCount() > 1) {
                    mPinPage.setEnabled(true);
                }
                SharedPrefs.setWidgetHeight(mContext, mCurrentWidgetHeight, appWidgetId);
                SharedPrefs.setWidgetWidth(mContext, mCurrentWidgetWidth, appWidgetId);
                mAllScrollsDisabled = false;
            }
        });
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    mResizeButtonTag = view.getTag().toString();
                    mResizeStartX = (int) (view.getX() - motionEvent.getRawX());
                    mResizeStartY = (int) (view.getY() - motionEvent.getRawY());
                    break;

                case MotionEvent.ACTION_MOVE:
                    switch (mResizeButtonTag) {
                        case "resize_down":
                            if (motionEvent.getRawY() + mResizeStartY > mCurrentWidgetMinHeight - 30 && motionEvent.getRawY() + mResizeStartY + 30 < mTopContainerHeight ) {
                                mCurrentResizeDownTopMargin = (int) motionEvent.getRawY() + mResizeStartY - 30;
                                mCurrentResizeRightTopMargin = mCurrentResizeDownTopMargin / 2 - 15;
                            }
                            break;
                        case "resize_right":
                            if (motionEvent.getRawX() + mResizeStartX > mCurrentWidgetMinWidth - 30 && motionEvent.getRawX() + mResizeStartX + 30 < mTopContainerWidth ) {
                                mCurrentResizeRightLeftMargin = (int) motionEvent.getRawX() + mResizeStartX;
                                mCurrentResizeDownLeftMargin = mCurrentResizeRightLeftMargin / 2 -15;
                            }
                            break;
                    }
                resizeWidget();
                    break;
            }
            return true;
        }
    }

    private void resizeWidget() {
        mCurrentWidgetWidth = mCurrentResizeRightLeftMargin + 30;
        mCurrentWidgetHeight = mCurrentResizeDownTopMargin + 30;
        launcherInfo.hostView.setLayoutParams(new FrameLayout.LayoutParams(mCurrentWidgetWidth, mCurrentWidgetHeight));
        if(mWidgetContainer.getChildCount() != 0) {
            FrameLayout.LayoutParams widgetParams = (FrameLayout.LayoutParams) launcherInfo.hostView.getLayoutParams();
            widgetParams.setMargins(0, 0, 0, mTopContainerHeight - mCurrentWidgetHeight);
            launcherInfo.hostView.setLayoutParams(widgetParams);
        }

        mResizeRightLayoutParams.setMargins(mCurrentResizeRightLeftMargin, mCurrentResizeRightTopMargin,0,0);
        mResizeDownLayoutParams.setMargins(mCurrentResizeDownLeftMargin, mCurrentResizeDownTopMargin, 0, 0);
        mResizeDown.setLayoutParams(mResizeDownLayoutParams);
        mResizeRight.setLayoutParams(mResizeRightLayoutParams);
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
        int numberOfApps = 0;
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setmLabel(ri.loadLabel(mPackageManager));
            app.setmName(ri.activityInfo.packageName);
            app.setmIcon(ri.activityInfo.loadIcon(mPackageManager));
            app.setmNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getmLabel().toString(), this));
            if(SharedPrefs.getAppVisible(this, (String) ri.loadLabel(mPackageManager))) {
                dockerApps.add(app);
                //Log.i("no of runs", "label: " + app.getmLabel() + ": " + " package name: " + String.valueOf(ri.activityInfo.packageName) + String.valueOf(app.getmNumberOfStarts()));
            }
            numberOfApps++;
        }
        SharedPrefs.setNumberOfApps(numberOfApps, mContext);
        AppsSorter.sortApps(this, dockerApps, "most used", true);
        int j;
        for(j = 0; j < dockerApps.size(); j++) {
            View view = LayoutInflater.from(this).inflate(R.layout.dock_item,null);
            TextView homeNotifications = (TextView) view.findViewById(R.id.home_notifications);
            if(dockerApps.get(j).getmLabel().toString().equalsIgnoreCase("Messaging")) {
                SharedPrefs.setMessagingPackageName(this, (String) dockerApps.get(j).getmName());
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
            } else if(dockerApps.get(j).getmLabel().toString().equalsIgnoreCase("Phone")) {
                view.setTag("Phone");
                if(MissedCallsCountRetriever.getMissedCallsCount(this) > 0) {
                    homeNotifications.setVisibility(View.VISIBLE);
                    homeNotifications.setText(MissedCallsCountRetriever.getMissedCallsCount(this));
                } else {
                    homeNotifications.setVisibility(View.GONE);
                }
            } else {
                view.setTag(dockerApps.get(j).getmName());
            }

            ImageView iv = (ImageView) view.findViewById(R.id.dock_app_icon);

            final TextView tv = (TextView) view.findViewById(R.id.dock_app_name);
            String path = this.getFilesDir().getAbsolutePath();

            ////////////// load icon from storage ////////////
            //Bitmap icon = IconLoader.loadImageFromStorage(path, (String) dockerApps.get(j).getmLabel());
            Bitmap icon = IconLoader.loadImageFromStorage(path, (String) dockerApps.get(j).getmLabel());
            if(icon == null) {
                icon = ((BitmapDrawable) dockerApps.get(j).getmIcon()).getBitmap();
            }
//            else {
//                // rounded??
//                //icon = RoundBitmapGenerator.getCircleBitmap(icon);
//            }
            iv.setImageDrawable(new BitmapDrawable(this.getResources(), icon));
            iv.setLayoutParams(layoutParams);
            tv.setText(dockerApps.get(j).getmLabel());
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
                        if(!mAllScrollsDisabled) {
                            if (diffX > 0) {
                                Log.i("swipe: ", "right");
                                if(mWidgetControlsInvisible) {
                                    mWidgetControlsInvisible = false;
                                    showWidgetControlButtonsOnSwipeRight();
                                }
                            } else {
                                onSwipeLeft();
                            }
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    private void showWidgetControlButtonsOnSwipeRight() {
        if(mWidgetControlsInvisible) {
            for(int i = 100, j = 4; i < 600; i = i + 100, j--) {
                final int jj = j;
                mControls.get(j).setVisibility(View.VISIBLE);
                final Animation fadeIn = new AlphaAnimation(1,0);
                fadeIn.setStartOffset(i);
                fadeIn.setDuration(i);
                mControls.get(j).setAnimation(fadeIn);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run()
                    {
                        mControls.get(jj).setVisibility(View.GONE);
                    }
                }, i * 2);

            }
        } else {
            for(int i = 100, j = 0; i < 600; i = i + 100, j++) {
                mControls.get(j).setVisibility(View.VISIBLE);
                final Animation fadeIn = new AlphaAnimation(0,1);
                fadeIn.setStartOffset(i);
                fadeIn.setDuration(i);
                mControls.get(j).setAnimation(fadeIn);
            }

            if(mWidgetContainer.getChildCount() > 0 &&  !mIsWidgetPinned) {
                mRemovePage.setEnabled(true);
            } else {
                mRemovePage.setEnabled(false);
            }
            if(mWidgetContainer.getChildCount() > 1) {
                mPinPage.setEnabled(true);
            } else {
                mPinPage.setEnabled(false);
            }
            if(mWidgetContainer.getChildCount() < 10 && !mIsWidgetPinned) {
                mAddPage.setEnabled(true);
            } else {
                mAddPage.setEnabled(false);
            }
        }
    }

    private void onSwipeLeft() {
        Intent intent = new Intent(HomeActivity.this, AppsListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }


    private boolean sameNoOfAllApps() {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = mPackageManager.queryIntentActivities(i, 0);
        int numberOfApps = 0;
        for(ResolveInfo ignored :availableActivities){
            numberOfApps++;
        }
        return SharedPrefs.getNumberOfApps(mContext) == numberOfApps;
    }

    @Override
    protected void onResume() {
        Log.i("onResume","home activity");
        mDockLayout = (LinearLayout) findViewById(R.id.dock_list);
        int dockCount = mDockLayout.getChildCount();
        if(dockCount > 0) {
            ((HorizontalScrollView) mDockLayout.getParent()).scrollTo(0,0);
        }
        if(SharedPrefs.getHomeReloadRequired(this) || (SharedPrefs.getVisibleCount(this) > 0 && mDockLayout.getChildCount() != SharedPrefs.getVisibleCount(this)) || !sameNoOfAllApps()) {
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
        if(mSmsReceiver != null) {
            unregisterReceiver(mSmsReceiver);
            mSmsReceiver = null;
        }
        if(missedCallObserver != null) {
            getApplicationContext().getContentResolver().unregisterContentObserver(missedCallObserver);
        }
        if(mPackageUpdateReceiver != null) {
            unregisterReceiver(mPackageUpdateReceiver);
            mPackageUpdateReceiver = null;
        }
        if(((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount() > 0) {
            for(int i = 0; i < ((LinearLayout) mWidgetScrollView.getChildAt(0)).getChildCount(); i++) {
                ((WidgetFrame) mWidgetContainer.getChildAt(i)).getAppWidgetHost().stopListening();
            }
        }
        super.onPause();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
