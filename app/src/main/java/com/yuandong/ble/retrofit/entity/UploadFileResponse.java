package com.yuandong.ble.retrofit.entity;

/**
 * Created by dong.yuan on 2017/5/23.
 */

public class UploadFileResponse {
    public String currentTime;
    public int code;
    public String message;
    public Photo data;

    public class Photo {
        public String thumb;
        public String url;

        @Override
        public String toString() {
            return "Photo{" +
                    "thumb='" + thumb + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UploadFileResponse{" +
                "currentTime='" + currentTime + '\'' +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
