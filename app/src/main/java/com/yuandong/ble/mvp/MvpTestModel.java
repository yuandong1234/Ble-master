package com.yuandong.ble.mvp;

import android.os.Handler;
import android.os.Looper;

import com.app.base.model.BaseModel;

/**
 * Created by yuandong on 2017/4/19.
 */

public class MvpTestModel implements BaseModel {
    Handler handler=new Handler(Looper.getMainLooper());

    public void Click(final MvpTestPresenter.CallBack callBack){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callBack.doCallBack();
            }
        },3000);
    }
}
