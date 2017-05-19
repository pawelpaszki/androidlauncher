package com.example.pawelpaszki.launcher;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.utils.SharedPrefs;

/**
 * Created by PawelPaszki on 11/05/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    private Spinner sortSpinner;
    private CheckBox showAppNamesCheckBox;
    private Spinner noOfColsSpinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        View someView = findViewById(R.id.settings_container);
        View root = someView.getRootView();
        root.setBackgroundColor(0xC5CAE9);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sortSpinner = (Spinner) findViewById(R.id.sort_by_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SettingsActivity.this, parent.getItemAtPosition(position).toString(),
                        Toast.LENGTH_LONG).show();
                if(parent.getItemAtPosition(position).toString().equals("by name")) {
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

        noOfColsSpinner = (Spinner) findViewById(R.id.no_of_apps_per_row);
        ArrayAdapter<CharSequence> colsAdapter = ArrayAdapter.createFromResource(this,
                R.array.no_of_cols, android.R.layout.simple_spinner_item);
        colsAdapter.setDropDownViewResource(R.layout.spinner_item);
        noOfColsSpinner.setAdapter(colsAdapter);
        noOfColsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPrefs.setNumberOfColumns(Integer.parseInt(parent.getItemAtPosition(position).toString()), SettingsActivity.this);
                if(Integer.parseInt(parent.getItemAtPosition(position).toString()) >=5) {
                    showAppNamesCheckBox.setChecked(false);
                    SharedPrefs.setShowAppNames(false, SettingsActivity.this);
                    showAppNamesCheckBox.setEnabled(false);
                } else {
                    showAppNamesCheckBox.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        int spinnerPosition = SharedPrefs.getNumberOfColumns(this);

        noOfColsSpinner.setSelection(spinnerPosition - 1);

        showAppNamesCheckBox = (CheckBox) findViewById(R.id.show_names_checkbox);
        showAppNamesCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefs.setShowAppNames(((CheckBox) v).isChecked(), SettingsActivity.this);
                Toast.makeText(SettingsActivity.this, String.valueOf(SharedPrefs.getShowAppNames(SettingsActivity.this)),
                        Toast.LENGTH_LONG).show();
            }
        });
        showAppNamesCheckBox.setChecked(SharedPrefs.getShowAppNames(this));
    }

    public void sortByName() {
        if(SharedPrefs.getSortingMethod(this).equals("name")) {
            if((SharedPrefs.getReverseListOrderFlag(this) == 1)) {
                SharedPrefs.setReverseListOrderFlag(0,this);
            } else {
                SharedPrefs.setReverseListOrderFlag(1,this);
            }
        } else {
            SharedPrefs.setReverseListOrderFlag(0,this);
        }
        SharedPrefs.setSortingMethod(this,"name");
    }

    public void sortByMostUsed() {
        if(!SharedPrefs.getSortingMethod(this).equals("name")) {
            if((SharedPrefs.getReverseListOrderFlag(this) == 1)) {
                SharedPrefs.setReverseListOrderFlag(0,this);
            } else {
                SharedPrefs.setReverseListOrderFlag(1,this);
            }
        } else {
            SharedPrefs.setReverseListOrderFlag(0,this);
        }

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
        Intent intent = new Intent(this, AppsListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        //overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        finish();
    }

    public void selectVisibleApps(View view) {
        Toast.makeText(this, "Show visible apps",
                Toast.LENGTH_LONG).show();
    }

    public void changeWallpaper(View view) {
        Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        startActivity(Intent.createChooser(intent, "Select Wallpaper"));
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
}
