package com.yuandong.ble.statusbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.app.base.activity.BaseActivity;
import com.app.base.util.StatusBarUtil;
import com.yuandong.ble.R;

import java.util.Random;

/**
 * 改变状态栏和标题栏的颜色
 */

public class StatusBarColorActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener{

    private ImageView iv_back;
    private Button changeColor;
    private RelativeLayout titleBar;
    private SeekBar seekBar;
    private int mColor;
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
        return R.layout.activity_status_bar_color;
    }

    @Override
    protected void initView(View view) {
        iv_back = (ImageView) view.findViewById(R.id.iv_back);
        changeColor = (Button) view.findViewById(R.id.changeColor);
        titleBar=(RelativeLayout) findViewById(R.id.titleBar);
        seekBar=(SeekBar) findViewById(R.id.sb_change_alpha);
        seekBar.setMax(255);
    }

    @Override
    protected void initListener() {
        iv_back.setOnClickListener(this);
        changeColor.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.changeColor:
                Random random = new Random();
                mColor = 0xff000000 | random.nextInt(0xffffff);
                titleBar.setBackgroundColor(mColor);
                StatusBarUtil.setColor(this, mColor, mAlpha);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mAlpha = progress;
        StatusBarUtil.setColor(this, mColor, mAlpha);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
