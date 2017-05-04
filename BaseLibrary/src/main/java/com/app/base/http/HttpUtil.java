package com.app.base.http;

import android.content.Context;
import android.text.TextUtils;

import com.app.base.http.callback.HttpCallBack;
import com.app.base.http.config.RetrofitClient;
import com.app.base.util.NetworkUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.app.base.http.constant.AppConstant.BASE_URL;

/**
 * 构建者模式创建网络工具类
 * Created by yuandong on 2017/4/24.
 */

public class HttpUtil<T> {
    private static volatile HttpUtil httpUtil;
    private static volatile ServiceApi mService;
    private Map<String, String> headerParams;//自定义请求头
    private Map<String, String> requestBodyParams;//请求体
    private String mUrl;//访问接口

    private HttpUtil(ServiceApi mService, String url, Map<String, String> headerParams, Map<String, String> requestBodyParams) {
        this.mService = mService;
        this.mUrl = url;
        this.headerParams = headerParams;
        this.requestBodyParams = requestBodyParams;
    }

    public static class Builder {
        private Context mContext;//全局context
        private OkHttpClient mClient;//自定义参数OkHttp
        private String url;//访问接口
        private Map<String, String> header;
        private Map<String, String> params;

        public Builder(Context context) {
            try {
                //防止传入的是activity的上下文
                mContext = context.getApplicationContext();
            } catch (Exception e) {
                e.printStackTrace();
                mContext = context;
            }
        }

        //自定义OKHttp
        public Builder client(OkHttpClient client) {
            this.mClient = client;
            return this;
        }

        //自定义请求头
        public Builder header(Map<String, String> header) {
            this.header = header;
            return this;
        }

        //自定义参数
        public Builder params(Map<String, String> params) {
            this.params = params;
            return this;
        }

        //访问URL
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public HttpUtil build() {
            if (TextUtils.isEmpty(this.url)) {
                throw new NullPointerException("request can not be null");
            }
            if (mClient == null) {
                mClient = RetrofitClient.getInstance(mContext).create();
            }
            Retrofit.Builder builder = new Retrofit.Builder();
            Retrofit retrofit = builder
                    .baseUrl(BASE_URL + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(mClient).build();
            ServiceApi serviceApi =
                    retrofit.create(ServiceApi.class);
            httpUtil = new HttpUtil(serviceApi, url, header, params);
            return httpUtil;
        }

    }

    //get请求
    public void get() {

    }

    //post请求
    public void post() {
        if(!NetworkUtil.isNetworkAvailable()){
            //提示网络有问题
        }
        Map<String, String> header = addCommonHeader();
        if (headerParams != null && headerParams.size() > 0) {
            header.putAll(headerParams);
        }
        checkParams(header);
        Map<String, String> params = addCommonParams();
        if (requestBodyParams != null && requestBodyParams.size() > 0) {
            params.putAll(requestBodyParams);
        }
        checkParams(params);
        Call<T> call = mService.post(header, mUrl, params);
        call.enqueue(new HttpCallBack<T>());
    }


    /**
     * 添加公共header头部参数
     *
     * @return
     */
    private Map<String, String> addCommonHeader() {
        Map<String, String> commonHeader = new HashMap<>();
        //自定义添加一些头部信息
        commonHeader.put("Cache-Time", "3600*24");
        return commonHeader;
    }

    /**
     * 添加公共请求体参数
     *
     * @return
     */
    private Map<String, String> addCommonParams() {
        Map<String, String> commonParams = new HashMap<>();
        //自定义添加一些公共参数
        return commonParams;
    }

    /**
     * 检验参数
     */
    private Map<String, String> checkParams(Map<String, String> params) {
        if (params != null && params.size() > 0) {
            //retrofit的headers的值不能为null，此处做下校验，防止出错
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() == null) {
                    params.put(entry.getKey(), "");
                }
            }
        }
        return params;
    }

}
