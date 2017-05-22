package com.yuandong.ble.retrofit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.base.fragment.BaseFragment;
import com.app.base.http.HttpUtil;
import com.app.base.http.callback.ResponseCallBack;
import com.app.base.util.LogUtils;
import com.yalantis.ucrop.UCrop;
import com.yuandong.ble.R;
import com.yuandong.ble.adapter.NewsAdapter;
import com.yuandong.ble.retrofit.entity.News;
import com.yuandong.ble.retrofit.entity.NewsListEntity;
import com.yuandong.ble.util.PhotoUtil;
import com.yuandong.ble.widget.BottomDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuandong on 2017/4/20.
 */

public class UoloadFragment extends BaseFragment {
    private Button upload;
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
        upload=(Button)view.findViewById(R.id.upload);

    }

    @Override
    protected void initListener() {
        upload.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.upload:
                _mActivity.startActivity(new Intent(_mActivity,UplaodFileActivity.class));
                break;
        }
    }


}
