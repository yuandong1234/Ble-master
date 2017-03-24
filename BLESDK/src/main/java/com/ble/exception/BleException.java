package com.ble.exception;



import com.ble.common.BleExceptionCode;
import java.io.Serializable;

/**BLE异常基类
 * Created by yuandong on 2017/3/8..
 */
public class BleException implements Serializable {
    private BleExceptionCode code;
    private String description;

    public BleException(BleExceptionCode code, String description) {
        this.code = code;
        this.description = description;
    }

    public BleExceptionCode getCode() {
        return code;
    }

    public BleException setCode(BleExceptionCode code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public BleException setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "BleException{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
