package com.ble.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by yaundong on 2017/3/9 0009.
 */

public class DateUtil {
    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
        String time = dateFormat.format(new Date());
        return time;
    }

    /**
     * 计算走路结束时间
     *
     * @param startTime    开始时间
     * @param durationTime 持续时间
     * @return
     */
    public static String getEndTime(String startTime, long durationTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long dayDiff = 0;
        String endTime = null;
        try {
            long startT = format.parse(startTime).getTime();
            dayDiff = (startT + durationTime * 1000);

            Date dat = new Date(dayDiff);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(dat);
            SimpleDateFormat format1 = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            endTime = format1.format(gc.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return endTime;
    }

}
