package com.yuandong.ble.retrofit;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.app.base.fragment.BaseFragment;
import com.app.base.http.HttpUtil;
import com.app.base.http.callback.ResponseCallBack;
import com.app.base.util.LogUtils;
import com.yuandong.ble.R;
import com.yuandong.ble.adapter.NewsAdapter;
import com.yuandong.ble.retrofit.entity.News;
import com.yuandong.ble.retrofit.entity.NewsListEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuandong on 2017/4/20.
 */

public class UoloadFragment extends BaseFragment {

    public static UoloadFragment newInstance() {
        UoloadFragment contentFragment = new UoloadFragment();
        return contentFragment;
    }

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected int bindLayout() {
        return R.layout.fragment_retrofit_upload;
    }

    @Override
    protected void initView(View view) {

    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {

    }
}
