package com.ble.utils;

import android.os.Parcel;

import com.ble.model.HeartRate;
import com.ble.model.Sleep;
import com.ble.model.Sport;


/**
 * 解析数据（同步运动，睡眠，心率）
 * Created by yuandong on 17-03-30.
 */
public class DataAnalyzeHelper {

    public static synchronized Sport analyzeSportsData(byte[] datas) {
        Parcel p = Parcel.obtain();
        Sport sport = Sport.CREATOR.createFromParcel(p);

        StringBuffer sTime = new StringBuffer();
        long durationTime = 0;

        //运动状态
        sport.type = datas[0];
//        int type = 0;
//        int tp = 0;
//        tp <<= 8;
//        type = datas[0] & 0xff;
//        tp |= type;
//        switch(tp) {
//            case 1://
//                sport.type = 0;
//                break;
//            case 2:
//                sport.type = 1;
//                break;
//        }
       // BleLog.i("sport.type = " + sport.type);
        // 开始时间
        for (int i = 1; i < datas.length - 7; i++) {
            int temp;
            int n = 0;
            n <<= 8;
            temp = datas[i] & 0xff;
            n |= temp;
            if (i == 1) {// 年b
//                if (n < 10) {
//                    sTime.append("200" + n);
//                } else {
                sTime.append("20" + n);
//                }
            } else if (i == 4) {// 小时
                if (n < 10) {
                    sTime.append(" 0" + n);
                } else {
                    sTime.append(" " + n);
                }

            } else if (i == 5 || i == 6) {// 分钟
                if (n < 10) {
                    sTime.append(":0" + n);
                } else if (n > 60) {
                    sTime.append(":59");
                } else {
                    sTime.append(":" + n);
                }
            } else {// 月，日
                if (n < 10) {
                    sTime.append("-0" + n);
                } else {
                    sTime.append("-" + n);
                }
            }
        }
        sport.startTime = sTime.toString();
        //BleLog.i("step.startTime = " + sport.startTime);

        // 持续时间
        for (int i = datas.length - 7; i < datas.length - 4; i++) {
            int temp;
            int n = 0;

            n <<= 8;
            temp = datas[i] & 0xff;
            n |= temp;
           // BleLog.i("持续时间n = " + n);
            if (i == 7) {
                durationTime = n * 60 * 60;
            } else if (i == 8) {
                durationTime += n * 60;
            } else if (i == 9) {
                durationTime += n;
            }
        }

        sport.durationTime = durationTime;
        sport.endTime = DateUtil.getEndTime(sport.startTime, sport.durationTime);
      //  BleLog.i("计算到秒的持续时间durationTime = " + durationTime);
       // BleLog.i("计算到秒的结束时间endTime = " + sport.endTime);
        // 总步数
        byte[] count = {datas[datas.length - 4], datas[datas.length - 3]};
        sport.countStep = byteReverseToInt(count);
       // BleLog.i("step.countStep = " + sport.countStep);
        // 标准卡路里
        byte[] calorieCount = {datas[datas.length - 2], datas[datas.length - 1]};
        sport.calorie = byteReverseToInt(calorieCount);
        //BleLog.i("step.calorie = " + sport.calorie);
        p.recycle();
        return sport;
    }

    /**
     * 解析睡眠数据
     *
     * @param datas 原始数据
     * @return
     */
    public static synchronized Sleep analyzeSleepData(byte[] datas) {
        Parcel p = Parcel.obtain();

        Sleep sleep = Sleep.CREATOR.createFromParcel(p);
        StringBuffer sTime = new StringBuffer();
        StringBuffer dTime = new StringBuffer();
        int durationTime;
        //睡眠状态
        int type;
        int tp = 0;
        tp <<= 8;
        type = datas[0] & 0xff;
        tp |= type;
        switch (tp) {
            case 1://
                sleep.type = 1;
                break;
            case 2:
                sleep.type = 0;
                break;
            case 3:
                sleep.type = 2;
                break;
            default:
                sleep.type = 3;
                break;
        }

        // 开始时间
        for (int i = 1; i < datas.length - 3; i++) {
            int temp;
            int n = 0;
            n <<= 8;
            temp = datas[i] & 0xff;
            n |= temp;

            if (i == 1) {// 年
                if (n < 10) {
                    sTime.append("200" + n);
                } else {
                    sTime.append("20" + n);
                }

            } else if (i == 4) {// 时

                if (n < 10) {
                    sTime.append(" 0" + n);
                } else {
                    sTime.append(" " + n);
                }

            } else if (i == 5 || i == 6) {// 分钟，秒
                if (n < 10) {
                    sTime.append(":0" + n);
                } else if (n > 60) {
                    sTime.append(":59");
                } else {
                    sTime.append(":" + n);
                }
            } else {// 月，日
                if (n < 10) {
                    sTime.append("-0" + n);
                } else {
                    sTime.append("-" + n);
                }
            }
        }

        sleep.startTime = sTime.toString();
       // BleLog.i( "睡眠开始时间 = " + sleep.startTime);
        byte[] durationBytes = {datas[7], datas[8]};
        durationTime = parseSleepDurationTime(durationBytes);
        sleep.durationTime = durationTime;
       // BleLog.i("睡眠时长  = " + sleep.durationTime);
        p.recycle();
        return sleep;
    }

