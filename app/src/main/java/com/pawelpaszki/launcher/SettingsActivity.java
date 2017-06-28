package com.pawelpaszki.launcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pawelpaszki.launcher.utils.SharedPrefs;

/**
 * Created by PawelPaszki on 11/05/2017.
 * Settings view
 */

public class SettingsActivity extends AppCompatActivity {

    private CheckBox mShowAppNamesCheckBox;
//    private boolean mPinEntered;
//    private AlertDialog.Builder mBuilder;
//    private String mPassPhrase;
//    private AlertDialog mDialog;
//    private Context mContext;
//    private TextView mSafeMode;
//    private TextView mSafeModeDesc;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
//        mContext = this;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        View someView = findViewById(R.id.settings_container);
        View root = someView.getRootView();
        root.setBackgroundColor(0xC5CAE9);

        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        ScrollView settings_scroll = (ScrollView) findViewById(R.id.settings_scroll);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) settings_scroll
                .getLayoutParams();

        layoutParams.setMargins(0, actionBarHeight, 0, 0);

        settings_scroll.setLayoutParams(layoutParams);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Spinner sortSpinner = (Spinner) findViewById(R.id.sort_by_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().startsWith("by name")) {
                    sortByName();
                } else {
                    sortByMostUsed();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        String sortType = SharedPrefs.getSortingMethod(this);
        if(sortType.equals("most used")) {
            sortSpinner.setSelection(1);
        }

        Spinner noOfColsSpinner = (Spinner) findViewById(R.id.no_of_apps_per_row);
        ArrayAdapter<CharSequence> colsAdapter = ArrayAdapter.createFromResource(this,
                R.array.no_of_cols, android.R.layout.simple_spinner_item);
        colsAdapter.setDropDownViewResource(R.layout.spinner_item);
        noOfColsSpinner.setAdapter(colsAdapter);
        noOfColsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPrefs.setNumberOfColumns(Integer.parseInt(parent.getItemAtPosition(position).toString()), SettingsActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        int spinnerPosition = SharedPrefs.getNumberOfColumns(this);
        if(spinnerPosition == 0) {
            spinnerPosition = 4;
        }

        noOfColsSpinner.setSelection(spinnerPosition - 1);

        mShowAppNamesCheckBox = (CheckBox) findViewById(R.id.show_names_checkbox);
        mShowAppNamesCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefs.setShowAppNames(((CheckBox) v).isChecked(), SettingsActivity.this);
            }
        });
        mShowAppNamesCheckBox.setChecked(SharedPrefs.getShowAppNames(this));
        TextView sortDesc = (TextView) findViewById(R.id.sort_by_text_view_desc);
        TextView showLabelsDesc = (TextView) findViewById(R.id.show_app_names_desc);
        TextView noOfColsDesc = (TextView) findViewById(R.id.no_of_columns_desc);

        sortDesc.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int width = sortDesc.getMeasuredWidth() * 3 / 4;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sortDesc.getLayoutParams();
        params.width = width;
        sortDesc.setLayoutParams(params);
        params = (RelativeLayout.LayoutParams) showLabelsDesc.getLayoutParams();
        params.width = width;
        showLabelsDesc.setLayoutParams(params);

        params = (RelativeLayout.LayoutParams) noOfColsDesc.getLayoutParams();
        params.width = width;
        noOfColsDesc.setLayoutParams(params);

//        mSafeMode = (TextView) findViewById(R.id.enable_safe_mode);
//
//        mSafeModeDesc = (TextView) findViewById(R.id.enable_safe_mode_desc);
//
//        if(SharedPrefs.getSafeModeOn(mContext)) {
//            mSafeMode.setText(R.string.disable_safe_mode);
//            mSafeModeDesc.setText(R.string.disable_safe_mode_desc);
//        }

    }


    public void set_icons(View view) {
        Intent i = new Intent(this, ChangeIconsActivity.class);
        i.putExtra("option", "gallery");
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void set_icons_web(View view) {
        Intent i = new Intent(this, ChangeIconsActivity.class);
        i.putExtra("option", "web");
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void reset_icons(View view) {
        if(SharedPrefs.getNonDefaultIconsCount(this) == 0) {
            Toast.makeText(SettingsActivity.this, "All icons are default icons",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent(this, ResetIconsActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        }
    }

    public void sortByName() {
        SharedPrefs.setSortingMethod(this,"name");
    }


    public void sortByMostUsed() {
        SharedPrefs.setSortingMethod(this,"most used");
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
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }

    public void selectVisibleApps(View view) {
        Intent i = new Intent(this, SelectAppsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void changeWallpaper(View view) {
        Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        startActivity(Intent.createChooser(intent, "Select Wallpaper"));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void setWallpaperColor(View view) {
        Intent i = new Intent(this, ColorPickActivity.class);
        i.putExtra("option", "wallpaper");
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

//    public void enableSafeMode(View view) {
//        mBuilder = new AlertDialog.Builder(this);
//        mBuilder.setTitle("Please type Passphrase");
//        mPinEntered = false;
//        View viewInflated = LayoutInflater.from(this).inflate(R.layout.pin_input, null, false);
//        final EditText pin_input = (EditText) viewInflated.findViewById(R.id.pin);
//        mBuilder.setView(viewInflated);
//
//        mBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        mBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        mDialog = mBuilder.create();
//        mDialog.show();
//        mDialog.setCancelable(false);
//        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v) {
//                if(!SharedPrefs.getSafeModeOn(mContext)) {
//                    if(!mPinEntered) {
//                        mDialog.setTitle("Please confirm Passphrase");
//                        mPassPhrase = pin_input.getText().toString();
//                        pin_input.setText("");
//                        mPinEntered = true;
//                    } else {
//                        if(pin_input.getText().toString().equals(mPassPhrase)) {
//                            SharedPrefs.setSafeModeOn(true, mContext);
//                            mSafeMode.setText(R.string.disable_safe_mode);
//                            mSafeModeDesc.setText(R.string.disable_safe_mode_desc);
//                            SharedPrefs.setPasshrase(mContext, pin_input.getText().toString());
//                            mDialog.dismiss();
//                            Toast.makeText(mContext,"Safe mode has been enabled" ,
//                                    Toast.LENGTH_LONG).show();
//                            onBackPressed();
//                        } else {
//                            pin_input.setText("");
//                            mDialog.setTitle("Please try again");
//                        }
//                    }
//                    Log.i("text", pin_input.getText().toString());
//                } else {
//                    if(pin_input.getText().toString().equals(SharedPrefs.getPassphrase(mContext))) {
//                        mSafeMode.setText(R.string.enable_safe_mode);
//                        mSafeModeDesc.setText(R.string.enable_safe_mode_desc);
//                        SharedPrefs.setSafeModeOn(false, mContext);
//                        Toast.makeText(mContext,"Safe mode has been disabled" ,
//                                Toast.LENGTH_LONG).show();
//                        SharedPrefs.setHomeRecreateRequired(true,mContext);
//                        SharedPrefs.setPasshrase(mContext,"");
//                        mDialog.dismiss();
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                onBackPressed();
//                            }
//                        }, 500);
//
//                    } else {
//                        pin_input.setText("");
//                        mDialog.setTitle("Please try again");
//                    }
//                }
//            }
//        });
//    }
}
