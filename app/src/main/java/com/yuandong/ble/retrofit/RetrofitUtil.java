package com.yuandong.ble.retrofit;

import android.util.Log;

import com.app.base.http.config.RetrofitClient;
import com.app.base.util.LogUtils;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.yuandong.ble.MyApplication;
import com.yuandong.ble.retrofit.entity.BaseResponse;
import com.yuandong.ble.retrofit.entity.UploadFileResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yuandong on 2017/4/20.
 */

public class RetrofitUtil {

    private static String TAG = RetrofitUtil.class.getSimpleName();
    public static final String BASE_URL = "http://v.juhe.cn/toutiao/";
    private static OkHttpClient mClient;


    public static void post(Map<String, String> params, final RequestCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService serviceApi = retrofit.create(ApiService.class);
        Call<BaseResponse> call = serviceApi.getNews(params);
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                Log.e(TAG, " request success ");
                // Log.e(TAG,"current thread : "+Thread.currentThread().toString());
                // Gson gson=new Gson();
                // Logger.json(gson.toJson(response.body()));
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Log.e(TAG, " request failure :" + t.toString());
                callback.onFailure(t.toString());
            }
        });
    }

    public static void postFile(String url, Map<String, String> params, ArrayList<String> imagePaths, final RequestCallback callback) {
        if (mClient == null) {
            mClient = RetrofitClient.getInstance(MyApplication.context).create();
        }
        String baseUrl = "http://192.168.87.211:8080/hesvit_file/rest/client/v2/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(mClient)
                .build();
        ApiService serviceApi = retrofit.create(ApiService.class);
        Call<UploadFileResponse> call = serviceApi.postFile(url, params, fileToRequestBody(imagePaths));
        call.enqueue(new Callback<UploadFileResponse>() {
            @Override
            public void onResponse(Call<UploadFileResponse> call, Response<UploadFileResponse> response) {
                Log.e(TAG, " request success ");
                Logger.e(response.body().toString());
                callback.onSuccess(response.body());

            }

            @Override
            public void onFailure(Call<UploadFileResponse> call, Throwable t) {
                Log.e(TAG, " request failure :" + t.toString());
                callback.onFailure(t.toString());
            }
        });
    }

    /**
     * 用RequestBody包裹File
     *
     * @param urls
     * @return
     */
    private static Map<String, RequestBody> fileToRequestBody(ArrayList<String> urls) {
        Map<String, RequestBody> map = new HashMap<>();
        int i = 0;
        for (String path : urls) {
            File file = new File(path);
            //"file\"; filename=\""+file.getName()+"\""
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            //注意这里"（image+i）" 如果服务器有配置参数名，直接替换（image+i）
            map.put("image" + i + "\"" + "; filename=\"\"", requestBody);
            i++;
        }
        return map;
    }
}
