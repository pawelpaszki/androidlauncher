package com.example.pawelpaszki.launcher.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.example.pawelpaszki.launcher.AppDetail;
import com.example.pawelpaszki.launcher.utils.IconLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by PawelPaszki on 08/05/2017.
 */

public class MyIntentService extends IntentService {

    private List<AppDetail> mApps = new ArrayList<>();
    private ArrayList<String> mActivities = new ArrayList<>();
    private int listSize = 0;

    public MyIntentService() {
        super("MyWorkerThread");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable(){
            public void run() {
                loadApps();
            }
        }).start();
    }

    private void loadApps(){
        PackageManager manager = getPackageManager();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        listSize = availableActivities.size();
        for(ResolveInfo ri:availableActivities){
            String path = this.getFilesDir().getAbsolutePath();
            AppDetail app = new AppDetail();
            app.setLabel(ri.loadLabel(manager));
            app.setName(ri.activityInfo.packageName);
            Bitmap icon = IconLoader.loadImageFromStorage(path, (String) ri.loadLabel(manager));
            if(icon != null) {
                //app.setIcon(RoundedBitmapDrawableFactory.create(this.getResources(), icon));
            } else {
                Bitmap immutableBmp= ((BitmapDrawable) ri.activityInfo.loadIcon(manager)).getBitmap();
                Bitmap mutableBitmap=immutableBmp.copy(Bitmap.Config.ARGB_8888, true);
                
                //BitMapFilter.addTexture(this.getResources(), mutableBitmap);
                //Bitmap iconToSet = BitMapFilter.getShadow(this.getResources(), mutableBitmap);// BitMapFilter.applyEdgeColors(this.getResources(), mutableBitmap));
                //app.setIcon(RoundedBitmapDrawableFactory.create(this.getResources(),iconToSet));
                IconLoader.saveIcon(this, mutableBitmap, (String) ri.loadLabel(manager));
            }
            mApps.add(app);
            mActivities.add(ri.activityInfo.packageName);
            //Log.i("app package name", String.valueOf(ri.activityInfo.packageName));
            //Log.i("app label", String.valueOf(ri.loadLabel(manager)));
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.i(TAG, "onHandleIntent, Thread name: " + Thread.currentThread().getName());

        ResultReceiver resultReceiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();
        bundle.putString("wait", "wait");
        resultReceiver.send(1, bundle);
        while (mApps.size() == 0 || mApps.size() < listSize) {
            new Thread(new Runnable(){
                public void run() {
                    try {
                        //Log.i("thread sleep", String.valueOf(mApps.size()));
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        //Log.i("mApps sent", "mApps sent");
        bundle = new Bundle();
        bundle.putStringArrayList("mApps", mActivities);

        resultReceiver.send(18, bundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.i(TAG, "onDestroy, Thread name: " + Thread.currentThread().getName());
    }
}
