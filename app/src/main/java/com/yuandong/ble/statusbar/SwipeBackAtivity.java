package com.yuandong.ble.statusbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.app.base.activity.BaseActivity;
import com.app.base.util.StatusBarUtil;
import com.app.base.widget.SwipeBackLayout;
import com.yuandong.ble.R;

public class SwipeBackAtivity extends BaseActivity {
    private int mColor = Color.GRAY;
    private RelativeLayout titleBar;
    private ImageView iv_back;

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected View bindView() {
        return null;
    }

    @Override
    protected int bindLayout() {
        isSwipeBack = true;
        return R.layout.activity_swipe_back_ativity;
    }

    @Override
    protected void initView(View view) {
        iv_back = (ImageView) view.findViewById(R.id.iv_back);
        titleBar = (RelativeLayout) view.findViewById(R.id.titleBar);
        titleBar.setBackgroundColor(mColor);
    }

    @Override
    protected void initListener() {
        iv_back.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColorForSwipeBack(this, mColor, 38);
    }

    @Override
    protected void setSwipeBackMode() {
        new SwipeBackLayout(this)
             //   .setEdgeOnly(true)
                .bind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }
}
