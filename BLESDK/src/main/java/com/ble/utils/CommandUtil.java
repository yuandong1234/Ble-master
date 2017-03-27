package com.ble.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Message;

import com.ble.BleManager;
import com.ble.common.BaseAction;
import com.ble.common.BleCommand;
import com.ble.common.BleConstant;
import com.ble.model.CommandQueue;

import java.util.LinkedList;

/**
 * 蓝牙命令工具类（用于处理命令发送、记录当前的发送状态、以及重发命令）
 * Created by yuandong on 2017/3/23 0023.
 */

public class CommandUtil {
    private final static String TAG = CommandUtil.class.getSimpleName();
    /**
     * 当前蓝牙发送状态 无操作
     */
    public static int STATE_DEFAULT = 10000;
    /**
     * 当前蓝牙发送状态 查询手环工作状态
     */
    public static int STATE_QUERY_BLE_STATE = 10001;
    /**
     * 当前蓝牙发送状态 发送 接收数据 正确
     */
    public static int STATE_SEND_RECEIVE_DATA_CORRECT = 10002;
    /**
     * 当前蓝牙发送状态 发送 同步数据 成功
     */
    public static int STATE_SEND_SYNC_DATA_SUCCESS = 10003;
    /**
     * 当前蓝牙发送状态 发送 删除数据 命令
     */
    public static int STATE_SEND_DELETE_DATA_COMM = 10004;
    /**
     * 当前蓝牙发送状态 发送请求同步运动数据
     */
    public static int STATE_REQUEST_SYNC_SPORTS = 10005;
    /**
     * 当前蓝牙发送状态 发送允许同步运动数据
     */
    public static int STATE_ALLOW_TO_SYNC_SPORTS = 10006;
    /**
     * 当前蓝牙发送状态 发送请求同步睡眠数据
     */
    public static final int STATE_REQUEST_SYNC_SLEEP = 10007;
    /**
     * 当前蓝牙发送状态 发送允许同步睡眠数据
     */
    public static final int STATE_ALLOW_TO_SYNC_SLEEP = 10008;
    /**
     * 当前蓝牙发送状态 发送请求同步心率数据
     */
    public static final int STATE_REQUEST_SYNC_HEART_RATE = 10009;
    /**
     * 当前蓝牙发送状态 发送允许同步心率数据
     */
    public static final int STATE_ALLOW_TO_SYNC_HEART_RATE = 10010;
    /**
     * 当前蓝牙发送状态 发送设置蓝牙传输间隔
     */
    public static final int STATE_SET_CONNECT_BLANK = 10011;

    /**
     * 蓝牙操作状态（默认无操作状态）
     */
    public static int CURRENT_STATE = STATE_DEFAULT;
    /**
     * 默认蓝牙传输间隔(ms)
     */
    private int defaultConnectionBlank = 20;
    /**
     * 同步完成数据后是否发送删除数据指令
     */
    private boolean isDeleteDataAfterSync = false;

    /**
     * 当前发送指令
     */
    private byte[] currentComm;
    /**
     * 当前发送指令类型
     */
    private byte currentCommType;
    /**
     * 命令序列集合
     */
    private LinkedList<String> commList;

