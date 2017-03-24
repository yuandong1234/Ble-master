package com.yuandong.ble;

import android.app.Application;
import android.content.Context;

/**
 * Created by yuandong on 2017/3/17 0017.
 */

public class MyApplication extends Application {
    public  static  MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    public static MyApplication getInstance(){
      return   instance;
    }
}
