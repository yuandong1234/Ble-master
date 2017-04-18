package com.yuandong.ble.statusbar;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.app.base.activity.BaseActivity;
import com.app.base.util.StatusBarUtil;
import com.yuandong.ble.R;

public class ImageViewTransparentActivity extends BaseActivity {
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
        return R.layout.activity_image_view_transparent;
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

    @Override
    protected void setStatusBar() {
        titleBar = (RelativeLayout) findViewById(R.id.titleBar);
        StatusBarUtil.setTranslucentForImageView(this, titleBar);
    }
}
