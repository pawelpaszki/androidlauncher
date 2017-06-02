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

import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.RoundBitmapGenerator;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class SelectAppsActivity extends AppCompatActivity {

    private PackageManager manager;
    private List<AppDetail> apps;
    private int visibleCounter = 0;
    private int iconSide;

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
            if(ri.loadLabel(manager).toString().equalsIgnoreCase("Settings")) {
                iconSide = ri.activityInfo.loadIcon(manager).getIntrinsicWidth();
                Log.i("icon side", String.valueOf(iconSide));
            }
            apps.add(app);
        }
    }

    private ListView list;
    private void loadListView(){
        list = (ListView)findViewById(R.id.visible_apps_list);

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.visible_app_item,
                apps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.visible_app_item, null);
                }

                String path = this.getContext().getFilesDir().getAbsolutePath();
                //Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getLabel());
                Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getLabel());
                if(icon == null) {
                    icon  = ((BitmapDrawable) apps.get(position).getIcon()).getBitmap();
                } else {
                    //rounded??
                    //icon = RoundBitmapGenerator.getCircleBitmap(icon);
                }

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.visible_app_icon);

                if(icon.getWidth() != iconSide || icon.getHeight() != iconSide) {
                    icon = Bitmap.createScaledBitmap(icon, iconSide, iconSide, false);
                }

                appIcon.setImageDrawable(new BitmapDrawable(SelectAppsActivity.this.getResources(), icon));

                TextView appLabel = (TextView)convertView.findViewById(R.id.visible_app_label);
                appLabel.setText(apps.get(position).getLabel());

                final CheckBox visible = (CheckBox)convertView.findViewById(R.id.toggle_visible);
                if(SharedPrefs.getAppVisible(SelectAppsActivity.this, (String) apps.get(position).getLabel())) {
                    visibleCounter++;
                }
                visible.setChecked(SharedPrefs.getAppVisible(SelectAppsActivity.this, (String) apps.get(position).getLabel()));
                visible.setText(apps.get(position).getLabel());
                visible.setTextSize(0);
                visible.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPrefs.setAppVisible(((CheckBox) v).isChecked(), (String) visible.getText(), SelectAppsActivity.this);
                        if(((CheckBox) v).isChecked()) {
                            visibleCounter++;
                        } else {
                            visibleCounter--;
                        }
                        SharedPrefs.setHomeReloadRequired(true, SelectAppsActivity.this);
//                        Toast.makeText(SelectAppsActivity.this, String.valueOf(SharedPrefs.getAppVisible(SelectAppsActivity.this, (String) visible.getText())) + " " + (String) visible.getText(),
//                                Toast.LENGTH_LONG).show();
                    }
                });

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        SharedPrefs.setVisibleCount(visibleCounter, this);
        super.onPause();

    }

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
