package com.pawelpaszki.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pawelpaszki.launcher.utils.AppsSorter;
import com.pawelpaszki.launcher.utils.IconLoader;
import com.pawelpaszki.launcher.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class SelectAppsActivity extends AppCompatActivity {

    private List<AppDetail> mApps;
    private int mVisibleCounter = 0;
    private int mIconside;

    private void loadApps(){
        PackageManager mPackageManager = getPackageManager();
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
                mIconside = ri.activityInfo.loadIcon(mPackageManager).getIntrinsicWidth();
            }
            mApps.add(app);
        }
        mApps = AppsSorter.sortApps(this,mApps, "most used", false);
    }

    private void loadListView(){
        ListView list = (ListView) findViewById(R.id.visible_apps_list);

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.visible_app_item,
                mApps) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.visible_app_item, null);
                }

                String path = this.getContext().getFilesDir().getAbsolutePath();
                //Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getmLabel());
                Bitmap icon = IconLoader.loadImageFromStorage(path, (String) mApps.get(position).getmLabel());
                if(icon == null) {
                    icon  = ((BitmapDrawable) mApps.get(position).getmIcon()).getBitmap();
                }
//                else {
//                    //rounded??
//                    //icon = RoundBitmapGenerator.getCircleBitmap(icon);
//                }

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.visible_app_icon);

                if(icon.getWidth() != mIconside || icon.getHeight() != mIconside) {
                    icon = Bitmap.createScaledBitmap(icon, mIconside, mIconside, false);
                }

                appIcon.setImageDrawable(new BitmapDrawable(SelectAppsActivity.this.getResources(), icon));

                TextView appLabel = (TextView)convertView.findViewById(R.id.visible_app_label);
                appLabel.setText(mApps.get(position).getmLabel());

                final CheckBox visible = (CheckBox)convertView.findViewById(R.id.toggle_visible);
                if(SharedPrefs.getAppVisible(SelectAppsActivity.this, (String) mApps.get(position).getmLabel())) {
                    mVisibleCounter++;
                }
                visible.setChecked(SharedPrefs.getAppVisible(SelectAppsActivity.this, (String) mApps.get(position).getmLabel()));
                visible.setText(mApps.get(position).getmLabel());
                visible.setTextSize(0);
                visible.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPrefs.setAppVisible(((CheckBox) v).isChecked(), (String) visible.getText(), SelectAppsActivity.this);
                        if(((CheckBox) v).isChecked()) {
                            mVisibleCounter++;
                        } else {
                            mVisibleCounter--;
                        }
                        SharedPrefs.setHomeReloadRequired(true, SelectAppsActivity.this);
                    }
                });

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        SharedPrefs.setVisibleCount(mVisibleCounter, this);
        super.onPause();

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        View someView = findViewById(R.id.select_apps_container);
        View root = someView.getRootView();
        root.setBackgroundColor(0xC5CAE9);
        Toolbar toolbar = (Toolbar) findViewById(R.id.select_apps_toolbar);
        toolbar.setTitle("Select visible apps");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadApps();
        loadListView();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }
}
