package com.app.base.http.interceptor;

import android.text.TextUtils;

import com.app.base.util.LogUtils;
import com.app.base.util.NetworkUtil;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yuandong on 2017/4/24.
 */
public class CacheInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
//        Request request = chain.request();
//        Response response = chain.proceed(request);
//        String cache = request.header("Cache-Time");
//        if (!TextUtils.isEmpty(cache)) {
//            Response response1 = response.newBuilder()
//                    .removeHeader("Pragma")
//                    .removeHeader("Cache-Control")
//                    //cache for 30 days
//                    .header("Cache-Control", "max-age="+cache)
//                    .build();
//            return response1;
//        } else {
//            return response;
//        }

        Request request = chain.request();
        if (!NetworkUtil.isNetworkAvailable()) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
            LogUtils.d(" network unavailable ");
        }

        Response response = chain.proceed(request);
        //获得自定义头部的缓存时间
        String cacheTime = request.header("Cache-Time");
        if (NetworkUtil.isNetworkAvailable()) {
            int maxAge = 60 * 60; // read from cache for 1 hour
            response.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, max-age=" +(TextUtils.isEmpty(cacheTime)?maxAge:cacheTime))
                    .build();
        } else {
            int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
            response.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
        return response;
    }
}
