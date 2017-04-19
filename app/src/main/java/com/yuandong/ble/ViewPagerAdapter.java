package com.yuandong.ble;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by yuandong on 2017/4/19.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment>fragments;
    private ArrayList<String> titles;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments=new ArrayList<>();
        titles=new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        if(fragments.size()>0){
           return  fragments.size();
        }
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(titles.size()>0){
            return titles.get(position);
        }
       return null;
    }

    public void addItems(ArrayList<Fragment>fs,ArrayList<String> ts){
        fragments.addAll(fs);
        titles.addAll(ts);
        notifyDataSetChanged();
    }
}
