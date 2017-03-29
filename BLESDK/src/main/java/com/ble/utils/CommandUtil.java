package com.ble.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
     * 当前蓝牙发送状态  发送命令超时
     */
    private final static int MSG_SEND_COMMAND_TIME_OUT = 9999;
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
     * 发送命令超时时间(ms)
     */
    private int sendCommandTimeOut = 600;
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
    /**
     * 响应超时最大发送指令次数
     */
    private static int MAX_RESEND_COMMAND_TIMES = 3;
    /**
     * 当前发送超时指令次数
     */
    public static int CURRENT_RESEND_COMMAND_TIMES = 0;

    /**
     * 数据检验错误的最大次数
     */
    private static int MAX_DATA_CHECK_ERROR_TIMES = 3;
    /**
     * 当前数据检验错误的次数
     */
    public static int CURRENT_DATA_CHECK_ERROR_TIMES = 0;
    /**
     * 记录是否超时
     */
    private boolean isTimeout = false;

    public static LinkedList<Long> CurrentTimes = new LinkedList<>();

    private BleManager bleManager;
    private BleDataUtil bleDataUtil;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //TODO 待处理，用来处理数据的各种情况
            switch (msg.what) {
                case BleDataUtil.MSG_QUERY_BLE_STATE://ble状态查询
                    //查询完成后，重置蓝牙发送状态
                    CURRENT_STATE = STATE_DEFAULT;
                    if ((byte) msg.obj == 2) {//设备正常
                        BleLog.e("ble 设备正常");
                        bleManager.sendBleBroadcast(BaseAction.ACTION_BLE_STATE_AVAILABLE);
                        sendNextAndRemoveFirstCommand();
                    } else {//设备异常
                        BleLog.e("ble 设备异常");
                        bleManager.sendBleBroadcast(BaseAction.ACTION_BLE_STATE_UNAVAILABLE);
                    }
                    break;
                case BleDataUtil.MSG_SET_CONNECT_BLANK://设置蓝牙时间间隔
                    if ((byte) msg.obj == 1) {//修改时间间隔成功
                        BleLog.e("ble 修改蓝牙时间间隔成功");
                        bleManager.sendBleBroadcast(BaseAction.ACTION_BLE_SET_CONNECT_BLANK_SUCCESS);
                        CURRENT_STATE = STATE_DEFAULT;
                        sendNextAndRemoveFirstCommand();
                    } else {
                        BleLog.e("ble 修改蓝牙时间间隔失败");
                        bleManager.sendBleBroadcast(BaseAction.ACTION_BLE_SET_CONNECT_BLANK_FAILURE);
                        //TODO 对于失败的情况暂时不予处理，可以进行重发
                    }
                    break;
                case BleDataUtil.MSG_DATA_CHECK_ERROR://ble数据校验错误、
                    //  重发操作
                    BleLog.e("========================数据校验错误==========================");
                    reSendCommand(BleDataUtil.MSG_DATA_CHECK_ERROR);
                    break;
                case MSG_SEND_COMMAND_TIME_OUT://ble发送命令超时
                    // 重发操作
                    BleLog.e("========================超时==========================");
                    BleLog.e("超时时间："+(System.currentTimeMillis()-CurrentTimes.peek()));
                    isTimeout = true;
                    CurrentTimes.clear();
                    reSendCommand(MSG_SEND_COMMAND_TIME_OUT);
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
        //取消超时处理
        if (handler != null) {
            handler.removeMessages(MSG_SEND_COMMAND_TIME_OUT);
        }
        String uuid = characteristic.getUuid().toString().toLowerCase();
        switch (uuid) {
            case BleConstant.READ_UUID:
                BleLog.e("receive: " + HexUtil.encodeHexStr(characteristic.getValue()));
                //处理接受的数据
                if (!isTimeout) {
                    //重置当前发送发送超时次数
                    CURRENT_RESEND_COMMAND_TIMES = 0;
                    bleDataUtil.handleReceiveData(characteristic.getValue());
                }
                break;
        }
    }

    /**
     * 写入请求命令
     */
    public void writeComm(byte[] currentComm) {
        isTimeout = false;
        //1.添加超时处理
        if (handler != null) {
            Message msg = handler.obtainMessage(MSG_SEND_COMMAND_TIME_OUT);
            CurrentTimes.add(System.currentTimeMillis());
            handler.sendMessageDelayed(msg, getBleTimeInterval());
        }
        //2.写入命令
        boolean isSuccess = bleManager.writeCharacteristic(currentComm);
        if (!isSuccess) {
            if (handler != null) {
                handler.removeMessages(MSG_SEND_COMMAND_TIME_OUT);
            }
        }
    }
    /**
     * 发送求重发指令
     *
     * @param reSendType 重发类型
     */
    public void reSendCommand(final int reSendType) {
        switch (reSendType) {
            case MSG_SEND_COMMAND_TIME_OUT://命令发送超时
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (CURRENT_RESEND_COMMAND_TIMES < MAX_RESEND_COMMAND_TIMES) {
                            CURRENT_RESEND_COMMAND_TIMES++;
                            BleLog.e("发送超时：当前第 >>" + CURRENT_RESEND_COMMAND_TIMES + "<< 次重新发送命令");
                            //如果当前请求命令重发的次数小于最大允许的重发次数
                            if (CURRENT_STATE == STATE_SEND_RECEIVE_DATA_CORRECT) {
                                //如果在传输数据过程中，发送数据错误C1
                                sendRequestLastDataComm();
                            } else {
                                //重新发送当前的命令
                                writeComm(currentComm);
                            }
                        } else {
                            //放弃本次数据传输
                            CURRENT_RESEND_COMMAND_TIMES = 0;
                            //重置蓝牙发送状态
                            CURRENT_STATE = STATE_DEFAULT;
                            clear();
                            //TODO 清空当前收到的数据（待做）

                            //TODO ..............................
                            //发送广播：发送命令超时
                            BleLog.e("达到最大重发命令次数 请求命令超时");
                            bleManager.sendBleBroadcast(BaseAction.ACTION_BLE_SEND_COMMAND_TIME_OUT);
                        }
                    }
                }, 500);
                break;
            case BleDataUtil.MSG_DATA_CHECK_ERROR://数据检验错误
                CURRENT_DATA_CHECK_ERROR_TIMES++;
                if (CURRENT_DATA_CHECK_ERROR_TIMES < MAX_DATA_CHECK_ERROR_TIMES) {
                    BleLog.e("当前帧数据：第 " + CURRENT_DATA_CHECK_ERROR_TIMES + " 次重新发送命令");
                    //如果当前请求命令重发的次数小于最大允许的重发次数
                    if (CURRENT_STATE == STATE_SEND_RECEIVE_DATA_CORRECT) {
                        //数据检验错误，发送数据错误C1
                        sendRequestLastDataComm();
                    } else {
                        //重新发送当前的命令
                        writeComm(currentComm);
                    }
                } else {
                    //放弃当前的命令请求，请求下一个命令
                    CURRENT_DATA_CHECK_ERROR_TIMES = 0;
                    //重置蓝牙发送状态
                    switch (currentCommType) {
                        case BleCommand.SPORTS:
                            //TODO 清空运动数据（待做）
                            break;
                        case BleCommand.SLEEP:
                            //TODO 空睡眠数据（待做）
                            break;
                        case BleCommand.HEART_RATE:
                            //TODO 清空心率数据（待做）
                            break;
                    }
                    CURRENT_STATE = STATE_DEFAULT;
                    sendNextAndRemoveFirstCommand();
                }
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
        writeComm(currentComm);
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
        writeComm(currentComm);
    }

    /**
     * 发送开始同步运动数据指令
     */
    public void sendSyncSportsDatas(boolean deleteDataAfterSync) {
        isDeleteDataAfterSync = deleteDataAfterSync;
        currentComm = CommandProtocol.getSyncSportsDataProtocol();
        currentCommType = BleCommand.SPORTS;
        CURRENT_STATE = STATE_REQUEST_SYNC_SPORTS;
        writeComm(currentComm);
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
        writeComm(currentComm);
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
        writeComm(currentComm);
    }

    /**
     * 运动、睡眠、心率
     * 发送允许同步数据指令
     */
    private void sendAllowSyncDatasComm() {

        switch (currentCommType) {
            case BleCommand.SPORTS:
                CURRENT_STATE = STATE_ALLOW_TO_SYNC_SPORTS;
                break;
            case BleCommand.SLEEP:
                CURRENT_STATE = STATE_ALLOW_TO_SYNC_SLEEP;
                break;
            case BleCommand.HEART_RATE:
                CURRENT_STATE = STATE_ALLOW_TO_SYNC_HEART_RATE;
                break;
        }
        currentComm = CommandProtocol.getAllowToSyncProtocol(currentCommType);
        writeComm(currentComm);
    }

    /**
     * 运动、睡眠、心率
     * 发送接收（每一帧）数据成功指令
     */
    private void sendReceiveDataCorrectComm() {
        currentComm = CommandProtocol.getReceiveDataCorrectProtocol(currentCommType);
        CURRENT_STATE = STATE_SEND_RECEIVE_DATA_CORRECT;
        writeComm(currentComm);
    }

    /**
     * 运动、睡眠、心率
     * 发送同步（全部）数据成功指令
     */
    private void sendSyncDataCorrectComm() {
        currentComm = CommandProtocol.getReceiveSyncDataCorrectProtocol(currentCommType);
        CURRENT_STATE = STATE_SEND_SYNC_DATA_SUCCESS;
        bleManager.writeCharacteristic(currentComm);
    }

    /**
     * 同步数据帧校验错误请求上一条数据
     */
    private  void sendRequestLastDataComm() {
        //发送C1 请求重发上一条数据
        byte[] currentComm = CommandProtocol.getRequestResendProtocol(currentCommType);
        writeComm(currentComm);
    }

    /**
     * 根据不同的请求命令设置不同的超时时间间隔
     */
    private int  getBleTimeInterval(){
        switch (currentCommType){
            case BleCommand.CONNECT_BLANK:
                sendCommandTimeOut=5000;
                break;
            default:
                sendCommandTimeOut=600;
                break;
        }
       return sendCommandTimeOut;
    }
}
