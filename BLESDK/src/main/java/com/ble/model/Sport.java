package com.ble.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuandong on 17-03-30.
 */
public class Sport implements Parcelable{
    /**
     * 开始时间
     */
    public String startTime;

    /**
     * 持续时间
     */
    public long durationTime;

    /**
     * 结束时间
     */
    public String endTime;

    /**
     * 步数
     */
    public int countStep;

    /**
     *  运动类型 0：走路 1：跑步
     */
    public int type;

    /**
     * 标准卡路里
     */
    public int calorie;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.startTime);
        dest.writeLong(this.durationTime);
        dest.writeString(this.endTime);
        dest.writeInt(this.countStep);
        dest.writeInt(this.type);
        dest.writeInt(this.calorie);
    }

    public Sport() {
    }

    protected Sport(Parcel in) {
        this.startTime = in.readString();
        this.durationTime = in.readLong();
        this.endTime = in.readString();
        this.countStep = in.readInt();
        this.type = in.readInt();
        this.calorie = in.readInt();
    }

    public static final Creator<Sport> CREATOR = new Creator<Sport>() {
        @Override
        public Sport createFromParcel(Parcel source) {
            return new Sport(source);
        }

        @Override
        public Sport[] newArray(int size) {
            return new Sport[size];
        }
    };

    @Override
    public String toString() {
        return "Sport{" +
                "startTime='" + startTime + '\'' +
                ", durationTime=" + durationTime +
                ", endTime='" + endTime + '\'' +
                ", countStep=" + countStep +
                ", type=" + type +
                ", calorie=" + calorie +
                '}';
    }
}
