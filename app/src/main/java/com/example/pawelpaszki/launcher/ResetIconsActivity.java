package com.example.pawelpaszki.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.Toast;

import com.example.pawelpaszki.launcher.utils.AppsSorter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.NetworkConnectivityChecker;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class ResetIconsActivity extends AppCompatActivity {

    private PackageManager manager;
    private List<AppDetail> apps;
    private int iconSide;
    private ListView list;
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
        manager = getPackageManager();
        apps = new ArrayList<AppDetail>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        String path = getFilesDir().getAbsolutePath();
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setLabel(ri.loadLabel(manager));
            app.setName(ri.activityInfo.packageName);
            app.setIcon(ri.activityInfo.loadIcon(manager));
            app.setNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getLabel().toString(), this));
            if(ri.loadLabel(manager).toString().equalsIgnoreCase("Settings")) {
                iconSide = ri.activityInfo.loadIcon(manager).getIntrinsicWidth();
            }
            if(IconLoader.loadImageFromStorage(path, (String) ri.loadLabel(manager)) != null) {
                apps.add(app);
            }
        }
        Log.i("apps count", String.valueOf(apps.size()));
        apps = AppsSorter.sortApps(this,apps, "most used", false);
    }

    private void loadListView() {
        list = (ListView)findViewById(R.id.reset_icons_list);

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.visible_app_item,
                apps) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.visible_app_item, null);
                }

                String path = this.getContext().getFilesDir().getAbsolutePath();
                final int index = position;
                //Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getLabel());
                Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getLabel());
                if(icon == null) {
                    icon  = ((BitmapDrawable) apps.get(position).getIcon()).getBitmap();
                } else {
                    // rounded ??
                    //icon = RoundBitmapGenerator.getCircleBitmap(icon);
                }

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.visible_app_icon);
                if(icon.getWidth() != iconSide || icon.getHeight() != iconSide) {
                    icon = Bitmap.createScaledBitmap(icon, iconSide, iconSide, false);
                }
                appIcon.setImageDrawable(new BitmapDrawable(ResetIconsActivity.this.getResources(), icon));

                TextView appLabel = (TextView)convertView.findViewById(R.id.visible_app_label);
                appLabel.setText(apps.get(position).getLabel());

                CheckBox visible = (CheckBox)convertView.findViewById(R.id.toggle_visible);
                visible.setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IconLoader.saveIcon(ResetIconsActivity.this, null, String.valueOf(apps.get(position).getLabel()));
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
