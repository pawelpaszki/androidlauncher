package com.example.pawelpaszki.launcher;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawelpaszki.launcher.utils.AppsSorter;
import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.NetworkConnectivityChecker;
import com.example.pawelpaszki.launcher.utils.RoundBitmapGenerator;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangeIconsActivity extends AppCompatActivity {

    private PackageManager manager;
    private List<AppDetail> apps;
    private static int RESULT_LOAD_IMAGE = 1;
    private String appName;
    private int actionBarHeight;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private int startX;
    private int startY;
    private int buttonSide;
    private String tag;
    private Button topLeft;
    private Button topRight;
    private Button bottomLeft;
    private Button bottomRight;
    private int x;
    private int y;
    private FrameLayout.LayoutParams topLeftButtonParams;
    private FrameLayout.LayoutParams topRightButtonParams;
    private FrameLayout.LayoutParams bottomLeftButtonParams;
    private FrameLayout.LayoutParams bottomRightButtonParams;

    private int height;
    private int width;
    private int previousTopLeftX;
    private int previousTopRightX;
    private int previousTopLeftY;
    private int previousTopRightY;
    private int previousBottomLeftX;
    private int previousBottomLeftY;
    private int previousBottomRightX;
    private int previousBottomRightY;
    private ImageView imageView;
    private int iconSide;
    private String option;

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
            if(ri.loadLabel(manager).toString().equalsIgnoreCase("Settings")) {
                iconSide = ri.activityInfo.loadIcon(manager).getIntrinsicWidth();
                Log.i("icon side", String.valueOf(iconSide));
            }
            if(SharedPrefs.getAppVisible(this, (String) ri.loadLabel(manager))) {
                apps.add(app);
            }
        }
        apps = AppsSorter.sortApps(this,apps, "most used", false);
    }

    private ListView list;
    private void loadListView(){
        list = (ListView)findViewById(R.id.set_icons_list);

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
                appIcon.setImageDrawable(new BitmapDrawable(ChangeIconsActivity.this.getResources(), icon));

                TextView appLabel = (TextView)convertView.findViewById(R.id.visible_app_label);
                appLabel.setText(apps.get(position).getLabel());

                CheckBox visible = (CheckBox)convertView.findViewById(R.id.toggle_visible);
                visible.setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appName = apps.get(index).getLabel().toString();
                        if(option.equalsIgnoreCase("gallery")) {
                            Intent i = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                            startActivityForResult(i, RESULT_LOAD_IMAGE);
                        } else {
                            if(NetworkConnectivityChecker.isNetworkAvailable(ChangeIconsActivity.this)) {
                                Intent i = new Intent(ChangeIconsActivity.this, LoadWebIconActivity.class);
                                i.putExtra("label",apps.get(position).getLabel());
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
            imageView = (ImageView) findViewById(R.id.image_view);
            int orientation = 1;
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            try {
                ExifInterface exif = new ExifInterface(picturePath);
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.i("orientation", String.valueOf(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)));
            } catch (IOException e) {

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
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if(bitmapHeight < 200 || bitmapHeight < 200) {
                Toast.makeText(ChangeIconsActivity.this, "Selected image is too small",
                        Toast.LENGTH_SHORT).show();
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
                imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmapWidth / resizeFactor, bitmapHeight / resizeFactor, false) );

            }
        }
    }

    public void saveIconToLocalStorage(View view) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = Bitmap.createBitmap(bitmapDrawable.getBitmap(),minX,minY,maxX-minX,maxY-minY);
        IconLoader.saveIcon(this,bitmap,appName);
        SharedPrefs.setHomeReloadRequired(true,this);
        recreate();
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            topLeftButtonParams = new FrameLayout.LayoutParams(topLeft.getLayoutParams());
            topRightButtonParams = new FrameLayout.LayoutParams(topRight.getLayoutParams());
            bottomLeftButtonParams = new FrameLayout.LayoutParams(bottomLeft.getLayoutParams());
            bottomRightButtonParams = new FrameLayout.LayoutParams(bottomRight.getLayoutParams());

            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    tag = view.getTag().toString();
                    startX = (int) (view.getX() - motionEvent.getRawX());
                    startY = (int) (view.getY() - motionEvent.getRawY());
                    break;

                case MotionEvent.ACTION_MOVE:
                    Log.i("x, y", motionEvent.getRawX() + startX + ", " + motionEvent.getRawY() + startY);
                    if(tag.equals("top_left")) {
                        if(motionEvent.getRawX() + startX > 0 && motionEvent.getRawX() + startX < maxX - buttonSide * 2 -1
                                && motionEvent.getRawY() + startY > 0 && motionEvent.getRawY() + startY < maxY - buttonSide * 2 -1) {
                            minY = (int)motionEvent.getRawY() + startY;
                            minX = (int)motionEvent.getRawX() + startX;

                            previousTopLeftX = (int)motionEvent.getRawX() + startX;
                            previousBottomLeftX = (int)motionEvent.getRawX() + startX;
                            previousTopLeftY = (int)motionEvent.getRawY() + startY;
                            previousTopRightY = (int)motionEvent.getRawY() + startY;
                        }

                    } else if (tag.equals("top_right")) {
                        if(motionEvent.getRawX() + startX > minX + buttonSide && motionEvent.getRawX() + startX < width - buttonSide  -1
                                && motionEvent.getRawY() + startY > 0 && motionEvent.getRawY() + startY < maxY - buttonSide) {
                            minY = (int)motionEvent.getRawY() + startY;
                            maxX = (int)motionEvent.getRawX() + startX + buttonSide;

                            previousTopRightX = (int)motionEvent.getRawX() + startX;
                            previousTopLeftY = (int)motionEvent.getRawY() + startY;
                            previousTopRightY = (int)motionEvent.getRawY() + startY;
                            previousBottomRightX = (int)motionEvent.getRawX() + startX;
                        }

                    } else if (tag.equals("bottom_left")) {
                        if(motionEvent.getRawX() + startX > 0 && motionEvent.getRawX() + startX < maxX - buttonSide *2 -1
                                && motionEvent.getRawY() + startY > minY + buttonSide && motionEvent.getRawY() + startY < height - buttonSide -1) {
                            minX = (int)motionEvent.getRawX() + startX;
                            maxY = (int)motionEvent.getRawY() + startY + buttonSide;

                            previousTopLeftX = (int)motionEvent.getRawX() + startX;
                            previousBottomLeftX = (int)motionEvent.getRawX() + startX;
                            previousBottomLeftY = (int)motionEvent.getRawY() + startY;
                            previousBottomRightY = (int)motionEvent.getRawY() + startY;
                        }
                    } else if (tag.equals("bottom_right")) {
                        if(motionEvent.getRawX() + startX > minX + buttonSide && motionEvent.getRawX() + startX < width - buttonSide  -1
                                && motionEvent.getRawY() + startY > minY + buttonSide && motionEvent.getRawY() + startY < height - buttonSide -1) {
                            maxX = (int)motionEvent.getRawX() + startX + buttonSide;
                            maxY = (int)motionEvent.getRawY() + startY + buttonSide;

                            previousTopRightX = (int)motionEvent.getRawX() + startX;
                            previousBottomLeftY = (int)motionEvent.getRawY() + startY;
                            previousBottomRightY = (int)motionEvent.getRawY() + startY;
                            previousBottomRightX = (int)motionEvent.getRawX() + startX;
                        }
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
        topLeftButtonParams.setMargins(previousTopLeftX, previousTopLeftY,0,0);
        topLeft.setLayoutParams(topLeftButtonParams);
        topLeft.setVisibility(View.GONE);
        topLeft.setVisibility(View.VISIBLE);

        topRightButtonParams.setMargins(previousTopRightX,previousTopRightY,0,0);
        topRight.setLayoutParams(topRightButtonParams);
        topRight.setVisibility(View.GONE);
        topRight.setVisibility(View.VISIBLE);

        bottomRightButtonParams.setMargins(previousBottomRightX,previousBottomRightY,0,0);
        bottomRight.setLayoutParams(bottomRightButtonParams);
        bottomRight.setVisibility(View.GONE);
        bottomRight.setVisibility(View.VISIBLE);

        bottomLeftButtonParams.setMargins(previousBottomLeftX,previousBottomLeftY,0,0);
        bottomLeft.setLayoutParams(bottomLeftButtonParams);
        bottomLeft.setVisibility(View.GONE);
        bottomLeft.setVisibility(View.VISIBLE);


        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.crop_background);
        ImageView croppedBackground = (ImageView) findViewById(R.id.cropped_background);
        croppedBackground.setBackground(new BitmapDrawable(this.getResources(), Bitmap.createScaledBitmap(image, maxX - minX, maxY - minY, true)));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(croppedBackground.getLayoutParams());
        params.leftMargin = minX;
        params.topMargin = minY;
        params.width = maxX - minX;
        params.height = maxY - minY;
        croppedBackground.setLayoutParams(params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        option = getIntent().getExtras().getString("option");
        Log.i("option", option);
        setContentView(R.layout.activity_change_icons);
        minX = 0;
        minY = 0;
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
        actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        final FrameLayout imageView = (FrameLayout) findViewById(R.id.image_frame);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView
                .getLayoutParams();

        layoutParams.setMargins(0, actionBarHeight, 0, 0);

        imageView.setLayoutParams(layoutParams);
        final ImageView parent = (ImageView) findViewById(R.id.image_view);
        parent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                buttonSide = 0;
                if(screenWidth <= 240) {
                    buttonSide = 20;
                } else if(screenWidth <= 320) {
                    buttonSide = 24;
                } else if(screenWidth <= 480) {
                    buttonSide = 36;
                } else if(screenWidth <= 768) {
                    buttonSide = 48;
                } else if(screenWidth <= 1080) {
                    buttonSide = 72;
                } else {
                    buttonSide = 96;
                }
                height = parent.getMeasuredHeight();
                width = parent.getMeasuredWidth();

                maxY = height;
                maxX = width;

                if(height>0) {
                    parent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.i("height", String.valueOf(height));
                    Log.i("width", String.valueOf(width));

                    layoutParams.setMargins((screenWidth - width)/2, actionBarHeight, (screenWidth - width)/2, 0);

                    imageView.setLayoutParams(layoutParams);
                    ImageView cropped_background = (ImageView) findViewById(R.id.cropped_background);
                    cropped_background.setLayoutParams(new FrameLayout.LayoutParams(width, height));

                    topLeft = (Button) findViewById(R.id.top_left_crop);
                    topLeft.setVisibility(View.VISIBLE);
                    topLeft.getLayoutParams().height = buttonSide;
                    topLeft.getLayoutParams().width = buttonSide;
                    topLeft.setOnTouchListener(new MyTouchListener());
                    previousTopLeftX = 0;
                    previousTopLeftY = 0;

                    Drawable dr = getResources().getDrawable(R.mipmap.crop_top_left, null);
                    Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                    Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, buttonSide, buttonSide, true));
                    topLeft.setBackground(d);

                    topRight = (Button) findViewById(R.id.top_right_crop);
                    topRight.setVisibility(View.VISIBLE);
                    topRight.getLayoutParams().height = buttonSide;
                    topRight.getLayoutParams().width = buttonSide;
                    previousTopRightX = width-buttonSide-1;
                    previousTopRightY = 0;
                    FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(topRight.getLayoutParams());
                    buttonParams.setMargins(width-buttonSide-1,0,0,0);
                    topRight.setLayoutParams(buttonParams);
                    topRight.setOnTouchListener(new MyTouchListener());

                    dr = getResources().getDrawable(R.mipmap.crop_top_right, null);
                    bitmap = ((BitmapDrawable) dr).getBitmap();
                    d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, buttonSide, buttonSide, true));
                    topRight.setBackground(d);

                    bottomLeft = (Button) findViewById(R.id.left_bottom_crop);
                    bottomLeft.setVisibility(View.VISIBLE);
                    bottomLeft.getLayoutParams().height = buttonSide;
                    bottomLeft.getLayoutParams().width = buttonSide;
                    buttonParams = new FrameLayout.LayoutParams(bottomLeft.getLayoutParams());
                    buttonParams.setMargins(0,height-buttonSide,0,0);
                    bottomLeft.setLayoutParams(buttonParams);
                    bottomLeft.setOnTouchListener(new MyTouchListener());
                    previousBottomLeftX = 0;
                    previousBottomLeftY = height-buttonSide -1;


                    dr = getResources().getDrawable(R.mipmap.crop_bottom_left, null);
                    bitmap = ((BitmapDrawable) dr).getBitmap();
                    d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, buttonSide, buttonSide, true));
                    bottomLeft.setBackground(d);


                    bottomRight = (Button) findViewById(R.id.bottom_right_crop);
                    bottomRight.setVisibility(View.VISIBLE);
                    bottomRight.getLayoutParams().height = buttonSide;
                    bottomRight.getLayoutParams().width = buttonSide;
                    buttonParams = new FrameLayout.LayoutParams(bottomRight.getLayoutParams());
                    buttonParams.setMargins(width -buttonSide,height-buttonSide,0,0);
                    bottomRight.setLayoutParams(buttonParams);
                    bottomRight.setOnTouchListener(new MyTouchListener());
                    previousBottomRightX = width -buttonSide - 1;
                    previousBottomRightY = height - buttonSide - 1;


                    dr = getResources().getDrawable(R.mipmap.crop_bottom_right, null);
                    bitmap = ((BitmapDrawable) dr).getBitmap();
                    d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, buttonSide, buttonSide, true));
                    bottomRight.setBackground(d);

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
