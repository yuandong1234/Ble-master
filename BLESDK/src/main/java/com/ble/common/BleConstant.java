package com.ble.common;

/**
 * BLE常量
 * Created by yuandong on 2017/3/9..
 */
public class BleConstant {
    //G1手环
    public static String SERVICE_UUID = "0000fee7-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String READ_G1_UUID = "0000fff4-0000-1000-8000-00805f9b34fb";//读，需要添加订阅
    public static String WRITE_G1_UUID = "0000fec7-0000-1000-8000-00805f9b34fb";//写
    public static String WRITE_CLASP_HANDS_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";//可读也可写
}
