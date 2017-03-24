package com.ble.model;

import java.util.LinkedList;

/**
 * 蓝牙命令队列
 * Created by yuandong on 2017/3/23 0023.
 */

public class CommandQueue {
    /**
     * 切换通信通道（区分APP和微信）
     */
    public static  String SEND_CHANNEL = "send_channel";
    /**
     * 设置蓝牙发送间隔
     */
    public static String SET_SEND_BLANK = "set_send_blank";
    /**
     * 查询设备状态
     */
    public static final String QUERY_STATE = "query_state";
    /**
     * 设置蓝牙发送间隔
     */
    public static final String BASIC_INFO = "basic_info";
    /**
     * 查询运动数据
     */
    public static final String SYNC_SPORTS_DATA = "sync_sports_data";
    /**
     * 查询睡眠数据
     */
    public static final String SYNC_SLEEP_DATA = "sync_sleep_data";
    /**
     * 查询心率数据
     */
    public static final String SYNC_HEART_RATE_DATA = "sync_heart_rate_data";

    //获得命令队列
    public static LinkedList<String> getCommandQueue() {
        LinkedList<String> list = new LinkedList<>();
        list.add(SEND_CHANNEL);
        list.add(QUERY_STATE);
        list.add(SET_SEND_BLANK);
        list.add(BASIC_INFO);
        list.add(SYNC_SPORTS_DATA);
        list.add(SYNC_SLEEP_DATA);
//        list.add(SYNC_HEART_RATE_DATA);
        return list;
    }


}
