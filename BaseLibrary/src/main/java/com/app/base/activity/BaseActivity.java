package com.app.base.activity;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.app.base.R;
import com.app.base.util.SystemBarTintManager;

import me.yokeyword.fragmentation.SupportActivity;

public abstract class BaseActivity extends SupportActivity {

    /*统一日志打印TAG*/
    protected final String TAG = this.getClass().getSimpleName();

    /* 当前Activity渲染的视图View**/
    private View mContextView = null;

    /**
     * 是否沉浸状态栏
     **/
    protected boolean isSetStatusBar = true;
    /**
     * 是否允许全屏
     **/
    protected boolean mAllowFullScreen = true;
    /**
     * 是否禁止旋转屏幕
     **/
    protected boolean isAllowScreenRoate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BaseActivity-->onCreate()");
        Bundle bundle = getIntent().getExtras();
        initParams(bundle);
        View view = bindView();
        if (null == view) {
            mContextView = LayoutInflater.from(this).inflate(bindLayout(), null);
        } else {
            mContextView = view;
        }
        //判断允许是全屏
        if (mAllowFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        //是否沉侵式标题栏
        if (isSetStatusBar) {
            setStatusBar();
        }
        //写入布局
        setContentView(mContextView);
        if (!isAllowScreenRoate) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        initView(mContextView);
        initListener();
    }

    /**
     * 初始化传递的参数
     *
     * @param params
     */
    protected abstract void initParams(Bundle params);

    /**
     * [绑定视图]
     *
     * @return
     */
    protected abstract View bindView();

    /**
     * [绑定布局]
     *
     * @return
     */
    protected abstract int bindLayout();

    /**
     * 初始化布局
     *
     * @param view
     */
    protected abstract void initView(View view);

    /**
     * 设置监听事件
     */
    protected abstract void initListener();

    /**
     * 设置沉浸式状态栏
     */
    private void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);//通知栏所需颜色
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}
