package com.app.base.http.interceptor;

import android.util.Log;

import com.app.base.http.cache.CacheManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 自定义硬盘缓存数据
 * Created by yuandong on 2017/5/11.
 */

public class DiskLruCacheInterceptor implements Interceptor {

    private static String TAG = DiskLruCacheInterceptor.class.getSimpleName();
    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        if (hasRequestBody) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            sb.append(buffer.readString(charset));
            buffer.close();
        }
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            Log.wtf(TAG, "----> HTTP FAILED: " + e);
            throw e;
        }
        ResponseBody responseBody = response.body();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        // Buffer the entire body.
        Buffer buffer = source.buffer();

        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                Log.wtf(TAG, "Couldn't decode the response body; charset is likely malformed.");
                return response;
            }
        }

        String key=sb.toString();
        String value = buffer.clone().readString(charset);
        //TODO  对返回数据进行判断，只存取正确的数据（尚未处理未做）
        Log.wtf(TAG, "---> start to save the response data into disk <---");
        Log.wtf(TAG, "---> key  :"+key);
        Log.wtf(TAG, "---> value  :"+value);
        //把数据存入到硬盘中
        CacheManager.getInstance().putCache(key, value);
        Log.wtf(TAG, "---> success to save the response data into disk <---");
        return response;
    }
}
