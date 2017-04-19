package com.yuandong.ble;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.TabLayout;


import com.app.base.activity.BaseActivity;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private TabLayout tab_layout;
    private ViewPager viewPager;
    public final static String BLUETOOTH = "蓝牙";
    public final static String STATUSBAR = "状态栏";
    public final static String MVP = "mvp";
    private ViewPagerAdapter adapter;

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected View bindView() {
        return null;
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(View view) {
        tab_layout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tab_layout.setupWithViewPager(viewPager);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        ArrayList<String> titles = new ArrayList<>();
        titles.add(BLUETOOTH);
        titles.add(STATUSBAR);
        titles.add(MVP);
        ArrayList<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            fragments.add(SimpleFragment.newInstance(titles.get(i)));
        }
        adapter.addItems(fragments,titles);
    }
}
