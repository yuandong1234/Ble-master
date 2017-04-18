package com.ble.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ble.common.BleCommand;
import com.ble.model.BLeProtocol;
import com.ble.model.HeartRate;
import com.ble.model.Sleep;
import com.ble.model.Sport;

import java.util.ArrayList;

/**
 * 数据处理类（数据校验）
 * Created by yuandong on 2017/3/27.
 */

public class BleDataUtil {

    private final static String TAG = BleDataUtil.class.getSimpleName();
    /**
     * 发信息  手环工作状态
     */
    public static final int MSG_QUERY_BLE_STATE = 100000;
    /**
     * 发信息  数据检验错误
     */
    public static final int MSG_DATA_CHECK_ERROR = 100001;
    /**
     * 发信息  设置蓝牙时间间隔
     */
    public static final int MSG_SET_CONNECT_BLANK = 100002;
    /**
     * 发信息  手环回复数据总长度(运动、睡眠、心率)
     */
    public static final int MSG_RECEIVER_TOTAL_DATA_LENGTH = 100003;
    /**
     * 发信息  接收每一帧数据正确(运动、睡眠、心率)
     */
    public static final int MSG_RECEIVER_DATA_CORRECT = 100004;
    /**
     * 发信息  接收全部数据成功(运动、睡眠、心率)
     */
    public static final int MSG_RECEIVER_TOTAL_DATA_SUCCESS = 100005;
    /**
     * 发信息  接收全部数据成功(运动、睡眠、心率)
     */
    public static final int MSG_RECEIVER_TOTAL_DATA_FAILURE = 100006;


