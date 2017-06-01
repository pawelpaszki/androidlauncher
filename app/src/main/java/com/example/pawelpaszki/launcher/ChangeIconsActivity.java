package com.example.pawelpaszki.launcher;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import com.example.pawelpaszki.launcher.utils.IconLoader;
import com.example.pawelpaszki.launcher.utils.SharedPrefs;

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

    private ListView list;
    private void loadListView(){
        list = (ListView)findViewById(R.id.set_icons_list);

        ArrayAdapter<AppDetail> adapter = new ArrayAdapter<AppDetail>(this,
                R.layout.visible_app_item,
                apps) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.visible_app_item, null);
                }

                String path = this.getContext().getFilesDir().getAbsolutePath();
                final int index = position;
                //Bitmap icon = IconLoader.loadImageFromStorage(path, (String) apps.get(position).getLabel());
                Bitmap icon  = ((BitmapDrawable) apps.get(position).getIcon()).getBitmap();

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.visible_app_icon);

                appIcon.setImageDrawable(new BitmapDrawable(ChangeIconsActivity.this.getResources(), icon));

                TextView appLabel = (TextView)convertView.findViewById(R.id.visible_app_label);
                appLabel.setText(apps.get(position).getLabel());

                CheckBox visible = (CheckBox)convertView.findViewById(R.id.toggle_visible);
                visible.setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appName = apps.get(index).getLabel().toString();
                        Intent i = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        startActivityForResult(i, RESULT_LOAD_IMAGE);
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
            ImageView imageView = (ImageView) findViewById(R.id.image_view);

            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if(bitmapHeight < 200 || bitmapHeight < 200) {
                Toast.makeText(ChangeIconsActivity.this, "Selected image is too small",
                        Toast.LENGTH_SHORT).show();
            } else {
                listView.setVisibility(View.GONE);
                imageViewFrame.setVisibility(View.VISIBLE);
                int resizeFactor;
                if(bitmapWidth > bitmapHeight) {
                    resizeFactor = bitmapWidth / 800;
                } else {
                    resizeFactor = bitmapHeight / 1200;
                }
                imageView.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeFile(picturePath), bitmapWidth / resizeFactor, bitmapHeight / resizeFactor, false) );

            }
        }
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
                    boolean canMove = false;
                    if(tag.equals("top_left")) {
                        if(motionEvent.getRawX() + startX > 0 && motionEvent.getRawX() + startX < maxX - buttonSide * 2 -1
                                && motionEvent.getRawY() + startY > 0 && motionEvent.getRawY() + startY < maxY - buttonSide * 2 -1) {
                            canMove = true;
                            previousTopLeftX = (int)motionEvent.getRawX() + startX;
                            previousBottomLeftX = (int)motionEvent.getRawX() + startX;
                            previousTopLeftY = (int)motionEvent.getRawY() + startY;
                            previousTopRightY = (int)motionEvent.getRawY() + startY;

                            topLeftButtonParams.setMargins((int)motionEvent.getRawX() + startX, (int)motionEvent.getRawY() + startY,0,0);
                            minX = (int)motionEvent.getRawX() + startX;
                            minY = (int)motionEvent.getRawY() + startY;

                            topRightButtonParams.topMargin = (int)motionEvent.getRawY() + startY;
                            topRightButtonParams.leftMargin = previousTopRightX;
                            topRight.setLayoutParams(topRightButtonParams);
                            topRight.setVisibility(View.GONE);
                            topRight.setVisibility(View.VISIBLE);

                            bottomLeftButtonParams.leftMargin = (int)motionEvent.getRawX() + startX;
                            bottomLeftButtonParams.topMargin = previousBottomLeftY;
                            bottomLeft.setLayoutParams(bottomLeftButtonParams);
                            bottomLeft.setVisibility(View.GONE);
                            bottomLeft.setVisibility(View.VISIBLE);


                            topLeft.setVisibility(View.GONE);
                            topLeft.setVisibility(View.VISIBLE);
                            topLeft.setLayoutParams(topLeftButtonParams);
                        }

                    } else if (tag.equals("top_right")) {
                        if(motionEvent.getRawX() + startX > minX + buttonSide * 2 && motionEvent.getRawX() + startX < width - buttonSide  -1
                                && motionEvent.getRawY() + startY > 0 && motionEvent.getRawY() + startY < height - buttonSide * 2 -1) {
                            canMove = true;
                            topRightButtonParams.setMargins((int)motionEvent.getRawX() + startX, (int)motionEvent.getRawY() + startY,0,0);
                            previousTopRightX = (int)motionEvent.getRawX() + startX;
                            maxX = (int)motionEvent.getRawX() + startX + buttonSide;
                            minY = (int)motionEvent.getRawY() + startY;
                            topLeftButtonParams.topMargin = (int)motionEvent.getRawY() + startY;
                            topLeftButtonParams.leftMargin = previousTopLeftX;
                            topLeft.setLayoutParams(topLeftButtonParams);
                            topRight.setLayoutParams(topRightButtonParams);
                            topRight.setVisibility(View.GONE);
                            topRight.setVisibility(View.VISIBLE);
                            topLeft.setVisibility(View.GONE);
                            topLeft.setVisibility(View.VISIBLE);

                        }

                    } else if (tag.equals("bottom_left")) {
                        if(motionEvent.getRawX() + startX > 0 && motionEvent.getRawX() + startX < maxX - buttonSide * 2 -1
                                && motionEvent.getRawY() + startY > minY + buttonSide*2 && motionEvent.getRawY() + startY < height - buttonSide -1) {
                            minX = (int)motionEvent.getRawX() + startX;
                            maxY = (int)motionEvent.getRawY() + startY + buttonSide;
                            previousTopLeftX = (int)motionEvent.getRawX() + startX;
                            previousBottomLeftX = (int)motionEvent.getRawX() + startX;
                            previousBottomLeftY = (int)motionEvent.getRawY() + startY;
                            previousBottomRightY = (int)motionEvent.getRawY() + startY;

                            topLeftButtonParams.setMargins(previousTopLeftX, previousTopLeftY,0,0);
                            topLeft.setLayoutParams(topLeftButtonParams);
                            topLeft.setVisibility(View.GONE);
                            topLeft.setVisibility(View.VISIBLE);


                            bottomLeftButtonParams.leftMargin = previousBottomLeftX;
                            bottomLeftButtonParams.topMargin = previousBottomLeftY;
                            bottomLeft.setLayoutParams(bottomLeftButtonParams);
                            bottomLeft.setVisibility(View.GONE);
                            bottomLeft.setVisibility(View.VISIBLE);

                            bottomRightButtonParams.leftMargin = previousBottomRightX;
                            bottomRightButtonParams.topMargin = previousBottomRightY;
                            bottomRight.setLayoutParams(bottomRightButtonParams);
                            bottomRight.setVisibility(View.GONE);
                            bottomRight.setVisibility(View.VISIBLE);

                        }
                    } else if (tag.equals("bottom_right")) {

                    }

                    Log.i("view's top margin", String.valueOf(topLeftButtonParams.topMargin));
                    break;
                default:
                    return false;
            }
            return true;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                int screenHeight = displayMetrics.heightPixels;
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
