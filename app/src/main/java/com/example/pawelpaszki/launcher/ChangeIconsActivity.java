package com.example.pawelpaszki.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.utils.AppsSorter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.NetworkConnectivityChecker;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangeIconsActivity extends AppCompatActivity {

    private List<AppDetail> mApps;
    private static int RESULT_LOAD_IMAGE = 1;
    private String mAppName;
    private int mActionBarHeight;
    private int mMinX;
    private int mMinY;
    private int mMaxX;
    private int mMaxY;
    private int mStartX;
    private int mStartY;
    private int mButtonSide;
    private String mTag;
    private Button mTopLeft;
    private Button mTopRight;
    private Button mBottomLeft;
    private Button mBottomRight;
    private FrameLayout.LayoutParams mTopLeftButtonParams;
    private FrameLayout.LayoutParams mTopRightButtonParams;
    private FrameLayout.LayoutParams mBottomLeftButtonParams;
    private FrameLayout.LayoutParams mBottomRightButtonParams;

    private int mHeight;
    private int mWidth;
    private int mPreviousTopLeftX;
    private int mPreviousTopRightX;
    private int mPreviousTopLeftY;
    private int mPreviousTopRightY;
    private int mPreviousBottomLeftX;
    private int mPreviousBottomLeftY;
    private int mPreviousBottomRightX;
    private int mPreviousBottomRightY;
    private ImageView mImageView;
    private int mIconSide;
    private String mOption;

    private void loadApps(){
        PackageManager manager = getPackageManager();
        mApps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppDetail app = new AppDetail();
            app.setmLabel(ri.loadLabel(manager));
            app.setmName(ri.activityInfo.packageName);
            app.setmIcon(ri.activityInfo.loadIcon(manager));
            app.setmNumberOfStarts(SharedPrefs.getNumberOfActivityStarts(app.getmLabel().toString(), this));
            if(ri.loadLabel(manager).toString().equalsIgnoreCase("Settings")) {
                mIconSide = ri.activityInfo.loadIcon(manager).getIntrinsicWidth();
                Log.i("icon side", String.valueOf(mIconSide));
            }
            if(SharedPrefs.getAppVisible(this, (String) ri.loadLabel(manager))) {
                mApps.add(app);
            }
        }
        mApps = AppsSorter.sortApps(this,mApps, "most used", false);
    }

    private void loadListView(){
        ListView list = (ListView) findViewById(R.id.set_icons_list);

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
                appIcon.setImageDrawable(new BitmapDrawable(ChangeIconsActivity.this.getResources(), icon));

                TextView appLabel = (TextView)convertView.findViewById(R.id.visible_app_label);
                appLabel.setText(mApps.get(position).getmLabel());

                CheckBox visible = (CheckBox)convertView.findViewById(R.id.toggle_visible);
                visible.setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAppName = mApps.get(position).getmLabel().toString();
                        if(mOption.equalsIgnoreCase("gallery")) {
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(i, RESULT_LOAD_IMAGE);
                        } else {
                            if(NetworkConnectivityChecker.isNetworkAvailable(ChangeIconsActivity.this)) {
                                Intent i = new Intent(ChangeIconsActivity.this, GetWebImageActivity.class);
                                i.putExtra("label",mApps.get(position).getmLabel());
                                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                startActivity(i);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                            } else {
                                Toast.makeText(ChangeIconsActivity.this, "Please check your internet connection",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            Log.i("picture path", picturePath);
            cursor.close();
            ListView listView = (ListView) findViewById(R.id.set_icons_list);
            FrameLayout imageViewFrame = (FrameLayout) findViewById(R.id.image_frame);
            mImageView = (ImageView) findViewById(R.id.image_view);
            int orientation = 1;
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            try {
                ExifInterface exif = new ExifInterface(picturePath);
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.i("orientation", String.valueOf(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)));
            } catch (IOException ignored) {

            }
            switch(orientation) {
                case 3:
                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    break;
                case 6:
                    matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    break;
                case 8:
                    matrix = new Matrix();
                    matrix.postRotate(270);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    break;
            }
            int bitmapWidth;
            int bitmapHeight;
            if(bitmap != null) {
                bitmapWidth = bitmap.getWidth();
                bitmapHeight = bitmap.getHeight();
            } else {
                bitmapWidth = 0;
                bitmapHeight = 0;
            }

            if(bitmapHeight < 200 || bitmapHeight < 200) {
                if(bitmapHeight == 0) {
                    Toast.makeText(ChangeIconsActivity.this, "This image cannot be loaded",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChangeIconsActivity.this, "Selected image is too small",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                listView.setVisibility(View.GONE);
                Button applyButton = (Button) findViewById(R.id.apply_image);
                applyButton.setVisibility(View.VISIBLE);
                imageViewFrame.setVisibility(View.VISIBLE);
                int resizeFactor;
                if(bitmapWidth > bitmapHeight && bitmapWidth > 800) {
                    resizeFactor = bitmapWidth / 800;
                } else if (bitmapHeight > 1200){
                    resizeFactor = bitmapHeight / 1200;
                } else {
                    resizeFactor = 1;
                }
                mImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmapWidth / resizeFactor, bitmapHeight / resizeFactor, false) );

            }
        }
    }

    public void saveIconToLocalStorage(View view) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
        Bitmap bitmap = Bitmap.createBitmap(bitmapDrawable.getBitmap(), mMinX, mMinY, mMaxX - mMinX, mMaxY - mMinY);
        IconLoader.saveIcon(this,bitmap, mAppName);
        SharedPrefs.setHomeReloadRequired(true,this);
        SharedPrefs.setNonDefaultIconsCount(SharedPrefs.getNonDefaultIconsCount(this) + 1, this);
        recreate();
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTopLeftButtonParams = new FrameLayout.LayoutParams(mTopLeft.getLayoutParams());
            mTopRightButtonParams = new FrameLayout.LayoutParams(mTopRight.getLayoutParams());
            mBottomLeftButtonParams = new FrameLayout.LayoutParams(mBottomLeft.getLayoutParams());
            mBottomRightButtonParams = new FrameLayout.LayoutParams(mBottomRight.getLayoutParams());

            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    mTag = view.getTag().toString();
                    mStartX = (int) (view.getX() - motionEvent.getRawX());
                    mStartY = (int) (view.getY() - motionEvent.getRawY());
                    break;

                case MotionEvent.ACTION_MOVE:
                    Log.i("x, y", motionEvent.getRawX() + mStartX + ", " + motionEvent.getRawY() + mStartY);
                    switch (mTag) {
                        case "top_left":
                            if (motionEvent.getRawX() + mStartX > 0 && motionEvent.getRawX() + mStartX < mMaxX - mButtonSide * 2 - 1
                                    && motionEvent.getRawY() + mStartY > 0 && motionEvent.getRawY() + mStartY < mMaxY - mButtonSide * 2 - 1) {
                                mMinY = (int) motionEvent.getRawY() + mStartY;
                                mMinX = (int) motionEvent.getRawX() + mStartX;

                                mPreviousTopLeftX = (int) motionEvent.getRawX() + mStartX;
                                mPreviousBottomLeftX = (int) motionEvent.getRawX() + mStartX;
                                mPreviousTopLeftY = (int) motionEvent.getRawY() + mStartY;
                                mPreviousTopRightY = (int) motionEvent.getRawY() + mStartY;
                            }

                            break;
                        case "top_right":
                            if (motionEvent.getRawX() + mStartX > mMinX + mButtonSide && motionEvent.getRawX() + mStartX < mWidth - mButtonSide - 1
                                    && motionEvent.getRawY() + mStartY > 0 && motionEvent.getRawY() + mStartY < mMaxY - mButtonSide) {
                                mMinY = (int) motionEvent.getRawY() + mStartY;
                                mMaxX = (int) motionEvent.getRawX() + mStartX + mButtonSide;

                                mPreviousTopRightX = (int) motionEvent.getRawX() + mStartX;
                                mPreviousTopLeftY = (int) motionEvent.getRawY() + mStartY;
                                mPreviousTopRightY = (int) motionEvent.getRawY() + mStartY;
                                mPreviousBottomRightX = (int) motionEvent.getRawX() + mStartX;
                            }

                            break;
                        case "bottom_left":
                            if (motionEvent.getRawX() + mStartX > 0 && motionEvent.getRawX() + mStartX < mMaxX - mButtonSide * 2 - 1
                                    && motionEvent.getRawY() + mStartY > mMinY + mButtonSide && motionEvent.getRawY() + mStartY < mHeight - mButtonSide - 1) {
                                mMinX = (int) motionEvent.getRawX() + mStartX;
                                mMaxY = (int) motionEvent.getRawY() + mStartY + mButtonSide;

                                mPreviousTopLeftX = (int) motionEvent.getRawX() + mStartX;
                                mPreviousBottomLeftX = (int) motionEvent.getRawX() + mStartX;
                                mPreviousBottomLeftY = (int) motionEvent.getRawY() + mStartY;
                                mPreviousBottomRightY = (int) motionEvent.getRawY() + mStartY;
                            }
                            break;
                        case "bottom_right":
                            if (motionEvent.getRawX() + mStartX > mMinX + mButtonSide && motionEvent.getRawX() + mStartX < mWidth - mButtonSide - 1
                                    && motionEvent.getRawY() + mStartY > mMinY + mButtonSide && motionEvent.getRawY() + mStartY < mHeight - mButtonSide - 1) {
                                mMaxX = (int) motionEvent.getRawX() + mStartX + mButtonSide;
                                mMaxY = (int) motionEvent.getRawY() + mStartY + mButtonSide;

                                mPreviousTopRightX = (int) motionEvent.getRawX() + mStartX;
                                mPreviousBottomLeftY = (int) motionEvent.getRawY() + mStartY;
                                mPreviousBottomRightY = (int) motionEvent.getRawY() + mStartY;
                                mPreviousBottomRightX = (int) motionEvent.getRawX() + mStartX;
                            }
                            break;
                    }
                    repaintCropButtons();
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private void repaintCropButtons() {
        mTopLeftButtonParams.setMargins(mPreviousTopLeftX, mPreviousTopLeftY,0,0);
        mTopLeft.setLayoutParams(mTopLeftButtonParams);
        mTopLeft.setVisibility(View.GONE);
        mTopLeft.setVisibility(View.VISIBLE);

        mTopRightButtonParams.setMargins(mPreviousTopRightX, mPreviousTopRightY,0,0);
        mTopRight.setLayoutParams(mTopRightButtonParams);
        mTopRight.setVisibility(View.GONE);
        mTopRight.setVisibility(View.VISIBLE);

        mBottomRightButtonParams.setMargins(mPreviousBottomRightX, mPreviousBottomRightY,0,0);
        mBottomRight.setLayoutParams(mBottomRightButtonParams);
        mBottomRight.setVisibility(View.GONE);
        mBottomRight.setVisibility(View.VISIBLE);

        mBottomLeftButtonParams.setMargins(mPreviousBottomLeftX, mPreviousBottomLeftY,0,0);
        mBottomLeft.setLayoutParams(mBottomLeftButtonParams);
        mBottomLeft.setVisibility(View.GONE);
        mBottomLeft.setVisibility(View.VISIBLE);


        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.crop_background);
        ImageView croppedBackground = (ImageView) findViewById(R.id.cropped_background);
        croppedBackground.setBackground(new BitmapDrawable(this.getResources(), Bitmap.createScaledBitmap(image, mMaxX - mMinX, mMaxY - mMinY, true)));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(croppedBackground.getLayoutParams());
        params.leftMargin = mMinX;
        params.topMargin = mMinY;
        params.width = mMaxX - mMinX;
        params.height = mMaxY - mMinY;
        croppedBackground.setLayoutParams(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOption = getIntent().getExtras().getString("option");
        Log.i("option", mOption);
        setContentView(R.layout.activity_change_icons);
        mMinX = 0;
        mMinY = 0;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        View someView = findViewById(R.id.set_icons_container);
        View root = someView.getRootView();
        root.setBackgroundColor(0xC5CAE9);
        Toolbar toolbar = (Toolbar) findViewById(R.id.set_icons_toolbar);
        toolbar.setTitle("Set Icons");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        final FrameLayout imageView = (FrameLayout) findViewById(R.id.image_frame);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView
                .getLayoutParams();

        layoutParams.setMargins(0, mActionBarHeight, 0, 0);

        imageView.setLayoutParams(layoutParams);
        final ImageView parent = (ImageView) findViewById(R.id.image_view);
        parent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                mButtonSide = 0;
                if(screenWidth <= 240) {
                    mButtonSide = 20;
                } else if(screenWidth <= 320) {
                    mButtonSide = 24;
                } else if(screenWidth <= 480) {
                    mButtonSide = 36;
                } else if(screenWidth <= 768) {
                    mButtonSide = 48;
                } else if(screenWidth <= 1080) {
                    mButtonSide = 72;
                } else {
                    mButtonSide = 96;
                }
                mHeight = parent.getMeasuredHeight();
                mWidth = parent.getMeasuredWidth();

                mMaxY = mHeight;
                mMaxX = mWidth;

                if(mHeight>0) {
                    parent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.i("height", String.valueOf(mHeight));
                    Log.i("width", String.valueOf(mWidth));

                    layoutParams.setMargins((screenWidth - mWidth)/2, mActionBarHeight, (screenWidth - mWidth)/2, 0);

                    imageView.setLayoutParams(layoutParams);
                    ImageView cropped_background = (ImageView) findViewById(R.id.cropped_background);
                    cropped_background.setLayoutParams(new FrameLayout.LayoutParams(mWidth, mHeight));

                    mTopLeft = (Button) findViewById(R.id.top_left_crop);
                    mTopLeft.setVisibility(View.VISIBLE);
                    mTopLeft.getLayoutParams().height = mButtonSide;
                    mTopLeft.getLayoutParams().width = mButtonSide;
                    mTopLeft.setOnTouchListener(new MyTouchListener());
                    mPreviousTopLeftX = 0;
                    mPreviousTopLeftY = 0;

                    Drawable dr = getResources().getDrawable(R.mipmap.crop_top_left, null);
                    Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                    Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, mButtonSide, mButtonSide, true));
                    mTopLeft.setBackground(d);

                    mTopRight = (Button) findViewById(R.id.top_right_crop);
                    mTopRight.setVisibility(View.VISIBLE);
                    mTopRight.getLayoutParams().height = mButtonSide;
                    mTopRight.getLayoutParams().width = mButtonSide;
                    mPreviousTopRightX = mWidth- mButtonSide -1;
                    mPreviousTopRightY = 0;
                    FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(mTopRight.getLayoutParams());
                    buttonParams.setMargins(mWidth- mButtonSide -1,0,0,0);
                    mTopRight.setLayoutParams(buttonParams);
                    mTopRight.setOnTouchListener(new MyTouchListener());

                    dr = getResources().getDrawable(R.mipmap.crop_top_right, null);
                    bitmap = ((BitmapDrawable) dr).getBitmap();
                    d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, mButtonSide, mButtonSide, true));
                    mTopRight.setBackground(d);

                    mBottomLeft = (Button) findViewById(R.id.left_bottom_crop);
                    mBottomLeft.setVisibility(View.VISIBLE);
                    mBottomLeft.getLayoutParams().height = mButtonSide;
                    mBottomLeft.getLayoutParams().width = mButtonSide;
                    buttonParams = new FrameLayout.LayoutParams(mBottomLeft.getLayoutParams());
                    buttonParams.setMargins(0,mHeight- mButtonSide,0,0);
                    mBottomLeft.setLayoutParams(buttonParams);
                    mBottomLeft.setOnTouchListener(new MyTouchListener());
                    mPreviousBottomLeftX = 0;
                    mPreviousBottomLeftY = mHeight- mButtonSide -1;


                    dr = getResources().getDrawable(R.mipmap.crop_bottom_left, null);
                    bitmap = ((BitmapDrawable) dr).getBitmap();
                    d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, mButtonSide, mButtonSide, true));
                    mBottomLeft.setBackground(d);


                    mBottomRight = (Button) findViewById(R.id.bottom_right_crop);
                    mBottomRight.setVisibility(View.VISIBLE);
                    mBottomRight.getLayoutParams().height = mButtonSide;
                    mBottomRight.getLayoutParams().width = mButtonSide;
                    buttonParams = new FrameLayout.LayoutParams(mBottomRight.getLayoutParams());
                    buttonParams.setMargins(mWidth - mButtonSide,mHeight- mButtonSide,0,0);
                    mBottomRight.setLayoutParams(buttonParams);
                    mBottomRight.setOnTouchListener(new MyTouchListener());
                    mPreviousBottomRightX = mWidth - mButtonSide - 1;
                    mPreviousBottomRightY = mHeight - mButtonSide - 1;


                    dr = getResources().getDrawable(R.mipmap.crop_bottom_right, null);
                    bitmap = ((BitmapDrawable) dr).getBitmap();
                    d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, mButtonSide, mButtonSide, true));
                    mBottomRight.setBackground(d);

                }
            }
        });
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
