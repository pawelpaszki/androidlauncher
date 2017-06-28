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

public class ResetIconsActivity extends AppCompatActivity {

    private List<AppDetail> mApps;
    private int mIconSide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_icons);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        View someView = findViewById(R.id.reset_icons_container);
        View root = someView.getRootView();
        root.setBackgroundColor(0xC5CAE9);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reset_icons_toolbar);
        toolbar.setTitle("Set Default Icons");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadApps();
        loadListView();
    }

    private void loadApps() {
        PackageManager mPackageManager = getPackageManager();
        mApps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = mPackageManager.queryIntentActivities(i, 0);
        String path = getFilesDir().getAbsolutePath();
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setmLabel(ri.loadLabel(mPackageManager));
            app.setmName(ri.activityInfo.packageName);
            app.setmIcon(ri.activityInfo.loadIcon(mPackageManager));
            app.setmNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getmLabel().toString(), this));
            if(ri.loadLabel(mPackageManager).toString().equalsIgnoreCase("Settings")) {
                mIconSide = ri.activityInfo.loadIcon(mPackageManager).getIntrinsicWidth();
            }
            if(IconLoader.loadImageFromStorage(path, (String) ri.loadLabel(mPackageManager)) != null) {
                mApps.add(app);
            }
        }
        mApps = AppsSorter.sortApps(mApps, "most used");
    }

    private void loadListView() {
        ListView list = (ListView) findViewById(R.id.reset_icons_list);

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.visible_app_item,
                mApps) {
            @NonNull
            @Override
            public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.visible_app_item, null);
                }

                String path = this.getContext().getFilesDir().getAbsolutePath();
                //Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getmLabel());
                Bitmap icon = IconLoader.loadImageFromStorage(path, (String) mApps.get(position).getmLabel());
                if(icon == null) {
                    icon  = ((BitmapDrawable) mApps.get(position).getmIcon()).getBitmap();
                } else {
                    // rounded ??
                    //icon = RoundBitmapGenerator.getCircleBitmap(icon);
                }

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.visible_app_icon);
                if(icon.getWidth() != mIconSide || icon.getHeight() != mIconSide) {
                    icon = Bitmap.createScaledBitmap(icon, mIconSide, mIconSide, false);
                }
                appIcon.setImageDrawable(new BitmapDrawable(ResetIconsActivity.this.getResources(), icon));

                TextView appLabel = (TextView)convertView.findViewById(R.id.visible_app_label);
                appLabel.setText(mApps.get(position).getmLabel());

                CheckBox visible = (CheckBox)convertView.findViewById(R.id.toggle_visible);
                visible.setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IconLoader.saveIcon(ResetIconsActivity.this, null, String.valueOf(mApps.get(position).getmLabel()));
                        int nonDefault = SharedPrefs.getNonDefaultIconsCount(ResetIconsActivity.this);
                        SharedPrefs.setNonDefaultIconsCount(nonDefault -1, ResetIconsActivity.this);
                        if(nonDefault == 1) {
                            onBackPressed();
                        } else {
                            reloadActivity();
                        }
                    }
                });

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }

    private void reloadActivity() {
        recreate();
    }
}