    private BleManager bleManager;
    private BleDataUtil bleDataUtil;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //TODO 待处理，用来处理数据的各种情况
            switch (msg.what) {
                case BleDataUtil.MSG_QUERY_BLE_STATE:
                    if ((byte)msg.obj == 2) {//设备正常
                        BleLog.e("ble 设备正常");
                        bleManager.sendBleBroadcast(BaseAction.ACTION_BLE_STATE_AVAILABLE);
                    } else {//设备异常
                        BleLog.e("ble 设备异常");
                        bleManager.sendBleBroadcast(BaseAction.ACTION_BLE_STATE_UNAVAILABLE);
                    }
                    break;
            }
        }
    };

    private static CommandUtil commandUtil;

    public CommandUtil() {
        commList = new LinkedList<>();
        if (bleManager == null) {
            bleManager = BleManager.getInstance();
        }
        if (bleDataUtil == null) {
            bleDataUtil = BleDataUtil.getInstance(handler);
        }
    }

    public static CommandUtil getInstance() {
        if (commandUtil == null) {
            synchronized (CommandUtil.class) {
                if (commandUtil == null) {
                    commandUtil = new CommandUtil();
                }
            }
        }
        return commandUtil;
    }

    /**
     * 发送下一个请求命令
     */
    public void sendNextCommand() {
        if (commList != null && commList.peek() != null) {
            switch (commList.peek()) {
                case CommandQueue.QUERY_STATE:
                    BleLog.e("请求命令：查询设备状态");
                    sendQueryStateComm();
                    break;
                case CommandQueue.SET_SEND_BLANK:
                    BleLog.e("请求命令：设置蓝牙传输间隔");
                    sendSetCommandBlankComm(defaultConnectionBlank);
                    break;
                case CommandQueue.SYNC_SPORTS_DATA:
                    BleLog.e("请求命令：请求运动数据");
                    sendSyncSportsDatas(true);
                    break;
                case CommandQueue.SYNC_SLEEP_DATA:
                    BleLog.e("请求命令：请求睡眠数据");
                    sendSyncSleepDatas(true);
                    break;
                case CommandQueue.SYNC_HEART_RATE_DATA:
                    BleLog.e("请求命令：请求心率数据");
                    sendSyncHeartRateDatas(true);
                    break;
            }
        } else {
            //TODO: 重置蓝牙发送状态
            CommandUtil.CURRENT_STATE = CommandUtil.STATE_DEFAULT;
            BleLog.e("commandList is empty stop ble send");
        }
    }

    /**
     * 添加命令
     */
    public void addCommand(String comm) {
        if (commList == null) {
            commList = new LinkedList<>();
        }
        if (commList.isEmpty()) {
            commList.add(comm);
            BleLog.e("命令队列为空，添加到队列，立即请求");
            sendNextCommand();
        } else {
            BleLog.e("添加到命令队列中，等待请求");
            commList.add(comm);
        }
    }

    /**
     * 添加命令集
     */
    public void addCommandList(LinkedList<String> comms) {
        if (commList == null) {
            commList = new LinkedList<>();
        }
        if (commList.isEmpty()) {
            commList.addAll(comms);
            BleLog.e("命令队列为空，添加到队列，立即请求");
            sendNextCommand();
        } else {
            BleLog.e("添加到命令队列中，等待请求");
            commList.addAll(comms);
        }
    }

    /**
     * 发送下一个请求,并且移除第一位数据
     */
    public void sendNextAndRemoveFirstCommand() {
        if (commList != null) commList.poll();
        sendNextCommand();
    }

    /**
     * 清除队列请求
     */
    public void clear() {
        if (commList != null) commList.clear();
    }

    /**
     * 接收ble设备发过来的数据
     */
    public void receiveBleData(BluetoothGattCharacteristic characteristic) {
        //TODO 待处理
        String uuid = characteristic.getUuid().toString().toLowerCase();
        switch (uuid) {
            case BleConstant.READ_UUID:
                BleLog.e("receive: " + HexUtil.encodeHexStr(characteristic.getValue()));
                //byte[] bytes = characteristic.getValue();
                bleDataUtil.handleReceiveData(characteristic.getValue());
                break;
        }
    }


    /**
     * 发送查询手环状态指令
     */
    public void sendQueryStateComm() {
        currentComm = CommandProtocol.getQueryBleStateProtocol();
        currentCommType = BleCommand.QUERY_STATE;
        CURRENT_STATE = STATE_QUERY_BLE_STATE;
        bleManager.writeCharacteristic(currentComm);
    }

    /**
     * 发送设置蓝牙传输间隔命令
     *
     * @param timeBlank 时间间隔(ms) 此值越小,蓝牙传输速率越快,同时错误率越高
     */
    public void sendSetCommandBlankComm(int timeBlank) {
        defaultConnectionBlank = timeBlank;
        timeBlank = (int) (timeBlank / 1.25);
        byte[] b = HexUtil.getBytes(timeBlank, false);
        sendSetCommandBlankComm(b[0], b[1]);
    }

    /**
     * 发送设置蓝牙传输间隔命令
     *
     * @param timeBlankbyte1
     * @param timeBlankbyte2
     */
    private void sendSetCommandBlankComm(byte timeBlankbyte1, byte timeBlankbyte2) {
        currentComm = CommandProtocol.getSetCommandBlankProtocol(timeBlankbyte1, timeBlankbyte2);
        currentCommType = BleCommand.CONNECT_BLANK;
        CURRENT_STATE = STATE_SET_CONNECT_BLANK;
        bleManager.writeCharacteristic(currentComm);
    }

    /**
     * 发送开始同步运动数据指令
     */
    public void sendSyncSportsDatas(boolean deleteDataAfterSync) {
        isDeleteDataAfterSync = deleteDataAfterSync;
        currentComm = CommandProtocol.getSyncSportsDataProtocol();
        currentCommType = BleCommand.SPORTS;
        CURRENT_STATE = STATE_REQUEST_SYNC_SPORTS;
        bleManager.writeCharacteristic(currentComm);
    }

    /**
     * 发送开始同步睡眠数据指令
     *
     * @param deleteDataAfterSync
     */
    public void sendSyncSleepDatas(boolean deleteDataAfterSync) {
        isDeleteDataAfterSync = deleteDataAfterSync;
        currentComm = CommandProtocol.getSyncSleepDataProtocol();
        currentCommType = BleCommand.SLEEP;
        CURRENT_STATE = STATE_REQUEST_SYNC_SLEEP;
        bleManager.writeCharacteristic(currentComm);
    }

    /**
     * 发送开始同步心率数据指令
     *
     * @param deleteDataAfterSync
     */
    public void sendSyncHeartRateDatas(boolean deleteDataAfterSync) {
        isDeleteDataAfterSync = deleteDataAfterSync;
        currentComm = CommandProtocol.getSyncHeartRateDataProtocol();
        currentCommType = BleCommand.HEART_RATE;
        CURRENT_STATE = STATE_REQUEST_SYNC_HEART_RATE;
        bleManager.writeCharacteristic(currentComm);
    }

}
