package com.ble.callback.scan;


import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.ble.BleManager;
import com.ble.common.State;
import com.ble.model.BleDevice;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 根据设备名扫描回调（针对API<18~20>）
 * Created by yuandong on 2017/3/7.
 */

public abstract class NameBleScanCallback extends BleScanCallback {

    private String name;
    private AtomicBoolean hasFound = new AtomicBoolean(false);

    public NameBleScanCallback(String name) {
        super();
        this.name = name;
        if (name == null) {
            throw new IllegalArgumentException("start scan, name can not be null!");
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!hasFound.get()) {
            if (device != null && device.getName() != null && name.equalsIgnoreCase(device.getName().trim())) {
                hasFound.set(true);
                if (bleManager != null) {
                    bleManager.stopLeScan(NameBleScanCallback.this);
                    bleManager.setState(State.SCAN_SUCCESS);
                }
                onDeviceFound(new BleDevice(device, rssi, scanRecord, System.currentTimeMillis()));
            }
        }
    }

}
