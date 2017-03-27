package com.ble.common;

/**
 * 手环请求命令字
 * Created by yuandong on 2017/3/23 0023.
 */

public class BleCommand {

    /**
     * 查询手环工作状态
     */
    public final static byte QUERY_STATE = (byte) 0xA2;
    /**
     * 基本信息
     */
    public final static byte BASIC_INFO = (byte) 0xB2;
    /**
     * 手环回复为数据长度
     */
    public final static byte RECEIVE_RESPONSE_DATA_LENGTH = (byte) 0xF9;
    /**
     * 接收正确
     */
    public final static byte RECEIVE_CORRECT = (byte) 0xD1;
    /**
     * 手环回复为数据传输完毕
     */
    public final static byte RECEIVE_DATA_END = (byte) 0xB9;
    /**
     * 运动
     */
    public final static byte SPORTS = (byte) 0xC1;
    /**
     * 睡眠
     */
    public final static byte SLEEP = (byte) 0xC2;
    /**
     * 心率
     */
    public final static byte HEART_RATE = (byte) 0xCC;
    /**
     * 更改连接间隔
     */
    public final static byte CONNECT_BLANK = (byte) 0xBA;
    /**
     * 心跳包
     */
    public final static byte HEART_BEAT = (byte) 0xBF;
    /**
     * 握手 切换通道
     */
    public final static byte CLASP_HANDS = (byte) 0x01;
}
