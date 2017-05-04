package com.app.base.http.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 访问响应回调，以及各种返回信息的处理
 * Created by yuandong on 2017/5/4.
 */

public class HttpCallBack<T> implements Callback<T> {


    @Override
    public void onResponse(Call<T> call, Response<T> response) {

    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {

    }
}
