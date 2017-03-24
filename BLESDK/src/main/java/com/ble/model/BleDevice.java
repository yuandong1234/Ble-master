package com.ble.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 蓝牙设备信息
 * Created by yuandong on 2017/3/7.
 */

public class BleDevice implements Parcelable {
    protected static final int MAX_RSSI_LOG_SIZE = 10;
    private static final long LOG_INVALIDATION_THRESHOLD = 10 * 1000;
    private BluetoothDevice mDevice;
    private Map<Long, Integer> mRssiLog;
    private int mFirstRssi;
    private long mFirstTimestamp;
    private int mCurrentRssi;
    private long mCurrentTimestamp;
    private byte[] mScanRecord;

    public BleDevice() {
    }

    public BleDevice(BluetoothDevice mDevice, int rssi,  byte[] mScanRecord,long timestamp) {
        this.mDevice = mDevice;
        this.mFirstRssi = rssi;
        this.mFirstTimestamp = timestamp;
        this.mScanRecord = mScanRecord;
        mRssiLog = new LinkedHashMap<>(MAX_RSSI_LOG_SIZE);
        updateRssi(timestamp, rssi);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mDevice, flags);
        dest.writeInt(this.mRssiLog.size());
        for (Map.Entry<Long, Integer> entry : this.mRssiLog.entrySet()) {
            dest.writeValue(entry.getKey());
            dest.writeValue(entry.getValue());
        }
        dest.writeInt(this.mFirstRssi);
        dest.writeLong(this.mFirstTimestamp);
        dest.writeInt(this.mCurrentRssi);
        dest.writeLong(this.mCurrentTimestamp);
    }


    protected BleDevice(Parcel in) {
        this.mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        int mRssiLogSize = in.readInt();
        this.mRssiLog = new HashMap<Long, Integer>(mRssiLogSize);
        for (int i = 0; i < mRssiLogSize; i++) {
            Long key = (Long) in.readValue(Long.class.getClassLoader());
            Integer value = (Integer) in.readValue(Integer.class.getClassLoader());
            this.mRssiLog.put(key, value);
        }
        this.mFirstRssi = in.readInt();
        this.mFirstTimestamp = in.readLong();
        this.mCurrentRssi = in.readInt();
        this.mCurrentTimestamp = in.readLong();
    }


    public void updateRssi(long timestamp, int rssiReading) {
        synchronized (mRssiLog) {
            if (timestamp - mCurrentTimestamp > LOG_INVALIDATION_THRESHOLD) {
                mRssiLog.clear();
            }
            mCurrentRssi = rssiReading;
            mCurrentTimestamp = timestamp;
            mRssiLog.put(timestamp, rssiReading);
        }
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public Map<Long, Integer> getmRssiLog() {
        return mRssiLog;
    }

    public int getmFirstRssi() {
        return mFirstRssi;
    }

    public long getmFirstTimestamp() {
        return mFirstTimestamp;
    }

    public int getmCurrentRssi() {
        return mCurrentRssi;
    }

    public long getmCurrentTimestamp() {
        return mCurrentTimestamp;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }
    public String getAddress() {
        return mDevice.getAddress();
    }
    public String getName() {return mDevice.getName();
    }
    public String getBleBondStatue(){
        return getBondState(mDevice.getBondState());
    }

    private static String getBondState(final int bondState) {
        switch (bondState) {
            case BluetoothDevice.BOND_BONDED://已配对
                return "Paired";
            case BluetoothDevice.BOND_BONDING://配对中
                return "Pairing";
            case BluetoothDevice.BOND_NONE://未配对
                return "UnBonded";
            default:
                return "Unknown";//未知状态
        }
    }

    public static final Parcelable.Creator<BleDevice> CREATOR = new Parcelable.Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel source) {
            return new BleDevice(source);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
