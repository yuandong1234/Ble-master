package com.yuandong.ble.retrofit;

import com.yuandong.ble.retrofit.entity.BaseResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
/**
 * Created by yuandong on 2017/4/20.
 */

public interface ApiService {

    @FormUrlEncoded
    @POST("index")
    Call<BaseResponse> getNews(@FieldMap Map<String,String>  params);

}
