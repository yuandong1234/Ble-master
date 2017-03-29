package com.ble.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ble.common.BleCommand;
import com.ble.model.BLeProtocol;

/**
 * 数据处理类（数据校验）
 * Created by yuandong on 2017/3/27.
 */

public class BleDataUtil {

    private final static String TAG = BleDataUtil.class.getSimpleName();
    public static final int MSG_QUERY_BLE_STATE = 100000;
    public static final int MSG_DATA_CHECK_ERROR = 100001;
    public static final int MSG_SET_CONNECT_BLANK = 100002;

    private static BleDataUtil bleDataUtil;
    private Handler handler;

    public BleDataUtil(Handler handler) {
        this.handler = handler;
    }

    public static BleDataUtil getInstance(Handler handler) {
        if (bleDataUtil == null) {
            synchronized (BleDataUtil.class) {
                if (bleDataUtil == null) {
                    bleDataUtil = new BleDataUtil(handler);
                }
            }
        }
        return bleDataUtil;
    }

    /**
     * 处理接收的原始数据
     *
     * @param data 蓝牙读取的原始byte数组数据
     */
    public boolean handleReceiveData(byte[] data) {
        if (data == null) {
            Log.e(TAG, "错误：数据为空");
            handleIncorrectData();
            return false;
        }

        //数据长度不对,丢弃
        if (data.length < 6) {
            Log.e(TAG, "错误：长度小于 6");
            handleIncorrectData();
            return false;
        }

        //数据头不是以AA55开头,丢弃
        if (data[0] != -86 || data[1] != 85) {
            Log.e(TAG, "错误：不是以AA55开头 ");
            handleIncorrectData();
            return false;
        }

        boolean success = confirmDataCheckDigit(data);
        Log.i(TAG, "数据校验：" + success);
        if (success) {
            handleCorrectData(data);
        } else {
            handleIncorrectData();
        }
        return success;
    }

    /**
     * 处理校验错误的数据
     */
    private void handleIncorrectData() {
        sendBleMessage(MSG_DATA_CHECK_ERROR, null);
    }

    /**
     * 校验数据校验位
     *
     * @param data 数据
     * @return true 校验通过       false 校验失败
     */
    private boolean confirmDataCheckDigit(byte[] data) {
        //数据域长度位
        byte length = data[2];
        //数据总长度位
        byte totalLength = (byte) (length + 4);

        if (totalLength < 0) {
            totalLength += 128;
        }

        if (data.length < totalLength) {
            return false;
        }

        //总数据校验位
        byte check = data[totalLength - 1];
        //总数据校验位开始位置
        byte checktmp = data[2];
        //从总数据校验位开始位置开始计算,到最后一位累加
        for (int i = 3; i < totalLength - 1; i++) {
            checktmp += data[i];
        }
        //总数据校验位是否正确
        return check == checktmp;
    }

    /**
     * 处理校验正确的数据
     *
     * @param data 数据
     */

    private void handleCorrectData(byte[] data) {
        BLeProtocol bLeProtocol = new BLeProtocol(data);
        switch (bLeProtocol.getCommand()) {
            case BleCommand.QUERY_STATE://设备状态
                //重置当前的校验数据的次数
                CommandUtil.CURRENT_DATA_CHECK_ERROR_TIMES = 0;
                sendBleMessage(MSG_QUERY_BLE_STATE, bLeProtocol.getDatas()[0]);
                break;
            case BleCommand.CONNECT_BLANK:
                //重置当前的校验数据的次数
                CommandUtil.CURRENT_DATA_CHECK_ERROR_TIMES = 0;
                sendBleMessage(MSG_SET_CONNECT_BLANK, bLeProtocol.getDatas()[0]);
                break;
            case BleCommand.SPORTS:
                //TODO 校验成功的情况下，先重置当前的校验数据的次数
                break;
            case BleCommand.SLEEP:
                //TODO 校验成功的情况下，先重置当前的校验数据的次数
                break;
            case BleCommand.HEART_RATE:
                //TODO 校验成功的情况下，先重置当前的校验数据的次数
                break;
        }
    }

    /**
     * 发信息
     *
     * @param what
     */
    private void sendBleMessage(int what, Object object) {
        Message msg = handler.obtainMessage(what);
        if (object != null) {
            msg.obj = object;
        }
        handler.sendMessage(msg);
    }
}