package com.pawelpaszki.launcher;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;

public class ColorPickActivity extends AppCompatActivity {

    private int mRedValue;
    private int mGreenValue;
    private int mBlueValue;
    private int mAlphaValue;
    private ImageView pickedColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_pick);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        View someView = findViewById(R.id.color_pick_container);
        View root = someView.getRootView();
        root.setBackgroundColor(0xC5CAE9);

        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        RelativeLayout settings_scroll = (RelativeLayout) findViewById(R.id.color_picker_layout);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) settings_scroll
                .getLayoutParams();

        layoutParams.setMargins(0, actionBarHeight, 0, 0);

        settings_scroll.setLayoutParams(layoutParams);
        Toolbar toolbar = (Toolbar) findViewById(R.id.color_pick_toolbar);
        toolbar.setTitle("Set Wallpaper Color");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pickedColor = (ImageView) findViewById(R.id.picked_color);

        SeekBar mRedSeekBar = (SeekBar) findViewById(R.id.rSeekBar);
        mRedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                mRedValue = progress;
                setImageViewColor();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar mGreenSeekBar = (SeekBar) findViewById(R.id.gSeekBar);
        mGreenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                mGreenValue = progress;
                setImageViewColor();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar mBlueSeekBar = (SeekBar) findViewById(R.id.bSeekBar);
        mBlueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                mBlueValue = progress;
                setImageViewColor();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar mAlphaSeekBar = (SeekBar) findViewById(R.id.aSeekBar);
        mAlphaSeekBar.setProgress(mAlphaSeekBar.getMax());
        mAlphaValue = mAlphaSeekBar.getMax();
        setImageViewColor();
        mAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                mAlphaValue = progress;
                setImageViewColor();
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    private void setImageViewColor() {
        pickedColor.setBackgroundColor(Color.argb(mAlphaValue, mRedValue, mGreenValue,mBlueValue));
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

    public void setSolidWallpaperColor(View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.argb(mAlphaValue, mRedValue, mGreenValue,mBlueValue));
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(getApplicationContext());
        try {
            myWallpaperManager.setBitmap(bitmap);
            Toast.makeText(ColorPickActivity.this,"Wallpaper has been set" ,
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(ColorPickActivity.this,"Unable to set wallpaper" ,
                    Toast.LENGTH_LONG).show();
        }
        onBackPressed();
    }
}
