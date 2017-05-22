package com.yuandong.ble.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * 头像工具类（拍照 、相册查看）
 * Created by dong.yuan on 2017/5/19.
 */

public class PhotoUtil {
    public final static int ALBUM_REQUEST_CODE = 1;//相册
    public final static int CROP_REQUEST_CODE = 2;//裁切
    public final static int CAMERA_REQUEST_CODE = 3;//照相


    //拍照
    public static Uri toCamera(Activity activity) {
        String imagePath = FileUtil.toCreateImagePath();
        Uri uri = Uri.fromFile(new File(imagePath));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
        return uri;
    }

    //相册
    public static void toAlbum(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, "选择图片"), ALBUM_REQUEST_CODE);
    }

    //剪切
    public static void toCrop(Uri originalUri, Activity activity) {
        String imagePath = FileUtil.toCreateImagePath();
        Uri destinationUri = Uri.fromFile(new File(imagePath));
        //使用Ucrop进行剪切
        UcropUtil.startUcropWithUri(activity, originalUri, destinationUri);
        //使用Android系统剪切功能
       // UcropUtil.startCropWithUri(activity,originalUri);
    }

    //获得图片路径
    public static  String getImagePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
