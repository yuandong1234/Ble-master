package com.yuandong.ble.retrofit;

import android.util.Log;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.yuandong.ble.retrofit.entity.BaseResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yuandong on 2017/4/20.
 */

public class RetrofitUtil{

    private static String TAG=RetrofitUtil.class.getSimpleName();
    public static final String BASE_URL = "http://v.juhe.cn/toutiao/";

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
                Log.e(TAG," request success ");
               // Log.e(TAG,"current thread : "+Thread.currentThread().toString());
               // Gson gson=new Gson();
               // Logger.json(gson.toJson(response.body()));
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Log.e(TAG," request failure :"+t.toString());
                callback.onFailure(t.toString());
            }
        });
    }
}
