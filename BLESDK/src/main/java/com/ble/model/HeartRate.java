package com.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuandong on 17-03-30.
 */
public class HeartRate implements Parcelable{
    /**
     * 心率次数
     */
    public int size;

    /**
     * 健康水平：-1偏低、0理想、1正常、2偏快
     */
    public int result;

    /**
     * 检测模式 1：手动 2：自动
     */
	public int measureType;

    /**
     * 所处状态：0静止、1运动
     */
    public int type;

    /**
     * 体表温度：摄氏度
     */
    public float  surfaceTem;

    /**
     * 检测时间(yyyy-MM-dd HH:mm:ss)
     */
    public String testTime;

    public HeartRate(){
        size = 0;
        result = 0;
        measureType = 0;
        type = 0;
        surfaceTem = 0;
        testTime = "";
    }

    protected HeartRate(Parcel in) {
        size = in.readInt();
        result = in.readInt();
        measureType = in.readInt();
        type = in.readInt();
        surfaceTem = in.readFloat();
        testTime = in.readString();
    }

    public static final Creator<HeartRate> CREATOR = new Creator<HeartRate>() {
        @Override
        public HeartRate createFromParcel(Parcel in) {
            return new HeartRate(in);
        }

        @Override
        public HeartRate[] newArray(int size) {
            return new HeartRate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(size);
        dest.writeInt(result);
        dest.writeInt(measureType);
        dest.writeInt(type);
        dest.writeFloat(surfaceTem);
        dest.writeString(testTime);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof HeartRate && ((HeartRate) o).size == size &&
                ((HeartRate) o).result == result &&
                ((HeartRate) o).measureType == measureType &&
                ((HeartRate) o).type == type &&
                ((HeartRate) o).surfaceTem == surfaceTem &&
                ((HeartRate) o).testTime.equals(testTime);
    }

    @Override
    public String toString() {
        return "HeartRate{" +
                "size=" + size +
                ", result=" + result +
                ", measureType=" + measureType +
                ", type=" + type +
                ", surfaceTem=" + surfaceTem +
                ", testTime='" + testTime + '\'' +
                '}';
    }
}
