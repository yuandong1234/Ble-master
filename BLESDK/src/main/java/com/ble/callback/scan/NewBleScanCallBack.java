package com.ble.callback.scan;

import android.annotation.TargetApi;
import android.bluetooth.le.*;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.bluetooth.le.ScanCallback;

import com.ble.BleManager;
import com.ble.common.State;
import com.ble.model.BleDevice;

import java.util.List;

/**
 * 设备扫描回调（针对API 21及21以上）
 * Created by yuandong on 2017/3/7 0007.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public abstract class NewBleScanCallBack extends ScanCallback {
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected BleManager bleManager;
    protected List<ScanFilter> filters;
    protected ScanSettings settings;
    protected int scanTimeout = -1; //-1表示一直扫描
    protected boolean isScan = true;
    protected boolean isScanning = false;

    public BleManager getViseBluetooth() {
        return bleManager;
    }

    public NewBleScanCallBack setViseBluetooth(BleManager bleManager) {
        this.bleManager = bleManager;
        return this;
    }

    public NewBleScanCallBack setScanTimeout(int scanTimeout) {
        this.scanTimeout = scanTimeout;
        return this;
    }

    public NewBleScanCallBack setScan(boolean scan) {
        isScan = scan;
        return this;
    }

    public NewBleScanCallBack setFilters(List<ScanFilter> filters) {
        this.filters = filters;
        return this;
    }

    public NewBleScanCallBack setSettings(ScanSettings settings) {
        this.settings = settings;
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
                            bleManager.stopLeScan(NewBleScanCallBack.this);
                        }
                        scanTimeout();
                    }
                }, scanTimeout);
            }
            isScanning = true;
            if (bleManager != null) {
                if (filters != null) {
                    bleManager.startLeScan(filters, settings, NewBleScanCallBack.this);
                } else {
                    bleManager.startLeScan(NewBleScanCallBack.this);
                }
            }
        } else {
            isScanning = false;
            if (bleManager != null) {
                bleManager.stopLeScan(NewBleScanCallBack.this);
            }
        }
    }

    public NewBleScanCallBack removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
        return this;
    }

    @Override
    public void onScanResult(int callbackType,final ScanResult result) {
        if (result == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                onDeviceFound(new BleDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis()));
            }
        });
    }

    public abstract void scanTimeout();

    public abstract void onDeviceFound(BleDevice bleDevice);
}
