package com.app.base.http.cache;

import android.content.Context;

import okhttp3.Cache;

/**
 * Created by yuandong on 2017/4/24.
 */
public class CacheProvider {
    Context mContext;

    public CacheProvider(Context context) {
        mContext = context;
    }

    public Cache provideCache() {
        return new Cache(mContext.getCacheDir(), 50*1024 * 1024);
    }
}
