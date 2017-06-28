package com.pawelpaszki.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;

import com.pawelpaszki.launcher.adapters.CustomPagerAdapter;
import com.pawelpaszki.launcher.adapters.NoScrollViewPager;
import com.pawelpaszki.launcher.utils.SharedPrefs;

/**
 * Created by PawelPaszki on 27/06/2017.
 */

public class FirstLaunchActivity extends Activity {

    private NoScrollViewPager mViewPager;
    private FloatingActionButton mNextSlideButton;
    private static final int CHILD_COUNT = 8;
    private FloatingActionButton mPreviousSlideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);

        mViewPager = (NoScrollViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new CustomPagerAdapter(this));
        mNextSlideButton = (FloatingActionButton) findViewById(R.id.next_slide);
        mPreviousSlideButton = (FloatingActionButton) findViewById(R.id.previous_slide);

    }

    public void showNextSlide(View view) {
        if(mViewPager.getCurrentItem() == 0) {
            mViewPager.setCurrentItem((mViewPager.getCurrentItem())+1);
            mPreviousSlideButton.setVisibility(View.VISIBLE);
        } else if (mViewPager.getCurrentItem() < CHILD_COUNT - 2) {
            mViewPager.setCurrentItem((mViewPager.getCurrentItem())+1);
        } else if (mViewPager.getCurrentItem() == CHILD_COUNT - 2) {
            mViewPager.setCurrentItem((mViewPager.getCurrentItem())+1);
            mNextSlideButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.got_it));
        } else {
            SharedPrefs.setIsFirstLaunch(false,this);
            Intent intent = new Intent(FirstLaunchActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        setTextViewBackground();
    }

    public void showPreviousSlide(View view) {
        if(mViewPager.getCurrentItem() == 1) {
            mPreviousSlideButton.setVisibility(View.GONE);
        } else if (mViewPager.getCurrentItem() + 1 == CHILD_COUNT) {
            mNextSlideButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.right_arrow));
        }
        mViewPager.setCurrentItem((mViewPager.getCurrentItem())-1);
        setTextViewBackground();
    }

    private void setTextViewBackground() {
        RelativeLayout layout = (RelativeLayout) mViewPager.findViewWithTag(mViewPager.getCurrentItem());
        if(mViewPager.getCurrentItem() % 2 == 1) {
            layout.getChildAt(1).setBackgroundColor(0x00000000);
        } else {
            layout.getChildAt(1).setBackgroundColor(0xFF1A237E);
        }
    }
}
