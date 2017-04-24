package com.app.base.http.config;

import android.content.Context;
import com.app.base.BuildConfig;
import com.app.base.http.cache.CacheProvider;
import com.app.base.http.interceptor.CacheInterceptor;
import com.app.base.http.interceptor.DownLoadInterceptor;
import com.app.base.http.interceptor.HttpLoggingInterceptor;
import com.app.base.http.interceptor.RetryAndChangeIpInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 自定义OkHttp：配置连接、读写超时、日志拦截、缓存等
 * Created by yuandong on 2017/4/24.
 */

public class RetrofitClient {

    private static volatile RetrofitClient singleton;
    private static Context mContext;
    private static final int DEFAULT_TIME = 30;
    public static OkHttpClient okHttpClient;

    public static RetrofitClient getInstance(Context context) {
        if (singleton == null) {
            synchronized (RetrofitClient.class) {
                if (singleton == null) {
                    mContext = context.getApplicationContext();
                    singleton = new RetrofitClient();
                }
            }
        }
        return singleton;
    }


    /**
     * 自定义配置OkhttpClient
     */
    public OkHttpClient build() {
        if(okHttpClient==null){
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new DownLoadInterceptor(BASE_URL))
                    .addInterceptor(new RetryAndChangeIpInterceptor(BASE_URL, SERVERS))
                    .addNetworkInterceptor(new CacheInterceptor())
                    .cache(new CacheProvider(mContext).provideCache())
                    .retryOnConnectionFailure(true)
                    .connectTimeout(DEFAULT_TIME, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIME, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIME, TimeUnit.SECONDS)
                    .build();
            if (BuildConfig.DEBUG) {//printf logs while  debug
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                client = client.newBuilder().addInterceptor(logging).build();
            }
            okHttpClient=client;
            return client;
        }

       return okHttpClient;
    }
}
