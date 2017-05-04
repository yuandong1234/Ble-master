package com.app.base.http;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * 访问Api接口(get 、post请求)
 * Created by yuandong on 2017/4/20.
 */

public interface ServiceApi<T> {

    /**
     * post请求 参数Map类型
     *
     * @param filePath 接口名称
     * @param params   Map类型参数
     * @return
     * @paramheader header 请求头
     */
    @FormUrlEncoded
    @POST("{filePath}")
    Call<T> post(@HeaderMap Map<String, String> header, @Path("filePath") String filePath, @FieldMap Map<String, String> params);

    /**
     * post请求 参数为json格式
     *
     * @param filePath 接口名称
     * @param jsonBody 传入的参数
     * @return
     */
    @POST("{filePath}")
    Call<T> post(@Path("filePath") String filePath, @Body RequestBody jsonBody);

    /**
     * get请求
     *
     * @param filePath 接口
     * @return
     */
    @GET("{filePath}")
    Call<T> get(@Path("filePath") String filePath);

}
