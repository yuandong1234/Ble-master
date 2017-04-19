package com.yuandong.ble;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.app.base.fragment.BaseFragment;
import com.yuandong.ble.bluetooth.BleActivity;
import com.yuandong.ble.mvp.MvpTestActivity;
import com.yuandong.ble.statusbar.StatusBarActivity;

import static com.yuandong.ble.MainActivity.BLUETOOTH;
import static com.yuandong.ble.MainActivity.MVP;
import static com.yuandong.ble.MainActivity.STATUSBAR;

/**
 * Created by yuandong on 2017/4/19.
 */

public class SimpleFragment extends BaseFragment {
    public static final String ARGUMENT = "argument";
    private String title;

    private Button btn;
    /**
     * 传入需要的参数，设置给arguments
     * @param argument
     * @return
     */
    public static SimpleFragment newInstance(String argument) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, argument);
        SimpleFragment contentFragment = new SimpleFragment();
        contentFragment.setArguments(bundle);
        return contentFragment;
    }
    @Override
    protected void initParams(Bundle params) {
        title=params.getString(ARGUMENT);
    }

    @Override
    protected int bindLayout() {
        return R.layout.fragment_layout_simple;
    }

    @Override
    protected void initView(View view) {
        btn=(Button) view.findViewById(R.id.btn);
    }

    @Override
    protected void initListener() {
        Log.e(TAG,"title :  "+title);
        btn.setText(title);
        btn.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (title){
            case BLUETOOTH:
                _mActivity.startActivity(new Intent(_mActivity, BleActivity.class));
                break;
            case STATUSBAR:
                _mActivity.startActivity(new Intent(_mActivity, StatusBarActivity.class));
                break;
            case MVP:
                _mActivity.startActivity(new Intent(_mActivity, MvpTestActivity.class));
                break;
        }
    }
}
