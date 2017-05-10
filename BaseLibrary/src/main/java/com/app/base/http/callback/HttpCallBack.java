package com.app.base.http.callback;

import android.text.TextUtils;

import com.app.base.http.entity.BaseEntity;
import com.app.base.util.LogUtils;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 访问响应回调，以及各种返回信息的处理
 * Created by yuandong on 2017/5/4.
 */

public class HttpCallBack<T extends BaseEntity> implements Callback<ResponseBody> {

    private final int RESPONSE_CODE_OK = 0;      //根据项目实际情况,成功返回正确数据状态码
    private final int RESPONSE_CODE_FAILED = -1; //根据项目实际情况,返回数据失败状态码

    private ResponseCallBack callBack;
    private Class<T> mClass;

    public HttpCallBack(ResponseCallBack<T> callBack, Class clazz) {
        this.callBack = callBack;
        this.mClass = clazz;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

        if (response.isSuccessful()) {
            try {
                String json = response.body().string();
                if (!TextUtils.isEmpty(json)) {
                    LogUtils.json(json);
                    Gson gson = new Gson();
                    BaseEntity baseEntity = gson.fromJson(json, BaseEntity.class);
                    //先判断数据返回是成功返回，然后在去解析相应的实体类
                    if (baseEntity.error_code == RESPONSE_CODE_OK) {
                        T bean = gson.fromJson(json, mClass);
                        if (callBack != null) {
                            //noinspection unchecked
                            callBack.onSuccess(bean);
                            callBack.onFinish();
                        }
                    } else {
                        //错误数据返回，进行相应的错误处理
                        if (callBack != null) {
                            callBack.onFailure(baseEntity.reason);
                            callBack.onFinish();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (callBack != null) {
                    callBack.onFailure("parse data error");
                    callBack.onFinish();
                }
            }
        }

    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        LogUtils.e(t.getClass());
        if (callBack != null) {
            callBack.onFailure(t.getMessage());
            callBack.onFinish();
        }
    }
}
