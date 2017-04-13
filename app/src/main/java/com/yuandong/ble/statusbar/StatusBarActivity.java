package com.yuandong.ble.statusbar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.app.base.activity.BaseActivity;
import com.app.base.util.StatusBarUtil;
import com.yuandong.ble.R;

/**
 * 改变DrawerLayout布局的Activity的背景半透明
 */

public class StatusBarActivity extends BaseActivity{

    private RelativeLayout titleBar;
    private ImageView iv_back;
    private DrawerLayout drawerLayout;

    private int mStatusBarColor;
    private int mAlpha = StatusBarUtil.DEFAULT_STATUS_BAR_ALPHA;
    private ViewGroup contentLayout;
    private Button changeToTranslucent,changeColor,transparent,imageViewTransparent,statusBarInFragment;
    private boolean isChange;

    @Override
    protected void initParams(Bundle params) {

    }
    @Override
    protected View bindView() {
        return null;
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_status_bar;
    }

    @Override
    protected void initView(View view) {
        titleBar=(RelativeLayout)view.findViewById(R.id.titleBar);
        iv_back=(ImageView)view.findViewById(R.id.iv_meun);
        drawerLayout=(DrawerLayout)view.findViewById(R.id.drawer_layout);
        contentLayout=(ViewGroup)view.findViewById(R.id.main);
        changeToTranslucent=(Button)view.findViewById(R.id.translucent);
        changeColor=(Button)view.findViewById(R.id.changeColor);
        transparent=(Button)view.findViewById(R.id.transparent);
        imageViewTransparent=(Button)view.findViewById(R.id.imageViewTransparent);
        statusBarInFragment=(Button)view.findViewById(R.id.statusBarInFragment);
    }

    @Override
    protected void initListener() {
        iv_back.setOnClickListener(this);
        changeToTranslucent.setOnClickListener(this);
        changeColor.setOnClickListener(this);
        transparent.setOnClickListener(this);
        imageViewTransparent.setOnClickListener(this);
        statusBarInFragment.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void setStatusBar() {
        mStatusBarColor = getResources().getColor(R.color.colorPrimary);
        StatusBarUtil.setColorForDrawerLayout(this, (DrawerLayout) findViewById(R.id.drawer_layout),
                mStatusBarColor, mAlpha);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_meun:
                if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
                    drawerLayout.closeDrawers();
                }else{
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.translucent:
                if(!isChange){
                    contentLayout.setBackgroundDrawable(getResources().getDrawable(R.mipmap.bg_girl));
                    StatusBarUtil.setTranslucentForDrawerLayout(this, drawerLayout, mAlpha);
                    titleBar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    isChange=true;
                }else{
                    contentLayout.setBackgroundDrawable(null);
                    titleBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    StatusBarUtil.setColorForDrawerLayout(this, drawerLayout, getResources().getColor(R.color.colorPrimary), mAlpha);
                    isChange=false;
                }
                break;
            case R.id.changeColor:
                startActivity(new Intent(this,StatusBarColorActivity.class));
                break;
            case R.id.transparent:
                startActivity(new Intent(this,TransparentActivity.class));
                break;
            case R.id.imageViewTransparent:
                startActivity(new Intent(this,ImageViewTransparentActivity.class));
                break;
            case R.id.statusBarInFragment:
                startActivity(new Intent(this,FragmentStatusBarActivity.class));
                break;
        }
    }

}
