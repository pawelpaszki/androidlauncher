package com.example.pawelpaszki.launcher;

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
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
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
                        Toast.makeText(ChangeIconsActivity.this, apps.get(index).getLabel().toString(),
                        Toast.LENGTH_LONG).show();
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
            listView.setVisibility(View.GONE);
            imageViewFrame.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            ImageView cropped_background = (ImageView) findViewById(R.id.cropped_background);
            Drawable drawable = imageView.getDrawable();
            drawable.mutate().setColorFilter( 0x500000FF, PorterDuff.Mode.MULTIPLY);
            cropped_background.setImageDrawable(drawable);
            cropped_background.requestLayout();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_icons);
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

                int height = parent.getMeasuredHeight();
                int width = parent.getMeasuredWidth();
                if(height>0) {
                    parent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.i("height", String.valueOf(height));
                    Log.i("width", String.valueOf(width));

                    layoutParams.setMargins((screenWidth - width)/2, actionBarHeight, (screenWidth - width)/2, 0);

                    imageView.setLayoutParams(layoutParams);

                    Button topLeft = (Button) findViewById(R.id.top_left_crop);
                    topLeft.setVisibility(View.VISIBLE);
                    topLeft.getLayoutParams().height = 30;
                    topLeft.getLayoutParams().width = 30;

                    Button topRight = (Button) findViewById(R.id.top_right_crop);
                    topRight.setVisibility(View.VISIBLE);
                    FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(topRight.getLayoutParams());
                    buttonParams.setMargins(width-30,0,0,0);
                    topRight.setLayoutParams(buttonParams);
                    topRight.getLayoutParams().height = 30;
                    topRight.getLayoutParams().width = 30;

                    Button topCenter = (Button) findViewById(R.id.top_center_crop);
                    topCenter.setVisibility(View.VISIBLE);
                    buttonParams = new FrameLayout.LayoutParams(topCenter.getLayoutParams());
                    buttonParams.setMargins(width / 2 - 15,0,0,0);
                    topCenter.setLayoutParams(buttonParams);
                    topCenter.getLayoutParams().height = 30;
                    topCenter.getLayoutParams().width = 30;

                    Button leftCenter = (Button) findViewById(R.id.left_center_crop);
                    leftCenter.setVisibility(View.VISIBLE);
                    buttonParams = new FrameLayout.LayoutParams(leftCenter.getLayoutParams());
                    buttonParams.setMargins(0,height / 2 - 15,0,0);
                    leftCenter.setLayoutParams(buttonParams);
                    leftCenter.getLayoutParams().height = 30;
                    leftCenter.getLayoutParams().width = 30;

                    Button leftBottom = (Button) findViewById(R.id.left_bottom_crop);
                    leftBottom.setVisibility(View.VISIBLE);
                    buttonParams = new FrameLayout.LayoutParams(leftBottom.getLayoutParams());
                    buttonParams.setMargins(0,height-30,0,0);
                    leftBottom.setLayoutParams(buttonParams);
                    leftBottom.getLayoutParams().height = 30;
                    leftBottom.getLayoutParams().width = 30;

                    Button bottomCenter = (Button) findViewById(R.id.bottom_center_crop);
                    bottomCenter.setVisibility(View.VISIBLE);
                    buttonParams = new FrameLayout.LayoutParams(bottomCenter.getLayoutParams());
                    buttonParams.setMargins(width / 2 - 15,height-30,0,0);
                    bottomCenter.setLayoutParams(buttonParams);
                    bottomCenter.getLayoutParams().height = 30;
                    bottomCenter.getLayoutParams().width = 30;


                    Button rightCenter = (Button) findViewById(R.id.right_center_crop);
                    rightCenter.setVisibility(View.VISIBLE);
                    buttonParams = new FrameLayout.LayoutParams(rightCenter.getLayoutParams());
                    buttonParams.setMargins(width -30,height/2-15,0,0);
                    rightCenter.setLayoutParams(buttonParams);
                    rightCenter.getLayoutParams().height = 30;
                    rightCenter.getLayoutParams().width = 30;

                    Button bottomRight = (Button) findViewById(R.id.bottom_right_crop);
                    bottomRight.setVisibility(View.VISIBLE);
                    buttonParams = new FrameLayout.LayoutParams(bottomRight.getLayoutParams());
                    buttonParams.setMargins(width -30,height-30,0,0);
                    bottomRight.setLayoutParams(buttonParams);
                    bottomRight.getLayoutParams().height = 30;
                    bottomRight.getLayoutParams().width = 30;
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
}
