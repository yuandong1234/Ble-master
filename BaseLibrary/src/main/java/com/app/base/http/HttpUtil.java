package com.app.base.http;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.app.base.http.callback.HttpCallBack;
import com.app.base.http.callback.ResponseCallBack;
import com.app.base.http.config.RetrofitClient;
import com.app.base.util.NetworkUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.app.base.http.constant.AppConstant.BASE_URL;

/**
 * 构建者模式创建网络工具类
 * Created by yuandong on 2017/4/24.
 */

public class HttpUtil {
    private static volatile HttpUtil httpUtil;
    private static volatile ServiceApi mServiceApi;
    private Map<String, String> headerParams;//自定义请求头
    private Map<String, String> requestBodyParams;//请求体
    private String mUrl;//访问接口
    private Class mModel;//返回数据类型

    private HttpUtil(ServiceApi serviceApi, String url, Map<String, String> headerParams,
                     Map<String, String> requestBodyParams,Class clazz) {
        this.mServiceApi = serviceApi;
        this.mUrl = url;
        this.headerParams = headerParams;
        this.requestBodyParams = requestBodyParams;
        this.mModel=clazz;
    }

    public static class Builder {
        private Context mContext;//全局context
        private OkHttpClient mClient;//自定义参数OkHttp
        private String url;//访问接口
        private Map<String, String> header;//消息头
        private Map<String, String> params;//参数
        private Class model;

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

        public Builder model(Class model){
            this.model=model;
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
                    .client(mClient)
                    .build();
            ServiceApi serviceApi = retrofit.create(ServiceApi.class);
            httpUtil = new HttpUtil(serviceApi, url, header, params,model);
            return httpUtil;
        }

    }

    //get请求
    public void get() {

    }

    //post请求
    public<T> void post(ResponseCallBack<T> callBack) {

        if (!NetworkUtil.isNetworkAvailable()) {
            //网络异常，比如Toast弹框提示
            if(callBack!=null){
                callBack.onNetworkError();
            }
        }else{
            //网络正常，比如加载框dialog
            if(callBack!=null){
                callBack.onStart();
            }
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
        Call<ResponseBody> call = mServiceApi.post(header, mUrl, params);
        //noinspection unchecked
        call.enqueue(new HttpCallBack(callBack,mModel));

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
