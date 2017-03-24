package com.ble.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuandong on 2017/3/8 0008.
 */

public class BleDeviceStore {
    private final Map<String, BleDevice> mDeviceMap;

    public BleDeviceStore() {
        mDeviceMap = new HashMap<>();
    }

    public void addDevice(BleDevice device) {
        if (device == null) {
            return;
        }
        if (mDeviceMap.containsKey(device.getAddress())) {
            mDeviceMap.get(device.getAddress()).updateRssi(device.getmCurrentTimestamp(), device.getmCurrentRssi());
        } else {
            mDeviceMap.put(device.getAddress(), device);
        }
    }

    public void removeDevice(BleDevice device){
        if (device == null) {
            return;
        }
        if(mDeviceMap.containsKey(device.getAddress())){
            mDeviceMap.remove(device.getAddress());
        }
    }

    public void clear() {
        mDeviceMap.clear();
    }

    public Map<String, BleDevice> getDeviceMap() {
        return mDeviceMap;
    }

    public List<BleDevice> getDeviceList() {
         List<BleDevice> methodResult = new ArrayList<>(mDeviceMap.values());

        Collections.sort(methodResult, new Comparator<BleDevice>() {

            @Override
            public int compare( BleDevice arg0,  BleDevice arg1) {
                return arg0.getAddress().compareToIgnoreCase(arg1.getAddress());
            }
        });

        return methodResult;
    }
}
