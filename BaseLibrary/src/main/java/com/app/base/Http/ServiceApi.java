package com.app.base.http;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * 访问Api接口(get 、post请求)
 * Created by yuandong on 2017/4/20.
 */

public interface ServiceApi<T> {

    /**
     * post请求
     * @param filePath 接口名称
     * @param params Map类型参数
     * @return
     */
    @FormUrlEncoded
    @POST("{filePath}")
    Call<T> post(@Path("filePath")String filePath,@FieldMap Map<String,String> params);

    /**
     * post请求 参数为json格式
     * @param filePath 接口名称
     * @param body 传入的参数为RequestBody
     * @return
     */
    @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
    @POST("{filePath}")
    Call<T> post(@Path("filePath")String filePath,@Body RequestBody body);

    /**
     * get请求
     * @param filePath 接口
     * @return
     */
    @GET("{filePath}")
    Call<T> get(@Path("filePath")String filePath);

}
