package com.app.base.http.callback;

/**
 * 数据结果回调接口
 * Created by yuandong on 2017/5/5.
 */

public interface ResponseCallBack<T>  {
    void onSuccess(T t);
    void onFailure(String error);
}