    /**
     * 解析心率数据
     *
     * @param datas 数据数组
     * @return
     */
    public static HeartRate analyzeHeartRateData(byte[] datas) {
        Parcel p = Parcel.obtain();
        HeartRate hr = HeartRate.CREATOR.createFromParcel(p);

        int size = datas[0];
        //当传过来的值最高位为1时,转换会变为负数,需要处理
        if (size < 0) {
//            size = Math.abs(size) + 8;
            size = (128 - Math.abs(size)) + 128;
        }
        hr.size = size;
        int type = datas[1];
        hr.type = type;
        int measureType = datas[2];
        hr.measureType = measureType;
        byte[] surfaceTemp = {datas[3], datas[4]};

        float surfaceTempInt = byteReverseToInt(surfaceTemp) / 10.0f;
        if (surfaceTempInt < 0) {
            surfaceTempInt = 0;
        }
        hr.surfaceTem = surfaceTempInt;
        byte[] time = {datas[5], datas[6], datas[7], datas[8], datas[9], datas[10]};

        StringBuffer sTime = new StringBuffer();

        // 开始时间
        for (int i = 0; i < time.length; i++) {
            int temp;
            int n = 0;
            n <<= 8;
            temp = time[i] & 0xff;
            n |= temp;
            if (i == 0) {// 年
                sTime.append("20" + n);

            } else if (i == 3) {// 小时
                if (n < 10) {
                    sTime.append(" 0" + n);
                } else {
                    sTime.append(" " + n);
                }

            } else if (i == 4 || i == 5) {// 分钟
                if (n < 10) {
                    sTime.append(":0" + n);
                } else {
                    sTime.append(":" + n);
                }
            } else {// 月，日
                if (n < 10) {
                    sTime.append("-0" + n);
                } else {
                    sTime.append("-" + n);
                }
            }
        }
        //BleLog.i("history pulse : size = " + size + " type = " + type + " measureType = " + measureType + " surfaceTemp = " + surfaceTempInt + " time = " + sTime.toString());
        hr.testTime = sTime.toString();
        p.recycle();
        return hr;
    }

    /**
     * 高低位转换
     *
     * @param b
     * @return
     */
    public static int byteReverseToInt(byte[] b) {

        int mask = 0xff;
        int temp = 0;
        int n = 0;
        for (int i = b.length - 1; i > -1; i--) {
            n <<= 8;
            temp = b[i] & mask;
            n |= temp;
        }
        return n;
    }

    /**
     * 解析睡眠时长数组
     *
     * @param b 协议字段(精度到分钟,忽略秒字段)
     * @return
     */
    public static int parseSleepDurationTime(byte[] b) {
        int hour = toInt(b[0]);
        int min = toInt(b[1]);
        return hour * 60 + min;
    }

    /**
     * 16进制的转成10进制
     *
     * @param a
     * @return
     */
    private static int toInt(byte a) {
        int r = 0;
        r <<= 8;
        r |= (a & 0x000000ff);
        return r;
    }

    /**
     * 2进制的byte[]数组转int类型
     *
     * @param b
     * @return
     */
    public static int byteToInt(byte[] b) {

        int mask = 0xff;
        int temp = 0;
        int n = 0;
        for (int i = 0; i < b.length; i++) {
            n <<= 8;
            temp = b[i] & mask;
            n |= temp;
        }
        return n;
    }

}
