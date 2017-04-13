package com.yuandong.ble.statusbar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.app.base.activity.BaseActivity;
import com.app.base.util.StatusBarUtil;
import com.yuandong.ble.R;

import java.util.ArrayList;
import java.util.Random;

public class FragmentStatusBarActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected View bindView() {
        return null;
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_fragment_status_bar;
    }

    @Override
    protected void initView(View view) {
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
    }

    @Override
    protected void initListener() {
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void initData() {
        mFragmentList.add(new ImageFragment());
        mFragmentList.add(new SimpleFragment());
        mFragmentList.add(new SimpleFragment());
        mFragmentList.add(new SimpleFragment());
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList.size();
            }
        });
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForImageViewInFragment(this, null);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                StatusBarUtil.setTranslucentForImageViewInFragment(this, null);
                break;
            default:
                Random random = new Random();
                int color = 0xff000000 | random.nextInt(0xffffff);
                if (mFragmentList.get(position) instanceof SimpleFragment) {
                    ((SimpleFragment) mFragmentList.get(position)).setTvTitleBackgroundColor(color);
                }
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
