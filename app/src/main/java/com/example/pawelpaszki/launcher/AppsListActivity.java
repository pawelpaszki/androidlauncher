package com.example.pawelpaszki.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pawelpaszki.launcher.adapters.GridAdapter;
import com.example.pawelpaszki.launcher.utils.AppsSorter;
import com.example.pawelpaszki.launcher.utils.MissedCallsCountRetriever;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class AppsListActivity extends Activity {

    private PackageManager mPackageManager;
    private List<AppDetail> mApps;
    private GridView mGridView;
    private int mVisibleCount = 0;
    private int mIconSide;
    private TextView mAppLabelTextView;
    private LinearLayout mUninstallPackageLayout;
    private String mHighlightedViewTag;
    private BroadcastReceiver mSmsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("message received", "msg");
            for(int i = 0; i < mGridView.getChildCount(); i++) {
                if(mGridView.getChildAt(i).getTag().equals("Messaging")) {
                    mAppLabelTextView = (TextView) ((FrameLayout) ((LinearLayout) mGridView.getChildAt(i)).getChildAt(0)).getChildAt(1);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAppLabelTextView.setText(String.valueOf(MissedCallsCountRetriever.getUnreadMessagesCount(AppsListActivity.this)));
                        }
                    }, 500);
                }
            }
        }
    };
    private boolean mUninstalled;

    @Override
    protected void onStart() {
        mSmsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("message received", "msg");
                for(int i = 0; i < mGridView.getChildCount(); i++) {
                    if(mGridView.getChildAt(i).getTag().equals("Messaging")) {
                        mAppLabelTextView = (TextView) ((FrameLayout) ((LinearLayout) mGridView.getChildAt(i)).getChildAt(0)).getChildAt(1);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAppLabelTextView.setText(String.valueOf(MissedCallsCountRetriever.getUnreadMessagesCount(AppsListActivity.this)));
                            }
                        }, 500);
                    }
                }
            }
        };
        registerReceiver(mSmsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));
        SharedPrefs.setHomeReloadRequired(true,this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(mSmsReceiver != null) {
            unregisterReceiver(mSmsReceiver);
            mSmsReceiver = null;
        }
        super.onStop();

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        loadApps();
        if(mVisibleCount != SharedPrefs.getVisibleCount(this)) {
            SharedPrefs.setVisibleCount(mVisibleCount, this);
        }
        mUninstallPackageLayout = (LinearLayout) findViewById(R.id.uninstall_package);
        mGridView =(GridView) findViewById(R.id.gridView);
        mGridView.setAdapter(new GridAdapter(this, mApps, mPackageManager, mIconSide, mUninstallPackageLayout));
        mGridView.setFastScrollEnabled(true);
        int noOfCols = SharedPrefs.getNumberOfColumns(this);
        if(noOfCols != 0) {
            mGridView.setNumColumns(SharedPrefs.getNumberOfColumns(this));
        }

        detector = new GestureDetectorCompat(this, new MyGestureListener());
        mGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });

    }

    private GestureDetectorCompat detector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void hideUninstallView(View view) {
        this.mUninstallPackageLayout.setVisibility(View.GONE);
        unHighlightView();
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1) {
                if (resultCode == RESULT_OK) {
                    mUninstalled = true;
                    hideUninstallView(null);
                } else if (resultCode == RESULT_CANCELED) {
                    hideUninstallView(null);
                } else if (resultCode == RESULT_FIRST_USER) {
                    Log.d("TAG", "onActivityResult: failed to (un)install");
                }
            } else {
                unHighlightView();
            }

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
                            onSwipeRight();
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

    private void onSwipeLeft() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void onSwipeRight() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    private void loadApps(){
        mPackageManager = getPackageManager();
        mApps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = mPackageManager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setmLabel(ri.loadLabel(mPackageManager));
            app.setmName(ri.activityInfo.packageName);
            app.setmIcon(ri.activityInfo.loadIcon(mPackageManager));
            app.setmNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getmLabel().toString(), this));
            if(ri.loadLabel(mPackageManager).toString().equalsIgnoreCase("Settings")) {
                mIconSide = ri.activityInfo.loadIcon(mPackageManager).getIntrinsicWidth();
                Log.i("icon side", String.valueOf(mIconSide));
            }
            if(SharedPrefs.getAppVisible(this, (String) ri.loadLabel(mPackageManager))) {
                mApps.add(app);
                mVisibleCount++;
                Log.i("app starts", String.valueOf(ri.loadLabel(mPackageManager)) + " " + String.valueOf(ri.activityInfo.packageName) + " "+ SharedPrefs.getNumberOfActivityStarts(app.getmLabel().toString(), this));
            }

            //Log.i("name", String.valueOf(ri.loadLabel(mPackageManager)) + " " + SharedPrefs.getNumberOfActivityStarts(app.getmLabel().toString(), this));
        }
        //Log.i("sorting method", SharedPrefs.getSortingMethod(this));
        mApps = AppsSorter.sortApps(this, mApps, SharedPrefs.getSortingMethod(this), false);
    }

    public void highlightView(String tag) {
        mHighlightedViewTag = tag;
        for(int i = 0; i < mGridView.getChildCount(); i++) {
            if (mGridView.getChildAt(i).getTag().equals(tag)) {
                LinearLayout view = (LinearLayout) mGridView.getChildAt(i);
                GradientDrawable border = new GradientDrawable();
                border.setColor(0x00FFFFFF);
                border.setStroke(3, 0xFFFF0000);
                view.setBackground(border);
                break;
            }
        }
    }

    public void unHighlightView() {
        for(int i = 0; i < mGridView.getChildCount(); i++) {
            if (mGridView.getChildAt(i).getTag().equals(mHighlightedViewTag)) {
                LinearLayout view = (LinearLayout) mGridView.getChildAt(i);
                GradientDrawable border = new GradientDrawable();
                border.setColor(0x00000000);
                view.setBackground(border);
                break;
            }
        }
        mHighlightedViewTag = "";
        if(mUninstalled){
            mUninstalled = false;
            recreate();
        }
    }

