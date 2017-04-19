package com.yuandong.ble.mvp;


import android.util.Log;
import android.view.View;

import com.app.base.presenter.BasePresenter;

/**
 * Created by yuandong on 2017/4/19.
 */

public class MvpTestPresenter extends BasePresenter<MvpTestModel, MvpTestView> {

    public void onClick(View v) {
        mModel.Click(new CallBack() {
            @Override
            public void doCallBack() {
                if(isActivityAlive()){
                    mView.showToast();
                }

            }
        });
    }

    public interface CallBack {
        void doCallBack();
    }
}
