package com.ble.utils;

/**
 * 蓝牙命令工具类（用于处理命令发送,记录当前的ble状态）
 * Created by yuandong on 2017/3/23 0023.
 */

public class CommandUtil {

    /**
     * 当前发送指令
     */
    private byte[] currentComm;
    /**
     * 当前发送指令类型
     */
    private byte currentCommType;

    /**
     * 当前蓝牙发送状态 无操作
     */
    private static int STATE_DEFAULT = 10000;

    /**
     * 当前蓝牙发送状态 查询手环工作状态
     */
    private static int STATE_QUERY_BLE_STATE = 10001;


    /**
     * 当前蓝牙发送状态 发送 接收数据 正确
     */
    private static int STATE_SEND_RECEIVE_DATA_CORRECT = 10002;
    /**
     * 当前蓝牙发送状态 发送 同步数据 成功
     */
    private static int STATE_SEND_SYNC_DATA_SUCCESS = 10003;
    /**
     * 当前蓝牙发送状态 发送 删除数据 命令
     */
    private static int STATE_SEND_DELETE_DATA_COMM = 10004;


    /**
     * 当前蓝牙发送状态 发送请求同步运动数据
     */
    private static int STATE_REQUEST_SYNC_SPORTS = 10005;
    /**
     * 当前蓝牙发送状态 发送允许同步运动数据
     */
    private static int STATE_ALLOW_TO_SYNC_SPORTS = 10006;


    /**
     * 当前蓝牙发送状态 发送请求同步睡眠数据
     */
    private static final int STATE_REQUEST_SYNC_SLEEP = 10007;
    /**
     * 当前蓝牙发送状态 发送允许同步睡眠数据
     */
    private static final int STATE_ALLOW_TO_SYNC_SLEEP = 10008;


    /**
     * 当前蓝牙发送状态 发送请求同步心率数据
     */
    private static final int STATE_REQUEST_SYNC_HEART_RATE = 10009;
    /**
     * 当前蓝牙发送状态 发送允许同步心率数据
     */
    private static final int STATE_ALLOW_TO_SYNC_HEART_RATE = 10010;

    /**
     * 蓝牙操作状态（默认无操作状态）
     */
    public static int CURRENT_STATE = STATE_DEFAULT;







}
