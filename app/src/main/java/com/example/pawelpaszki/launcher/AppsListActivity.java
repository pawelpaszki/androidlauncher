package com.example.pawelpaszki.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.adapters.GridAdapter;
import com.example.pawelpaszki.launcher.utils.NoOfStartsSorter;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.pawelpaszki.launcher.animations.SlideAnimation.slideInFromRight;
import static com.example.pawelpaszki.launcher.animations.SlideAnimation.slideOutToRight;

public class AppsListActivity extends Activity {

    private PackageManager manager;
    private List<AppDetail> apps;
    private boolean menuVisible = false;
    private boolean reverseList = false;
    private Button settings;
    private Button sort_az;
    private Button sort_most_used;
    private RelativeLayout menu_options;
    private GridView gv;
    private int noOfCols;
    private Handler handler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_apps_list);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        loadApps();
        gv=(GridView) findViewById(R.id.gridView);
        gv.setAdapter(new GridAdapter(this, apps, manager));
        gv.setFastScrollEnabled(true);
        noOfCols = SharedPrefs.getNumberOfColumns(this);
        if(noOfCols != 0) {
            gv.setNumColumns(SharedPrefs.getNumberOfColumns(this));
        }

        detector = new GestureDetectorCompat(this, new MyGestureListener());
        gv.setOnTouchListener(new View.OnTouchListener() {
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
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    private void loadApps(){
        manager = getPackageManager();
        apps = new ArrayList<AppDetail>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setLabel(ri.loadLabel(manager));
            app.setName(ri.activityInfo.packageName);
            app.setIcon(ri.activityInfo.loadIcon(manager));
            app.setNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getLabel().toString(), this));
            apps.add(app);
            Log.i("app starts", String.valueOf(ri.loadLabel(manager)) + " " + SharedPrefs.getNumberOfActivityStarts(app.getLabel().toString(), this));
        }
        Log.i("sorting method", SharedPrefs.getSortingMethod(this));
        sortApps(SharedPrefs.getSortingMethod(this));
    }

    private void sortApps(String parameter) {

        Log.i("sorting method", parameter);
        if(parameter.equals("name")) {
            Collections.sort(apps, new Comparator<AppDetail>() {
                @Override
                public int compare(AppDetail app1, AppDetail app2) {
                    return app1.getLabel().toString().compareTo(app2.getLabel().toString());
                }
            });
            if(SharedPrefs.getReverseListOrderFlag(this) == 1) {
                Collections.reverse(apps);
            }
        } else {
            Collections.sort(apps, new NoOfStartsSorter() {
                @Override
                public int compare(AppDetail app1, AppDetail app2) {
                    if (app1.getNumberOfStarts() > app2.getNumberOfStarts())
                        return 1;
                    if (app1.getNumberOfStarts() < app2.getNumberOfStarts())
                        return -1;
                    return app1.getLabel().toString().compareTo(app2.getLabel().toString()) * -1;
                }
            });
            if(!(SharedPrefs.getReverseListOrderFlag(this) == 1)) {
                Collections.reverse(apps);
            }
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
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        finish();
    }

}