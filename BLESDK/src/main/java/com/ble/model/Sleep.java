package com.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuandong on 17-03-30.
 */
public class Sleep implements Parcelable{

    public String startTime;

    public int durationTime;

    /**睡眠类型 1-深睡 2-浅睡 3-清醒 4-放置在桌面*/
    public int type;

    public Sleep(){
        startTime = "";
        durationTime = 0;
        type = 0;
    }

    protected Sleep(Parcel in) {
        startTime = in.readString();
        durationTime = in.readInt();
        type = in.readInt();
    }

    public static final Creator<Sleep> CREATOR = new Creator<Sleep>() {
        @Override
        public Sleep createFromParcel(Parcel in) {
            return new Sleep(in);
        }

        @Override
        public Sleep[] newArray(int size) {
            return new Sleep[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(startTime);
        dest.writeInt(durationTime);
        dest.writeInt(type);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Sleep && ((Sleep) o).startTime.equals(startTime) && ((Sleep) o).durationTime == durationTime && ((Sleep) o).type == type;

    }

    @Override
    public String toString() {
        return "Sleep{" +
                "startTime='" + startTime + '\'' +
                ", durationTime=" + durationTime +
                ", type=" + type +
                '}';
    }
}