//    public void toggleMenu(View view) {
//        Toast.makeText(this,String.valueOf(SharedPrefs.getNumberOfColumns(this)) ,
//                Toast.LENGTH_LONG).show();
//        this.menuVisible = !menuVisible;
//        menu_options = (RelativeLayout) findViewById(R.id.options);
//        Button toggle_menu = (Button) findViewById(R.id.arrow);
//        sort_az = (Button) findViewById(R.id.sort_az);
//        sort_most_used = (Button) findViewById(R.id.sort_most_used);
//        settings = (Button) findViewById(R.id.settings);
//        if(!this.menuVisible) {
//
//            slideOutToRight(this, settings);
//
//            slideOutToRight(this, sort_az);
//
//            slideOutToRight(this, sort_most_used);
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    menu_options.setBackgroundColor(android.graphics.Color.argb(0, 255, 255, 255));
//                    settings.setVisibility(View.GONE);
//                    sort_az.setVisibility(View.GONE);
//                    sort_most_used.setVisibility(View.GONE);
//                }
//            }, 300);
//            toggle_menu.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.left_arrow, 0, 0, 0);
//        } else {
//            menu_options.setBackgroundColor(android.graphics.Color.argb(102, 255, 255, 255));
//            settings.setVisibility(View.VISIBLE);
//            slideInFromRight(this, settings);
//            sort_az.setVisibility(View.VISIBLE);
//            slideInFromRight(this, sort_az);
//            sort_most_used.setVisibility(View.VISIBLE);
//            slideInFromRight(this, sort_most_used);
//            toggle_menu.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.right_arrow, 0, 0, 0);
//        }
//    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }

}