package com.ble.utils;

import com.ble.common.BleCommand;

/**
 * 命令协议
 * Created by yuandong on 2017/3/23 0023.
 */

public class CommandProtocol {
    /**
     * 构建 指定类型的请求数据协议
     *
     * @param type 请求数据类型
     * @return
     */
    private static byte[] getRequestProtocolByType(byte type) {
        byte[] tempProtocol = {0x55, (byte) 0xAA, 0x02, (byte) 0x09, type, 0x00};

        tempProtocol[5] = (byte) (tempProtocol[2] + tempProtocol[3] + tempProtocol[4]);
        return tempProtocol;
    }

    /**
     * 构建 查询手环工作状态 协议
     */
    public static byte[] getQueryBleStateProtocol() {
        return getRequestProtocolByType(BleCommand.QUERY_STATE);
    }

    /**
     * 构建心跳包协议
     *
     * @return
     */
    public static byte[] getHeartBeatProtocol() {
        return getRequestProtocolByType(BleCommand.HEART_BEAT);
    }

    /**
     * 构建请求 同步运动数据协议
     *
     * @return
     */
    public static byte[] getSyncSportsDataProtocol() {
        return getRequestProtocolByType(BleCommand.SPORTS);
    }

    /**
     * 构建请求 同步睡眠数据协议
     *
     * @return
     */
    public static byte[] getSyncSleepDataProtocol() {
        return getRequestProtocolByType(BleCommand.SLEEP);
    }

    /**
     * 构建请求同步心率数据协议
     *
     * @return
     */
    public static byte[] getSyncHeartRateDataProtocol() {
        return getRequestProtocolByType(BleCommand.HEART_RATE);
    }

    /**
     * 构建 允许发送指定数据 协议
     *
     * @param type 数据类型
     * @return
     */
    public static byte[] getAllowToSyncProtocol(byte type) {
        byte[] tempProtocol = {0x55, (byte) 0xAA, 0x02, (byte) 0xE9, (byte) 0x00, 0x00};
        tempProtocol[4] = type;
        tempProtocol[5] = (byte) (tempProtocol[2] + tempProtocol[3] + tempProtocol[4]);
        return tempProtocol;
    }

    /**
     * 构建 接收数据正确 协议（每一帧数据）
     *
     * @param type 数据类型
     * @return
     */
    public static byte[] getReceiveDataCorrectProtocol(byte type) {
        byte[] tempProtocol = {0x55, (byte) 0xAA, 0x02, (byte) 0xD1, type, (byte) 0x00};
        tempProtocol[5] = (byte) (tempProtocol[2] + tempProtocol[3] + tempProtocol[4]);
        return tempProtocol;
    }
    /**
     * 构建请求重发数据协议
     * @param type  请求数据类型
     * @return
     */
    public static byte[] getRequestResendProtocol(byte type) {
        byte[] tempProtocal = { 0x55, (byte) 0xAA, 0x02, (byte) 0xC1, type,
                0x00 };
        tempProtocal[5] = (byte) (tempProtocal[2] + tempProtocal[3] + tempProtocal[4]);
        return tempProtocal;
    }
    /**
     * 构建 回复接收指定数据成功 协议（全部数据接收完成）
     *
     * @param type 数据类型
     * @return
     */
    public static byte[] getReceiveSyncDataCorrectProtocol(byte type) {
        byte[] tempProtocol = {0x55, (byte) 0xAA, 0x02, (byte) 0xB1, type, 0x00};
        tempProtocol[5] = (byte) (tempProtocol[2] + tempProtocol[3] + tempProtocol[4]);
        return tempProtocol;
    }

    /**
     * 构建设置蓝牙传输间隔协议
     *
     * @param byte1 时间字段(内存低位)
     * @param byte2 时间字段(内存高位)
     * @return
     */
    public static byte[] getSetCommandBlankProtocol(byte byte1, byte byte2) {
        byte[] tempProtocol = new byte[]{0x55, (byte) 0xAA, 0x04, 0x09, BleCommand.CONNECT_BLANK,
                byte1, byte2, 0x00};
        tempProtocol[7] = (byte) (tempProtocol[2] + tempProtocol[3]
                + tempProtocol[4] + tempProtocol[5] + tempProtocol[6]);
        return tempProtocol;
    }

}
