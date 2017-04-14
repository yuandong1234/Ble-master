package com.yuandong.ble.statusbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.app.base.activity.BaseActivity;
import com.app.base.util.StatusBarUtil;
import com.yuandong.ble.R;

public class SwipeBackAtivity extends BaseActivity {
    private int mColor = Color.GRAY;
    private RelativeLayout titleBar;

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected View bindView() {
        return null;
    }

    @Override
    protected int bindLayout() {
        isSwipeBack=true;
        return R.layout.activity_swipe_back_ativity;
    }

    @Override
    protected void initView(View view) {
        titleBar=(RelativeLayout) view.findViewById(R.id.titleBar);
        titleBar.setBackgroundColor(mColor);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }
    @Override
    protected void setStatusBar() {
        StatusBarUtil.setColorForSwipeBack(this, mColor, 38);
    }
}
