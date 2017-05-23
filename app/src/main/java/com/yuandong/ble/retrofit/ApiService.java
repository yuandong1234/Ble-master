package com.yuandong.ble.retrofit;

import com.yuandong.ble.retrofit.entity.BaseResponse;
import com.yuandong.ble.retrofit.entity.UploadFileResponse;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by yuandong on 2017/4/20.
 */

public interface ApiService {

    @FormUrlEncoded
    @POST("index")
    Call<BaseResponse> getNews(@FieldMap Map<String, String> params);

    /**
     * Post 上传文件
     *
     * @param filePath
     * @param params
     * @param images
     * @return
     */
    @Multipart
    @POST("{filePath}")
    Call<UploadFileResponse> postFile(@Path("filePath") String filePath, @QueryMap Map<String, String> params,
                                      @PartMap Map<String, RequestBody> images);

}
