package com.yuandong.ble.retrofit;

/**
 * Created by yuandong on 2017/4/20.
 */

public interface RequestCallback {
    void onSuccess(Object object);
    void onFailure(String error);
}
