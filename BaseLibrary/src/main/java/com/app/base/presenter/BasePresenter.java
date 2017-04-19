package com.app.base.presenter;

import android.app.Activity;
import android.content.Context;

import com.app.base.util.TUtil;
import java.lang.ref.WeakReference;

/**
 * 基类BasePresenter
 * Created by yuandong on 2017/4/19.
 */

public abstract class BasePresenter<M,V> {

    /**
     * 使用弱引用避免内存泄漏
     * context weak reference
     */
    private WeakReference<Context> mContextRef;
    /**
     * View weak reference
     */
    private WeakReference<V> mViewRef;

    protected Context mContext;
    protected V mView;
    protected M mModel;


    /**
     * 绑定
     *
     * @param context
     */
    public void attach(Context context,V v) {
        this.mContextRef = new WeakReference<>(context);
        this.mViewRef = new WeakReference<>(v);
        mContext = mContextRef.get();
        mView = mViewRef.get();
        this.mModel = TUtil.getT(this, 0);
    }

    /**
     * 销毁
     */
    public void detach() {
        if (mContextRef != null) {
            mContextRef.clear();
        }
        mContextRef = null;
        if (mViewRef != null) {
            mViewRef.clear();
        }
        mViewRef = null;
       // mView=null;
       // mContext=null;
        mModel=null;
    }

    /**
     * 返回 Context. 如果 Activity被销毁, 那么返回应用的Context.
     *
     * 注意:
     *     通过过Context进行UI方面的操作时应该调用 {@link #isActivityAlive()}
     * 判断Activity是否还已经被销毁, 在Activity未销毁的状态下才能操作. 否则会引发crash.
     * 而获取资源等操作则可以使用应用的Context.
     *
     * @return
     */
    protected boolean isActivityAlive() {
        return !isActivityFinishing() && mViewRef.get() != null;
    }

    /**
     * activity 是否是finishing状态
     *
     * @return
     */
    private boolean isActivityFinishing() {
        if (mContextRef == null) {
            return true;
        }
        Context context = mContextRef.get();
        if (context instanceof Activity) {
            Activity hostActivity = (Activity) context;
            return hostActivity.isFinishing();
        }
        return true;
    }

}
