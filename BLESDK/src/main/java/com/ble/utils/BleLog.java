package com.ble.utils;

import android.os.Environment;
import android.util.Log;

/**
 * 蓝牙日志打印
 * Created by yuandong on 2017/3/7.
 */

public class BleLog {

    private static String TAG = "Bluetooth :"+DateUtil.getCurrentDateTime()+" ";
//    public static final String ROOT = Environment.getExternalStorageDirectory().getPath();// SD卡中的根目录
//    public static final String PATH = "/appName/log";
    private static boolean isDEBUG = true;

    public static void e(String content) {
        if (isDEBUG) {
            Log.e(TAG, content);
        }
    }

    public static void d(String content) {
        if (isDEBUG) {
            Log.d(TAG, content);
        }
    }

    public static void w(String content) {
        if (isDEBUG) {
            Log.w(TAG, content);
        }
    }

    public  static void i(String content) {
        if (isDEBUG) {
            Log.i(TAG, content);
        }
    }

    public static void v(String content) {
        if (isDEBUG) {
            Log.v(TAG, content);
        }
    }

    public static void wtf(String content) {
        if (isDEBUG) {
            Log.wtf(TAG, content);
        }
    }
    public static void e(String content,Throwable tr ) {
        if (isDEBUG) {
            Log.e(TAG,content, tr);
        }
    }
}
