package com.ble.callback.scan;


import android.bluetooth.BluetoothDevice;

import com.ble.common.State;
import com.ble.model.BleDevice;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 根据设备mac地址扫描回调（针对API<18~20>）
 * Created by yuandong on 2017/3/7.
 */

public abstract class MacBleScanCallback extends BleScanCallback {

    private String mac;
    private AtomicBoolean hasFound = new AtomicBoolean(false);

    public MacBleScanCallback(String mac) {
        super();
        this.mac = mac;
        if (mac == null) {
            throw new IllegalArgumentException("start scan, mac can not be null!");
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!hasFound.get()) {
            if (device != null && device.getAddress() != null && mac.equalsIgnoreCase(device.getAddress().trim())) {
                hasFound.set(true);
                if (bleManager != null) {
                    bleManager.stopLeScan(MacBleScanCallback.this);
                    bleManager.setState(State.SCAN_SUCCESS);
                }
                onDeviceFound(new BleDevice(device, rssi, scanRecord, System.currentTimeMillis()));
            }
        }
    }

}
