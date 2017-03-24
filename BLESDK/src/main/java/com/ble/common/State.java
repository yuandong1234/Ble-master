package com.ble.common;
/**
 * 蓝牙状态描述
 * Created by yuandong on 2017/3/7.
 */
public enum  State {
    SCAN_PROCESS(0x01),
    SCAN_SUCCESS(0x02),
    SCAN_TIMEOUT(0x03),
    CONNECT_PROCESS(0x04),
    CONNECT_SUCCESS(0x05),
    CONNECT_FAILURE(0x06),
    CONNECT_TIMEOUT(0x07),
    DISCONNECT(0x08),
    WRITE_PROCESS(0x09),
    WRITE_TIMEOUT(0x0A),
    WRITE_SUCCESS(0x0B),
    WRITE_FAILURE(0x0C),
    READ_PROCESS(0x0D),
    READ_TIMEOUT(0x0E),
    READ_SUCCESS(0x0F),
    READ_FAILURE(0x10);


    private int code;

    State(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
