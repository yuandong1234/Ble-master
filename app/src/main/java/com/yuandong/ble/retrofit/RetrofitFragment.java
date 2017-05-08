package com.yuandong.ble.retrofit;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.app.base.fragment.BaseFragment;
import com.app.base.http.HttpUtil;
import com.app.base.http.callback.ResponseCallBack;
import com.app.base.util.LogUtils;
import com.yuandong.ble.R;
import com.yuandong.ble.adapter.NewsAdapter;
import com.yuandong.ble.retrofit.entity.BaseResponse;
import com.yuandong.ble.retrofit.entity.News;
import com.yuandong.ble.retrofit.entity.ResponseResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuandong on 2017/4/20.
 */

public class RetrofitFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private NewsAdapter adapter;
    private ArrayList<News> pics;

    public static RetrofitFragment newInstance() {
        RetrofitFragment contentFragment = new RetrofitFragment();
        return contentFragment;
    }

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected int bindLayout() {
        return R.layout.fragment_retrofit;
    }

    @Override
    protected void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        //创建默认的线性LayoutManager
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        recyclerView.setHasFixedSize(true);
        //创建并设置Adapter
        pics = new ArrayList<>();
        adapter = new NewsAdapter(getActivity(), pics);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {
        Map<String, String> params = new HashMap<>();
        params.put("type", "top");
        params.put("key", "aa47561558f285fee99f1943c7b844fb");
//        RetrofitUtil.post(params, new RequestCallback() {
//            @Override
//            public void onSuccess(Object object) {
//                BaseResponse baseResponse=(BaseResponse)object;
//                adapter.addItem(baseResponse.result.data);
//            }
//
//            @Override
//            public void onFailure(String error) {
//
//            }
//        });

        HttpUtil.Builder builder = new HttpUtil.Builder(_mActivity)
                .url("index")
                .params(params);
        HttpUtil httpUtil = builder.build();
        httpUtil.post(new ResponseCallBack<ResponseResult>() {
            @Override
            public void onSuccess(ResponseResult responseResult) {
                LogUtils.e("success");
            }

            @Override
            public void onFailure(String error) {
                LogUtils.e(error);
            }
        });


    }
}
