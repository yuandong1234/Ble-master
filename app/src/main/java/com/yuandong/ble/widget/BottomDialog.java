package com.yuandong.ble.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.yuandong.ble.R;

/**
 * Created by dong.yuan on 2017/5/19.
 */

public class BottomDialog extends Dialog {
    public BottomDialog(Context context) {
        super(context);
    }

    public BottomDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public BottomDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static class Builder {
        private Context mContext;
        private View contentView;
        private View.OnClickListener listener;

        public Builder(Context mContext) {
            this.mContext = mContext;
        }

        public Builder setContentView(View contentView) {
            this.contentView = contentView;
            return this;
        }

        public Builder setListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public BottomDialog create() {
            BottomDialog dialog = new BottomDialog(mContext, R.style.dialog);
            dialog.addContentView(contentView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setCanceledOnTouchOutside(true);
            //设置监听
            contentView.findViewById(R.id.ll_sex_man).setOnClickListener(listener);
            contentView.findViewById(R.id.ll_sex_woman).setOnClickListener(listener);
            contentView.findViewById(R.id.cancel).setOnClickListener(listener);
            dialog.setContentView(contentView);
            Window window = dialog.getWindow();
            // 可以在此设置显示动画
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.x = 0;
            wl.y = ((Activity)mContext).getWindowManager().getDefaultDisplay().getHeight();
            // 以下这两句是为了保证按钮可以水平满屏
            wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
            wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            // 设置显示位置
            dialog.onWindowAttributesChanged(wl);
            return dialog;
        }
    }

}
