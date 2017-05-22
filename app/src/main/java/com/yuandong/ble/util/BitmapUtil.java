package com.yuandong.ble.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 获得BitMap的方法
 * Created by yuandong on 2017/5/21.
 */

public class BitmapUtil {

    public static final float DISPLAY_WIDTH = 300;
    public static final float DISPLAY_HEIGHT = 300;

    /**
     * 从path中获取图片信息,并且根据比列进行缩放
     *
     * @param path 图片路径
     * @return
     */
    public static Bitmap decodeBitmap(String path) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        //inJustDecodeBounds
        //If set to true, the decoder will return null (no bitmap), but the out…
        op.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, op); //获取尺寸信息
        //获取比例大小
        int wRatio = (int) Math.ceil(op.outWidth / DISPLAY_WIDTH);
        int hRatio = (int) Math.ceil(op.outHeight / DISPLAY_HEIGHT);
        //如果超出指定大小，则缩小相应的比例
        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }
        op.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path, op);
        return bmp;
    }

    /**
     *
     * @param filePath 图片路径
     * @param quality 图片压缩质量
     * @return
     */
    public static String compressImage(String filePath, int quality)  {
        Bitmap bm = decodeBitmap(filePath);//获取一定尺寸的图片
        File outputFile=new File(FileUtil.toCreateImagePath());
        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }catch (Exception e){}
        return outputFile.getPath();
    }

}
