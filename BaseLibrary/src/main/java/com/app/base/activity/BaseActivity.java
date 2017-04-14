package com.app.base.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.app.base.R;
import com.app.base.util.StatusBarUtil;
import com.app.base.widget.SwipeBackLayout;

import me.yokeyword.fragmentation.SupportActivity;

public abstract class BaseActivity extends SupportActivity implements View.OnClickListener {

    /*统一日志打印TAG*/
    protected final String TAG = this.getClass().getSimpleName();

    /* 当前Activity渲染的视图View**/
    private View mContextView = null;
    /**
     * 是否允许全屏
     **/
    protected boolean mAllowFullScreen = true;
    /**
     * 是否禁止旋转屏幕
     **/
    protected boolean isAllowScreenRoate = false;

    /**
     * 是否允许侧滑关闭activity
     */
    protected boolean isSwipeBack=false;

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
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            //此方法对Activity有效，对AppCompatActivity无效
            //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        //写入布局
        setContentView(mContextView);
        //是否沉侵式标题栏
        setStatusBar();
        //设置右滑关闭当前的activity
        setSwipeBackMode();
        if (!isAllowScreenRoate) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        initView(mContextView);
        initListener();
        initData();

    }

    /**
     * 设置状态栏的样式
     */
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));
    }

    /**
     * 设置右滑关闭当前activity
     */
    protected  void setSwipeBackMode(){
        if(isSwipeBack){
           new SwipeBackLayout(this).bind();
        }

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
     * 数据逻辑处理
     */
    protected abstract void initData();


    /**
     *点击事件
     */
    @Override
    public void onClick(View v) {
    }
}
