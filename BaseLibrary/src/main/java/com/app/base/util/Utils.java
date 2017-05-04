package com.app.base.util;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * 工具类初始化
 * Created by yuandong on 2017/4/24.
 */
public final class Utils {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private Utils() {
        throw new UnsupportedOperationException("can not  instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Utils.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) return context;
        throw new NullPointerException("u should init first");
    }
}