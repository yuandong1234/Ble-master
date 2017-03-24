package com.ble.callback;

import com.ble.exception.BleException;


/** BLE操作回调
 * Created by yuandong on 2017/3/8
 */

public interface BleCallBack<T> {
    void onSuccess(T t, int type);
    void onFailure(BleException exception);
}
