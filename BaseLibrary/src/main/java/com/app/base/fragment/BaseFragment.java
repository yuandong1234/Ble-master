package com.app.base.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by yuandong on 2017/4/17.
 */

public abstract  class BaseFragment extends SupportFragment implements View.OnClickListener{

    /*统一日志打印TAG*/
    protected final String TAG = this.getClass().getSimpleName();
    /* 当前Activity渲染的视图View**/
    private View mContextView = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG,"onAttach...");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate...");
        Bundle bundle = _mActivity.getIntent().getExtras();
        initParams(bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView...");
        int layoutResId = bindLayout();
        if(layoutResId>0){
            mContextView=inflater.inflate(layoutResId, container, false);
        }
        return mContextView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG,"onViewCreated...");
        //在这里可以做一些view差实话
        initView(mContextView);
        initListener();
        initData();
    }

    /**
     * 初始化传递的参数
     *
     * @param params
     */
    protected abstract void initParams(Bundle params);
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
     * 点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {

    }
}
