package com.app.base.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.app.base.R;

/**
 * 右滑关闭当前的activity效果
 * Created by yuandong on 2017/4/14.
 */

public class SwipeBackLayout extends FrameLayout {

    private static final String TAG = SwipeBackLayout.class.getName();

    private Activity mActivity;
    private Scroller mScroller;
    private int mShadowWidth;//阴影宽度
    private Drawable mLeftShadow;
    private int mLastX;
    private int mLastY;
    private int mScreenWidth;
    private int mMinX;
    private boolean edgeOnly = false;//是否从边缘滑动

    public SwipeBackLayout setEdgeOnly(boolean edgeOnly) {
        this.edgeOnly = edgeOnly;
        return this;
    }

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    //初始化
    private void init(Context context) {
        this.mActivity = (Activity) context;
        mScroller = new Scroller(context);
        //滑动时渐变的阴影
        mLeftShadow = getResources().getDrawable(R.drawable.shadow_left);
        //阴影的宽度
        mShadowWidth = ((int) getResources().getDisplayMetrics().density) * 16;
        //得到屏幕的宽度
        mScreenWidth = mActivity.getWindowManager().getDefaultDisplay().getWidth();
        mMinX = mScreenWidth / 10;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY=(int)event.getY();
                //如果设置必须从边缘
                if (edgeOnly && mLastX > 20) {
                    return super.onTouchEvent(event);
                }

                break;
            case MotionEvent.ACTION_MOVE:
                int eventX = (int) event.getX();
                int eventY=(int) event.getY();
                Log.d(TAG, "eventX: " + eventX+ "eventY :"+eventY);
                int dx = mLastX - eventX;
                int dy=mLastY-eventY;
                //如果第一次滑动时，水平方向滑动的距离小于竖直方向滑动的距离则不滑动
                if(Math.abs(dx)<Math.abs(dy)&&getScrollX()==0){
                    return super.onTouchEvent(event);
                }
                if (getScrollX() + dx >= 0) {
                    scrollTo(0, 0);
                } else if (eventX > mMinX) {
                    //手指处于屏幕边缘时不处理滑动
                    scrollBy(dx, 0);
                }
                mLastX = eventX;
                break;
            case MotionEvent.ACTION_UP:
                if (-getScrollX() < mScreenWidth / 2) {
                    slideBack();
                } else {
                    slideFinish();
                }
                break;
        }
        return true;
    }

    private void slideFinish() {
        mScroller.startScroll(getScrollX(), 0, -getScrollX() - mScreenWidth, 0, 200);
        invalidate();
    }

    private void slideBack() {
        mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 200);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        } else if (-getScrollX() == mScreenWidth) {
            mActivity.finish();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //绘制边缘阴影
        canvas.save();
        //设置阴影的大小范围
        mLeftShadow.setBounds(0, 0, mShadowWidth, getHeight());
        //平移画布
        canvas.translate(-mShadowWidth, 0);
        //绘制
        mLeftShadow.draw(canvas);
        canvas.restore();
    }


    public void bind() {
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
        View child = decorView.getChildAt(0);
        decorView.removeView(child);
        addView(child);
        decorView.addView(this);
    }
}
