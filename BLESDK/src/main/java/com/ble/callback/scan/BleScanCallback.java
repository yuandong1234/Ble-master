package com.ble.callback.scan;


import android.os.Handler;
import android.os.Looper;

import com.ble.BleManager;
import com.ble.common.State;
import com.ble.model.BleDevice;
import com.ble.utils.BleLog;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter.LeScanCallback;

/**
 * 设备扫描回调（针对API<18~20>）
 * Created by yuandong on 2017/3/7.
 */

public abstract class BleScanCallback implements LeScanCallback {

    protected Handler handler = new Handler(Looper.getMainLooper());
    protected BleManager bleManager;
    protected int scanTimeout = -1; //-1表示一直扫描
    protected boolean isScan = true;
    protected boolean isScanning = false;

    public BleManager getBleManager() {
        return bleManager;
    }

    public BleScanCallback setBleManager(BleManager bleManager) {
        this.bleManager = bleManager;
        return this;
    }

    public BleScanCallback setScanTimeout(int scanTimeout) {
        this.scanTimeout = scanTimeout;
        return this;
    }

    public BleScanCallback setScan(boolean scan) {
        isScan = scan;
        return this;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public int getScanTimeout() {
        return scanTimeout;
    }

    public void scan() {
        if (isScan) {
            if (isScanning) {
                return;
            }
            if (scanTimeout > 0) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isScanning = false;
                        if (bleManager != null) {
                            bleManager.setState(State.SCAN_TIMEOUT);
                            bleManager.stopLeScan(BleScanCallback.this);
                        }
                        scanTimeout();
                    }
                }, scanTimeout);
            }
            isScanning = true;
            if (bleManager != null) {
                bleManager.startLeScan(BleScanCallback.this);
            }
        } else {
            isScanning = false;
            if (bleManager != null) {
                bleManager.stopLeScan(BleScanCallback.this);
            }
        }
    }

    public BleScanCallback removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
        return this;
    }

    @Override
    public void onLeScan(final BluetoothDevice bluetoothDevice, final int rssi, final byte[] scanRecord) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onDeviceFound(new BleDevice(bluetoothDevice, rssi, scanRecord, System.currentTimeMillis()));
            }
        });
    }
    public abstract void scanTimeout();

    public abstract void onDeviceFound(BleDevice bleDevice);

}