    /**
     * 手环回复数据总长度长度
     */
    private int dataLength = 0;
    /**
     * 记录数据条数，以便检验数据长度
     */
    private int dataCount = 0;
    /**
     * 数据传输帧编号（一直循环）
     * 09 19 29 39 49 59 69 79 89 99 A9
     */
    private byte tempFrameNo = 0x00;
    private ArrayList<Sport> sportList;
    private ArrayList<Sleep> sleepList;
    private ArrayList<HeartRate> heartRateList;

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
            case BleCommand.CONNECT_BLANK://蓝牙时间间隔
                //重置当前的校验数据的次数
                CommandUtil.CURRENT_DATA_CHECK_ERROR_TIMES = 0;
                sendBleMessage(MSG_SET_CONNECT_BLANK, bLeProtocol.getDatas()[0]);
                break;
            default://运动 、睡眠、心率
                switch (bLeProtocol.getFrameNo()) {
                    case BleCommand.RECEIVE_RESPONSE_DATA_LENGTH://手环回复数据总长度长度
                        //重置当前的校验数据的次数
                        CommandUtil.CURRENT_DATA_CHECK_ERROR_TIMES = 0;
                        //创建不同类型数据集合
                        createDataList(bLeProtocol.getCommand());
                        //获得当前的数据总长度
                        dataLength = HexUtil.bytesToInt(bLeProtocol.getDatas());

                        tempFrameNo = 0x09;
                        dataCount = 0;

                        sendBleMessage(MSG_RECEIVER_TOTAL_DATA_LENGTH, dataLength);
                        break;
                    case BleCommand.RECEIVE_DATA_END://数据接收结束 ,需要校验总数
                        //重置当前的校验数据的次数
                        CommandUtil.CURRENT_DATA_CHECK_ERROR_TIMES = 0;
                        if (checkDataTotalCount(bLeProtocol.getCommand())) {
                            //接受全部数据成功
                            switch (bLeProtocol.getCommand()) {
                                case BleCommand.SPORTS:
                                    sendBleMessage(MSG_RECEIVER_TOTAL_DATA_SUCCESS, sportList);
                                    break;
                                case BleCommand.SLEEP:
                                    sendBleMessage(MSG_RECEIVER_TOTAL_DATA_SUCCESS, sleepList);
                                    break;
                                case BleCommand.HEART_RATE:
                                    sendBleMessage(MSG_RECEIVER_TOTAL_DATA_SUCCESS, heartRateList);
                                    break;
                            }
                        } else {
                            //校验总数失败
                            clearDataList();
                            sendBleMessage(MSG_RECEIVER_TOTAL_DATA_FAILURE, null);
                        }

                        break;
                    default://接受每一帧的数据
                        if (checkFrameNumber(bLeProtocol.getFrameNo())) {
                            //如果帧编号正确 重置当前的校验数据的次数
                            CommandUtil.CURRENT_DATA_CHECK_ERROR_TIMES = 0;
                            //解析、添加数据
                            addDataList(bLeProtocol);
                            //发消息：接受数据校验正确
                            sendBleMessage(MSG_RECEIVER_DATA_CORRECT, null);
                        } else {
                            //帧编号错误
                            //发送消息：数据校验失败
                            handleIncorrectData();
                        }
                        break;
                }
                break;
        }
    }


    /**
     * 校验数据帧编号
     *
     * @param frameNo 当前帧序号
     * @return
     */
    private boolean checkFrameNumber(byte frameNo) {
        boolean result = false;

        if (frameNo - tempFrameNo == 0x10) {
            BleLog.i("data check true");
            tempFrameNo = frameNo;
            result = true;
        } else if (tempFrameNo == (byte) 0x79 && frameNo == (byte) 0x89) {
            BleLog.i("data check true");
            tempFrameNo = frameNo;
            result = true;
        } else if (tempFrameNo == (byte) 0xA9 && frameNo == (byte) 0x19) {
            BleLog.i("data check true go circle");
            tempFrameNo = frameNo;
            result = true;
        } else if (tempFrameNo == frameNo) {
            // 当传输过程中出现异常，比如超时，此时手环传过来的帧序号可能与上一帧相同
            // 而手环本身也会产生重复数据，所以手环发送过来的数据长度是不可靠的
            // 这种情况另外处理：认为手环数据无误，给手环发送D1确保帧序号回到正确的值上。
            BleLog.e("frame number repeat,go next");
            dataCount--;
            tempFrameNo = frameNo;
            result = true;
        }
        return result;
    }

    /**
     * 校验数据总数
     *
     * @param type 数据类型
     * @return
     */
    private boolean checkDataTotalCount(byte type) {
        boolean result = false;
        int count = 0;
        switch (type) {
            case BleCommand.SPORTS:
                count = dataCount * 14; //运动每帧14个字节数据
                BleLog.e("运动-----集合条数--> " + dataCount + ", 数据长度--> " + dataLength / 14);
                break;
            case BleCommand.SLEEP:
                count = dataCount * 10; //睡眠每帧10个字节数据
                BleLog.e("睡眠-----集合条数--> " + dataCount + ", 数据长度--> " + dataLength / 10);
                break;
            case BleCommand.HEART_RATE:
                count = dataCount * 11; //心率每帧11个字节数据
                BleLog.e("心率-----集合条数--> " + dataCount + ", 数据长度--> " + dataLength / 11);
                break;
        }
        if (count == dataLength) {
            result = true;
        }
        return result;
    }

    /**
     * 创建不同类型（运动、睡眠、心率）的数据集合
     */
    private void createDataList(byte type) {
        switch (type) {
            case BleCommand.SPORTS:
                sportList = new ArrayList<>();
                break;
            case BleCommand.SLEEP:
                sleepList = new ArrayList<>();
                break;
            case BleCommand.HEART_RATE:
                heartRateList = new ArrayList<>();
                break;
        }
    }

    /**
     * 解析当前的数据,添加不同类型的数据
     */
    private void addDataList(BLeProtocol bLeProtocol) {
        switch (bLeProtocol.getCommand()) {
            case BleCommand.SPORTS:
                //解析当前的数据
                Sport sport = DataAnalyzeHelper.analyzeSportsData(bLeProtocol.getDatas());
                synchronized (this) {
                    if (sportList.size() == 0 || !sportList.get(sportList.size() - 1).equals(sport)) {
                        sportList.add(sport);
                    } else {
                        BleLog.e("运动数据重复***");
                    }
                }
                dataCount++;
                break;
            case BleCommand.SLEEP:
                Sleep sleep = DataAnalyzeHelper.analyzeSleepData(bLeProtocol.getDatas());
                synchronized (this) {
                    if (sleepList.size() == 0 || !sleepList.get(sleepList.size() - 1).equals(sleep)) {
                        sleepList.add(sleep);
                    } else {
                        BleLog.e("睡眠数据重复***");
                    }
                }
                dataCount++;
                break;
            case BleCommand.HEART_RATE:
                HeartRate heartRate = DataAnalyzeHelper.analyzeHeartRateData(bLeProtocol.getDatas());
                synchronized (this) {
                    if (heartRateList.size() == 0 || !heartRateList.get(heartRateList.size() - 1).equals(heartRate)) {
                        heartRateList.add(heartRate);
                    } else {
                        BleLog.e("心率数据重复***");
                    }
                }
                dataCount++;
                break;
        }

    }

    /**
     * 清空集合
     */
    public void clearDataList() {
        sportList = null;
        sleepList = null;
        heartRateList = null;
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