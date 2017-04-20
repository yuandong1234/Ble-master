package com.app.base.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * 访问Api接口(get 、post请求)
 * Created by yuandong on 2017/4/20.
 */

public interface ServiceApi<T> {

    @FormUrlEncoded
    @POST("{filePath}")
    Call<T> post(@Path("filePath")String filePath,@FieldMap Map<String,String> params);

    @GET("{filePath}")
    Call<T> get(@Path("filePath")String filePath);

}
