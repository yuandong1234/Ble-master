package com.ble.common;

import android.Manifest;

/**
 * 一些蓝牙基础操作action意图
 * <p>
 * Created by yuandong on 2017/3/16 0016.
 */

public class BaseAction {
    /**
     * 限制广播的接收权限，必须有蓝牙权限才可以接收此广播
     */
    public final static String RECEIVE_BROADCAST_PERMISSION = Manifest.permission.BLUETOOTH;
    /**
     * 广播action 扫描设备超时
     */
    public final static String ACTION_DEVICE_SCAN_TIMEOUT = "com.ble.action_device_scan_timeout";
    /**
     * 广播action 设备正在连接
     */
    public final static String ACTION_DEVICE_CONNECTING = "com.ble.action_device_connecting";
    /**
     * 广播action 设备断开连接
     */
    public final static String ACTION_DEVICE_DISCONNECTED = "com.ble.action_device_disconnect";
    /**
     * 广播action 设备已连接
     */
    public final static String ACTION_DEVICE_CONNECTED = "com.ble.action_device_connect";
    /**
     * 广播action 设备连接超时
     */
    public final static String ACTION_BLE_CONNECT_TIME_OUT = "com.ble.action_ble_connect_time_out";
    /**
     * 广播action 设备连接失败
     */
    public final static String ACTION_BLE_CONNECT_FAILURE = "com.ble.action_ble_connect_failure";
    /**
     * 广播action 找到指定的服务和特征
     */
    public final static String ACTION_BLE_SERVICE_DISCOVER_SUCCESS = "com.ble.action_ble_discover_success";
    /**
     * 广播action 找不到指定的服务和特征
     */
    public final static String ACTION_BLE_SERVICE_DISCOVER_FAILURE = "com.ble.action_ble_discover_failure";
    /**
     * 广播action 订阅成功
     */
    public final static String ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_SUCCESS = "com.ble.action_ble_characteristic_subscribe_success";
    /**
     * 广播action 订阅失败
     */
    public final static String ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE = "com.ble.action_ble_characteristic_subscribe_failure";
    /**
     * 广播action 订阅超时
     */
    public final static String ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_TIMEOUT = "com.ble.action_ble_characteristic_subscribe_time_out";
    /**
     * 广播action 蓝牙发生错误
     */
    public final static String ACTION_BLE_ERROR = "com.ble.action_ble_error";
    /**
     * 广播action 写入超时
     */
    public final static String ACTION_BLE_WRITE_TIME_OUT = "com.ble.action_ble_write_time_out";

    /**
     * 广播action 写入失败
     */
    public final static String ACTION_BLE_WRITE_FAILURE = "com.ble.action_ble_write_failure";
    /**
     * 广播action 写入成功
     */
    public final static String ACTION_BLE_WRITE_SUCCESS = "com.ble.action_ble_write_success";
    /**
     * 广播action 读取超时
     */
    public final static String ACTION_BLE_READ_TIME_OUT = "com.ble.action_ble_read_time_out";
    /**
     * 广播action 读取失败
     */
    public final static String ACTION_BLE_READ_FAILURE = "com.ble.action_ble_read_failure";
    /**
     * 广播action 读取成功
     */
    public final static String ACTION_BLE_READ_SUCCESS = "com.ble.action_ble_read_success";
    /**
     * 广播action 发送命令请求超时（异常）
     */
    public final static String ACTION_BLE_SEND_COMMAND_TIME_OUT = "com.ble.action_ble_send_command_time_out";
    /**
     * 广播action 设备状态可使用（正常）
     */
    public final static String ACTION_BLE_STATE_AVAILABLE = "com.ble.action_ble_state_available";

    /**
     * 广播action 设备状态不可使用（异常）
     */
    public final static String ACTION_BLE_STATE_UNAVAILABLE = "com.ble.action_ble_state_unavailable";

    /**
     * 广播action 修改时间间隔时间成功
     */
    public final static String ACTION_BLE_SET_CONNECT_BLANK_SUCCESS = "com.ble.action_ble_set_connect_blank_success";


    /**
     * 广播action 修改时间间隔时间失败
     */
    public final static String ACTION_BLE_SET_CONNECT_BLANK_FAILURE = "com.ble.action_ble_set_connect_blank_failure";

    /**
     * 广播action 不同类型同步数据成功（运动、睡眠、心率）
     */
    public final static String ACTION_BLE_SYNC_DATA_SUCCESS = "com.ble.action_ble_sync_data_success";
    /**
     * 广播action 全部数据同步成功
     */
    public final static String ACTION_BLE_SYNC_TOTAL_DATA_SUCCESS = "com.ble.action_ble_sync_total_data_success";

}
