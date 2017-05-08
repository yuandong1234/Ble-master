package com.app.base.http.callback;

import com.app.base.http.entity.BaseEntity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 访问响应回调，以及各种返回信息的处理
 * Created by yuandong on 2017/5/4.
 */

public class HttpCallBack<T> implements Callback<BaseEntity<T>> {

    private final int RESPONSE_CODE_OK = 0;      //根据项目实际情况,成功返回正确数据状态码
    private final int RESPONSE_CODE_FAILED = -1; //根据项目实际情况,返回数据失败状态码

    private ResponseCallBack<T> callBack;

    public HttpCallBack(ResponseCallBack<T> callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onResponse(Call<BaseEntity<T>> call, Response<BaseEntity<T>> response) {

        if (response.isSuccessful()) {
            if (response.body().error_code == RESPONSE_CODE_OK) {//成功数据返回
                if (callBack != null) {
                    callBack.onSuccess(response.body().result);
                }

            } else {//错误数据返回
                //根据项目的返回的error_code,进行不同的处理
                callBack.onFailure(response.body().reason);
            }
        }

    }

    @Override
    public void onFailure(Call<BaseEntity<T>> call, Throwable t) {
//        if (t instanceof ) {
//
//        }
    }
}
