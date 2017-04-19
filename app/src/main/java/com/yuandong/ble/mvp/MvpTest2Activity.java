package com.yuandong.ble.mvp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.base.activity.BaseActivity;
import com.yuandong.ble.R;

public class MvpTest2Activity extends BaseActivity<MvpTestPresenter> implements MvpTestView {
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
        return R.layout.activity_mvp_test2;
    }

    @Override
    protected void initView(View view) {
        btn = (Button) view.findViewById(R.id.button2);
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
        mPresenter.onClick(v);
    }

    @Override
    public void showToast() {
        btn.setText("MVP 测试模式");
        Toast.makeText(this,"MVP 测试模式",Toast.LENGTH_SHORT).show();
    }
}
