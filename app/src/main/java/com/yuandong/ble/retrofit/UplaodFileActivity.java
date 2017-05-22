package com.yuandong.ble.retrofit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.app.base.activity.BaseActivity;
import com.yalantis.ucrop.UCrop;
import com.yuandong.ble.R;
import com.yuandong.ble.util.PhotoUtil;
import com.yuandong.ble.widget.BottomDialog;

public class UplaodFileActivity extends BaseActivity {

    private static String TAG = UplaodFileActivity.class.getSimpleName();
    private static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    private BottomDialog dialog;
    private Uri originalUri;

    private ImageView imageView;
    private Button button;

    @Override
    protected void initParams(Bundle params) {

    }

    @Override
    protected View bindView() {
        return null;
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_uplaod_file;
    }

    @Override
    protected void initView(View view) {
        button = (Button) view.findViewById(R.id.upload);
        imageView = (ImageView) view.findViewById(R.id.image);
        initDialog();
    }

    @Override
    protected void initListener() {
        button.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    private void initDialog() {
        View dialogView = View.inflate(this, R.layout.layout_bottom_dialog, null);
        dialog = new BottomDialog.Builder(this)
                .setContentView(dialogView)
                .setListener(this)
                .create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload:
                if (!dialog.isShowing()) {
                    dialog.show();
                }
                break;
            case R.id.ll_sex_man:
                dialog.dismiss();
                originalUri = PhotoUtil.toCamera(this);
                break;
            case R.id.ll_sex_woman:
                dialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_READ_ACCESS_PERMISSION);
                } else {
                    PhotoUtil.toAlbum(this);
                }

                break;
            case R.id.cancel:
                dialog.dismiss();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PhotoUtil.CAMERA_REQUEST_CODE:
                    //在指定Uri的情况下，data返回为null
                    Log.e(TAG, "照相成功 :" + originalUri.getPath());
                    PhotoUtil.toCrop(originalUri, this);
                    break;
                case PhotoUtil.ALBUM_REQUEST_CODE:
                    if (data != null) {
                        try {
                            Uri uri = data.getData();
                            Log.e(TAG, "从相册获取成功 :" + uri.getPath());
                            PhotoUtil.toCrop(uri, this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    if (data != null) {
                        Uri resultUri = UCrop.getOutput(data);
                        Log.e(TAG, "图片剪切成功 : " + resultUri.getPath());
                        disPlayImage(resultUri);
                    }
                    break;
                case UCrop.RESULT_ERROR:
                    Throwable cropError = UCrop.getError(data);
                    Log.e(TAG, "图片剪切失败 :" + cropError.toString());
                    break;
                case PhotoUtil.CROP_REQUEST_CODE:
                    if (data != null) {
                        Uri resultUri = data.getData();
                        Log.e(TAG, "系统图片剪切成功 :" + resultUri.getPath());
                        disPlayImage(resultUri);
                    }
                    break;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PhotoUtil.toAlbum(this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void disPlayImage(Uri uri) {
        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
        imageView.setImageBitmap(bitmap);
    }

}
