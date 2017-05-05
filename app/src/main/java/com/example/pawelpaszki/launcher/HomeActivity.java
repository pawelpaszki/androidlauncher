package com.example.pawelpaszki.launcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void showApps(View v){
        Intent i = new Intent(this, AppsListActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
