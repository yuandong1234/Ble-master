package com.yuandong.ble.mvp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.base.activity.BaseActivity;
import com.yuandong.ble.R;

public class MvpTestActivity extends BaseActivity{
    private Button btn;

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected View bindView() {
        return null;
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_mvp_test;
    }

    @Override
    protected void initView(View view) {
        btn = (Button) view.findViewById(R.id.button);
    }

    @Override
    protected void initListener() {
        btn.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.button:
                startActivity(new Intent(this,MvpTest2Activity.class));
                break;
        }
    }
}
