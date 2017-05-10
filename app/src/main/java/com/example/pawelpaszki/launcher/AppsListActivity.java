package com.example.pawelpaszki.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.example.pawelpaszki.launcher.adapters.GridAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.example.pawelpaszki.launcher.animations.SlideAnimation.*;

public class AppsListActivity extends Activity {

    private PackageManager manager;
    private List<AppDetail> apps;
    private boolean menuVisible = false;
    private Button settings;
    private Button sort_az;
    private Button sort_most_used;
    private RelativeLayout menu_options;
    GridView gv;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);
        loadApps();
        gv=(GridView) findViewById(R.id.gridView);

        gv.setAdapter(new GridAdapter(this, apps, manager));
        gv.setFastScrollEnabled(true);
    }

    public String getSortingMethod() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString("sortingMethod", "name");
    }

    public void setSortingMethod(String sortValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("sortingMethod", sortValue);
        editor.commit();
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
            apps.add(app);
        }
    }

    public void toggleMenu(View view) {
        this.menuVisible = !menuVisible;
        menu_options = (RelativeLayout) findViewById(R.id.options);
        Button toggle_menu = (Button) findViewById(R.id.arrow);
        sort_az = (Button) findViewById(R.id.sort_az);
        sort_most_used = (Button) findViewById(R.id.sort_most_used);
        settings = (Button) findViewById(R.id.settings);
        if(!this.menuVisible) {

            slideOutToRight(this, settings);

            slideOutToRight(this, sort_az);

            slideOutToRight(this, sort_most_used);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    menu_options.setBackgroundColor(android.graphics.Color.argb(0, 255, 255, 255));
                    settings.setVisibility(View.GONE);
                    sort_az.setVisibility(View.GONE);
                    sort_most_used.setVisibility(View.GONE);
                }
            }, 300);
            toggle_menu.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.left_arrow, 0, 0, 0);
        } else {
            menu_options.setBackgroundColor(android.graphics.Color.argb(102, 255, 255, 255));
            settings.setVisibility(View.VISIBLE);
            slideInFromRight(this, settings);
            sort_az.setVisibility(View.VISIBLE);
            slideInFromRight(this, sort_az);
            sort_most_used.setVisibility(View.VISIBLE);
            slideInFromRight(this, sort_most_used);
            toggle_menu.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.right_arrow, 0, 0, 0);
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        finish();
    }
}