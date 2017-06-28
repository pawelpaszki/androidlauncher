package com.pawelpaszki.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import android.widget.Toast;

import com.pawelpaszki.launcher.adapters.GridAdapter;
import com.pawelpaszki.launcher.utils.AppsSorter;
import com.pawelpaszki.launcher.utils.MissedCallsCountRetriever;
import com.pawelpaszki.launcher.utils.SharedPrefs;

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
            onSmsReceive();
        }
    };

    private BroadcastReceiver mPackageUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reload();
        }
    };
    private boolean mUninstalled;

    @Override
    protected void onStart() {
        mSmsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onSmsReceive();
            }
        };
        registerReceiver(mSmsReceiver, new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED"));

        mPackageUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reload();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        registerReceiver(mPackageUpdateReceiver, intentFilter);
        SharedPrefs.setHomeReloadRequired(true,this);
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
        super.onStart();
    }

    private void onSmsReceive() {
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

    private void reload() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        }, 50);
    }

    @Override
    protected void onStop() {
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

    @Override
    protected void onResume() {
        if(!sameNoOfAllApps()) {
            recreate();
        }
        super.onResume();
    }

    private boolean sameNoOfAllApps() {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = mPackageManager.queryIntentActivities(i, 0);
        int numberOfApps = 0;
        for(ResolveInfo ignored :availableActivities){
            numberOfApps++;
        }
        return SharedPrefs.getNumberOfApps(this) == numberOfApps;
    }

    public void hideUninstallView(View view) {
        this.mUninstallPackageLayout.setVisibility(View.GONE);
        unHighlightView();
    }

    public void uninstallPackage(View view) {
        if(mHighlightedViewTag.equals("Messaging") || mHighlightedViewTag.equals("Phone")) {
            Toast.makeText(this,"This application cannot be uninstalled" ,
                    Toast.LENGTH_LONG).show();
            hideUninstallView(view);
        } else {
            int UNINSTALL_REQUEST_CODE = 1;
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:" + mHighlightedViewTag));
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
        }
        SharedPrefs.setHomeReloadRequired(true, this);
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            try {
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
            } catch (Exception e) {
                Toast.makeText(this,"This application cannot be uninstalled" ,
                        Toast.LENGTH_LONG).show();
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
                        }
                    }
                }
                return true;
            }
            return false;
        }
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
        if(mPackageUpdateReceiver != null) {
            unregisterReceiver(mPackageUpdateReceiver);
            mPackageUpdateReceiver = null;
        }
        if(mSmsReceiver != null) {
            unregisterReceiver(mSmsReceiver);
            mSmsReceiver = null;
        }
        super.onPause();
    }

    private void loadApps(){
        mPackageManager = getPackageManager();
        mApps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        int numberOfApps = 0;
        List<ResolveInfo> availableActivities = mPackageManager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setmLabel(ri.loadLabel(mPackageManager));
            app.setmName(ri.activityInfo.packageName);
            app.setmIcon(ri.activityInfo.loadIcon(mPackageManager));
            app.setmNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getmLabel().toString(), this));
            if(ri.loadLabel(mPackageManager).toString().equalsIgnoreCase("Settings")) {
                mIconSide = ri.activityInfo.loadIcon(mPackageManager).getIntrinsicWidth();
            }
            if(SharedPrefs.getAppVisible(this, (String) ri.loadLabel(mPackageManager))) {
                if(ri.loadLabel(mPackageManager).toString().equalsIgnoreCase("Settings")) {
                    if(!SharedPrefs.getSafeModeOn(this)) {
                        mApps.add(app);
                    }
                } else {
                    mApps.add(app);
                }

                mVisibleCount++;
                Log.i("app starts", String.valueOf(ri.loadLabel(mPackageManager)) + " " + String.valueOf(ri.activityInfo.packageName) + " "+ SharedPrefs.getNumberOfActivityStarts(app.getmLabel().toString(), this));
            }
            numberOfApps++;
        }
        SharedPrefs.setNumberOfApps(numberOfApps, this);
        mApps = AppsSorter.sortApps(mApps, SharedPrefs.getSortingMethod(this));
    }

    public void highlightView(String tag) {
        mHighlightedViewTag = tag;
        setAroundAppIconBorder(0x00FFFFFF);
    }

    public void unHighlightView() {
        setAroundAppIconBorder(0x00000000);
        mHighlightedViewTag = "";
        if(mUninstalled){
            mUninstalled = false;
            recreate();
        }
    }

    private void setAroundAppIconBorder(int color) {
        for(int i = 0; i < mGridView.getChildCount(); i++) {
            if (mGridView.getChildAt(i).getTag().equals(mHighlightedViewTag)) {
                LinearLayout view = (LinearLayout) mGridView.getChildAt(i);
                GradientDrawable border = new GradientDrawable();
                border.setColor(color);
                if(color != 0x00000000) {
                    border.setStroke(3, 0xFFFF0000);
                }
                view.setBackground(border);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }

}