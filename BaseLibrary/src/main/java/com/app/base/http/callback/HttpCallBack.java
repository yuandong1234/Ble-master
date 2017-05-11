package com.app.base.http.callback;

import android.text.TextUtils;
import android.util.Log;

import com.app.base.http.cache.CacheManager;
import com.app.base.http.entity.BaseEntity;
import com.app.base.util.LogUtils;
import com.app.base.util.NetworkUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 访问响应回调，以及各种返回信息的处理
 * Created by yuandong on 2017/5/4.
 */

public class HttpCallBack<T extends BaseEntity> implements Callback<ResponseBody> {

    private static String TAG = HttpCallBack.class.getSimpleName();

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final int RESPONSE_CODE_OK = 0;      //根据项目实际情况,成功返回正确数据状态码
    private final int RESPONSE_CODE_FAILED = -1; //根据项目实际情况,返回数据失败状态码

    private ResponseCallBack<T> callBack;
    private Class<T> mClass;

    public HttpCallBack(ResponseCallBack<T> callBack, Class clazz) {
        this.callBack = callBack;
        this.mClass = clazz;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

        if (response.isSuccessful()) {
            try {
                String json = response.body().string();
                if (!TextUtils.isEmpty(json)) {
                    Log.wtf(TAG, "----> data from the service");
                    LogUtils.json(json);
                    Gson gson = new Gson();
                    BaseEntity baseEntity = gson.fromJson(json, BaseEntity.class);
                    //先判断数据返回是成功返回，然后在去解析相应的实体类
                    if (baseEntity.error_code == RESPONSE_CODE_OK) {
                        T bean = gson.fromJson(json, mClass);
                        if (callBack != null) {
                            //noinspection unchecked
                            callBack.onSuccess(bean);
                            callBack.onFinish();
                        }
                    } else {
                        //TODO 错误数据返回，进行相应的错误处理
                        if (callBack != null) {
                            callBack.onFailure(baseEntity.reason);
                            callBack.onFinish();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (callBack != null) {
                    callBack.onFailure("parse data error");
                    callBack.onFinish();
                }
            }
        }

    }

    @Override
    public void onFailure(Call<ResponseBody> call,final Throwable t) {
        LogUtils.e(t.getClass());
        if (NetworkUtil.isNetworkAvailable() || !CacheManager.isCache) {
            //网络可用或者不开启缓存时直接返回失败
            if (callBack != null) {
                callBack.onFailure(t.getMessage());
                callBack.onFinish();
            }
        }

        Request request = call.request();
        String url = request.url().toString();
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        if (hasRequestBody) {
            Buffer buffer = new Buffer();
            try {
                requestBody.writeTo(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            sb.append(buffer.readString(charset));
            buffer.close();
        }
        CacheManager.getInstance().getCache(sb.toString(), new DiskLruCacheCallBack() {
            @Override
            public void onCache(String json) {
                if(!TextUtils.isEmpty(json)){
                    Log.wtf(TAG, "----> data from the disk cache");
                    LogUtils.json(json);
                    Gson gson = new Gson();
                    T bean = gson.fromJson(json, mClass);
                    if(callBack!=null){
                        callBack.onCache(bean);
                        callBack.onFinish();
                    }
                }else{
                    if(callBack!=null){
                       callBack.onFailure(t.getMessage());
                        callBack.onFinish();
                    }

                }
            }
        });


    }
}
