package com.yuandong.ble.statusbar;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.app.base.activity.BaseActivity;
import com.app.base.util.StatusBarUtil;
import com.yuandong.ble.R;

/**
 * 界面背景为图片的透明和半透明效果
 */
public class TransparentActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    private SeekBar seekBar;
    private int mAlpha;

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected View bindView() {
        return null;
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_transparent;
    }

    @Override
    protected void initView(View view) {
        seekBar = (SeekBar) findViewById(R.id.sb_change_alpha);
    }

    @Override
    protected void initListener() {
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucent(this, StatusBarUtil.DEFAULT_STATUS_BAR_ALPHA);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mAlpha = progress;
        StatusBarUtil.setTranslucent(this, mAlpha);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}
