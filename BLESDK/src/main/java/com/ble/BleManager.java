package com.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ble.callback.scan.BleScanCallback;
import com.ble.callback.scan.MacBleScanCallback;
import com.ble.callback.scan.NameBleScanCallback;
import com.ble.callback.scan.NewBleScanCallBack;
import com.ble.common.BaseAction;
import com.ble.common.BleConstant;
import com.ble.common.State;
import com.ble.model.BleDevice;
import com.ble.utils.BleLog;
import com.ble.utils.BleUtil;
import com.ble.utils.CommandUtil;
import com.ble.utils.HexUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 蓝牙操作管理类(扫描、连接、断开、读写等)
 * Created by yuandong on /3/7 0007.
 */

public class BleManager {
    public static final int DEFAULT_SCAN_TIME = 20000;//最大扫描时间
    public static final int DEFAULT_CONN_TIME = 10000;//最大连接时间
    public static final int DEFAULT_OPERATE_TIME = 1000;//最大操作时间（读、写 ）
    public static final int RECONNECT_MAX_TIME = 3;//重连最大次数

    //message.what
    private static final int MSG_WRITE_CHA = 1;
    private static final int MSG_WRITE_DES = 2;
    private static final int MSG_READ_CHA = 3;
    private static final int MSG_READ_DES = 4;
    private static final int MSG_SCAN_TIMEOUT = 5;
    private static final int MSG_CONNECT_TIMEOUT = 6;


    public Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic readCharacteristic;//读 (需要添加订阅)
    private BluetoothGattCharacteristic writeCharacteristic;//写
    private BluetoothGattCharacteristic heartCharacteristic;//写(心跳包
    private BluetoothGattDescriptor descriptor;
    private State state = State.DISCONNECT;
    private int scanTimeout = DEFAULT_SCAN_TIME;
    private int connectTimeout = DEFAULT_CONN_TIME;//连接超时时间
    private int operateTimeout = DEFAULT_OPERATE_TIME;//操作超时时间（比如读、写）
    private int reConnectTimes;//重连次数
    private BleDevice mBleDevice;
    //TODO 测试
    private long lastTime;
    /**
     * 用来记录
     * 1.从写入命令到接收到ble设备发过来的数据的time1
     * 2.从写入命令到写入命令回调的time2
     * 经验证 基本上 time1 < time2
     */
    private long writeTime;

    private static BleManager bleManager;

    private BleManager() {
    }

    public static BleManager getInstance() {
        if (bleManager == null) {
            synchronized (BleManager.class) {
                if (bleManager == null) {
                    bleManager = new BleManager();
                }
            }
        }
        return bleManager;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCAN_TIMEOUT://扫描超时
                    //TODO 待处理：暂时以信息的处理，方便以后可以做其他的操作（比如：重连。。。）
                    //发送广播 扫描超时
                    state = State.SCAN_TIMEOUT;
                    sendBleBroadcast(BaseAction.ACTION_DEVICE_SCAN_TIMEOUT);
                    break;
                case MSG_CONNECT_TIMEOUT://连接超时
                    if (state == State.CONNECT_PROCESS) {
                        state = State.CONNECT_TIMEOUT;
                        BleLog.e("ble connect timeout");
                        //发送广播 连接超时
                        sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_TIME_OUT);
                        clear();
                        //重新连接
                        reConnect();
                    }
                    break;
                case MSG_WRITE_CHA://写入超时
                case MSG_WRITE_DES:
                    if (state == State.WRITE_PROCESS) {
                        state = State.WRITE_TIMEOUT;
                        //发送广播 写入超时
                        BleLog.e("write time out ");
                        sendBleBroadcast(BaseAction.ACTION_BLE_WRITE_TIME_OUT);
                    }
//                    if (state == State.SUBSCRIBE_PROCESS) {
//                        //发送广播 订阅超时
//                        BleLog.e("subscribe time out ");
//                        state = State.SUBSCRIBE_TIMEOUT;
//                        sendBleBroadcast(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_TIMEOUT);
//                    }
                    break;
                case MSG_READ_CHA://读取超时
                case MSG_READ_DES:
                    if (state != State.READ_PROCESS) {
                        state = State.READ_TIMEOUT;
                        sendBleBroadcast(BaseAction.ACTION_BLE_READ_TIME_OUT);
                    }
                    break;
            }
            msg.obj = null;
        }
    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, int newState) {
            BleLog.e("onConnectionStateChange :  status: " + status + " ,newState: " + newState +
                    "  ,thread: " + Thread.currentThread().toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTING:
                        //state = State.CONNECT_PROCESS;
                        //发广播通知正在连接
                        //sendBleBroadcast(BaseAction.ACTION_DEVICE_CONNECTING);
                        break;
                    case BluetoothGatt.STATE_CONNECTED:
                        if (gatt != null) {
                            BleLog.e("连接用时：" + (System.currentTimeMillis() - lastTime));
                            bluetoothGatt = gatt;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    BleLog.e("to discover  the service....");
                                    gatt.discoverServices();
                                }
                            }, 500);
                        }
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        state = State.DISCONNECT;
                        if (handler != null) {
                            handler.removeMessages(MSG_CONNECT_TIMEOUT);
                        }
                        //发广播通知连接断开
                        sendBleBroadcast(BaseAction.ACTION_DEVICE_DISCONNECTED);
                        //TODO 在这里如果有需要可以进行重连操作（暂时不做处理）
//                        runOnMainThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                close();
//                            }
//                        });
                        break;
                }
            } else {
                if (handler != null) {
                    handler.removeMessages(MSG_CONNECT_TIMEOUT);
                }
                state = State.CONNECT_FAILURE;
                //TODO 连接出现其他的异常，比如133 、8、等其他异常 ,进行重连操作（可以限制重连次数）
                //发广播通知出现异常，连接失败
                //sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_FAILURE);
                BleLog.e("ble 连接异常");
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        reConnect();
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            BleLog.e("onServicesDiscovered  status: " + status);
            if (handler != null) {
                handler.removeMessages(MSG_CONNECT_TIMEOUT);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BleLog.e("发现服务用时：" + (System.currentTimeMillis() - lastTime));
                if (gatt != null) {
                    bluetoothGatt = gatt;
                }
                //TODO 在这里进行判断，达到超时时间，及时发现服务成功也不发送广播
                if (state == State.CONNECT_PROCESS) {
                    state = State.CONNECT_SUCCESS;
                    //重新设置重连次数
                    reConnectTimes = 0;
                    // 发广播通知连接成功
                    sendBleBroadcast(BaseAction.ACTION_DEVICE_CONNECTED);
                }

            } else {
                //TODO 设备连接成功，但是status会出现“129”等情况,在这里可以进行断开重连操作
                state = State.CONNECT_FAILURE;
                // 发广播通知连接失败
                // sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_FAILURE);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
//                        disconnect();
//                        close();
                        reConnect();
                    }
                });
            }
        }

        //读取characteristic回调
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleLog.i("onCharacteristicRead  status: " + status + ", data:" + HexUtil.encodeHexStr(characteristic.getValue()));
            if (handler != null) {
                handler.removeMessages(MSG_READ_CHA);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                state = State.READ_SUCCESS;
                sendBleBroadcast(BaseAction.ACTION_BLE_READ_SUCCESS);
            } else {
                state = State.READ_FAILURE;
                sendBleBroadcast(BaseAction.ACTION_BLE_READ_FAILURE);
            }
        }

        //写入characteristic回调
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            BleLog.i("onCharacteristicWrite  status: " + status + ", data:" + HexUtil.encodeHexStr(characteristic.getValue()));

            if (handler != null) {
                handler.removeMessages(MSG_WRITE_CHA);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                state = State.WRITE_SUCCESS;
                sendBleBroadcast(BaseAction.ACTION_BLE_WRITE_SUCCESS);
            } else {
                state = State.WRITE_FAILURE;
                sendBleBroadcast(BaseAction.ACTION_BLE_WRITE_FAILURE);
            }

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, final int status) {
            BleLog.i("onDescriptorRead  status: " + status + ", data:" + HexUtil.encodeHexStr(descriptor.getValue()));
            if (handler != null) {
                handler.removeMessages(MSG_READ_DES);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                state = State.READ_SUCCESS;
                sendBleBroadcast(BaseAction.ACTION_BLE_READ_SUCCESS);
            } else {
                state = State.READ_FAILURE;
                sendBleBroadcast(BaseAction.ACTION_BLE_READ_FAILURE);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            BleLog.i("onDescriptorWrite  status: " + status + ", data:" + HexUtil.encodeHexStr(descriptor.getValue()));
            if (handler != null) {
                handler.removeMessages(MSG_WRITE_DES);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (state == State.SUBSCRIBE_PROCESS) {//订阅
                    BleLog.e("SUBSCRIBE SUCCESS");
                    state = State.SUBSCRIBE_SUCCESS;
                    sendBleBroadcast(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_SUCCESS);
                } else if (state == State.WRITE_PROCESS) {
                    BleLog.e("WRITE SUCCESS");
                    state = State.WRITE_SUCCESS;
                    sendBleBroadcast(BaseAction.ACTION_BLE_WRITE_SUCCESS);
                }
            } else {
                if (state == State.SUBSCRIBE_PROCESS) {//订阅
                    state = State.SUBSCRIBE_FAILURE;
                    sendBleBroadcast(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_SUCCESS);
                } else {
                    state = State.WRITE_FAILURE;
                    sendBleBroadcast(BaseAction.ACTION_BLE_WRITE_FAILURE);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //BleLog.i("onCharacteristicChanged data:" + HexUtil.encodeHexStr(characteristic.getValue()));
//            //TODO 测试=====================================
//            if(CommandUtil.CurrentTimes.size()>0) {
//                BleLog.e("从写入命令到接受到数据耗时：" + (System.currentTimeMillis() - CommandUtil.CurrentTimes.peek()));
//                CommandUtil.CurrentTimes.poll();
//            }
//            //TODO 测试=====================================
            CommandUtil.getInstance().receiveBleData(characteristic);
        }

    };

    //初始化蓝牙
    public void init(Context context) {
        if (this.context == null) {
            this.context = context.getApplicationContext();
            bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    /*==================Android API(18---20)Scan========================*/
    @Deprecated
    public void startLeScan(LeScanCallback leScanCallback) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.startLeScan(leScanCallback);
            state = State.SCAN_PROCESS;
        }
    }

    @Deprecated
    public void stopLeScan(LeScanCallback leScanCallback) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    //ble设备开始扫描
    @Deprecated
    public void startScan(BleScanCallback scanCallback) {
        if (scanCallback == null) {
            throw new IllegalArgumentException("this BleScanCallback is Null!");
        }
        scanCallback.setBleManager(this).setScan(true).setScanTimeout(scanTimeout).scan();
    }

    //ble设备停止扫描
    @Deprecated
    public void stopScan(BleScanCallback scanCallback) {
        if (scanCallback == null) {
            throw new IllegalArgumentException("this BleScanCallback is Null!");
        }
        scanCallback.setBleManager(this).setScan(false).removeHandlerMsg().scan();
    }

    /*==================Android API(21----以上)Scan========================*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startLeScan(ScanCallback leScanCallback) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.getBluetoothLeScanner().startScan(leScanCallback);
            state = State.SCAN_PROCESS;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startLeScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback leScanCallback) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, leScanCallback);
            state = State.SCAN_PROCESS;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopLeScan(ScanCallback leScanCallback) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
        }
    }

    //ble 设备扫描
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScan(NewBleScanCallBack scanCallback) {
        if (scanCallback == null) {
            throw new IllegalArgumentException("this PeriodScanCallback is Null!");
        }
        scanCallback.setViseBluetooth(this).setScan(true).setScanTimeout(scanTimeout).scan();
    }

    //ble 根据筛选条件进行扫描
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScan(List<ScanFilter> filters, ScanSettings settings, NewBleScanCallBack scanCallback) {
        if (scanCallback == null) {
            throw new IllegalArgumentException("this PeriodScanCallback is Null!");
        }
        scanCallback.setViseBluetooth(this).setScan(true).setScanTimeout(scanTimeout).setFilters(filters).setSettings(settings).scan();
    }

    //ble 停止扫描
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopScan(NewBleScanCallBack scanCallback) {
        if (scanCallback == null) {
            throw new IllegalArgumentException("this PeriodScanCallback is Null!");
        }
        scanCallback.setViseBluetooth(this).setScan(false).removeHandlerMsg().scan();
    }

    /*======================================connect===========================================*/

    public synchronized BluetoothGatt connect(BluetoothDevice bluetoothDevice, boolean autoConnect) {
        if (bluetoothDevice == null) {
            throw new IllegalArgumentException("this BluetoothDevice or IConnectCallback is Null!");
        }
        if (handler != null) {
            Message msg = handler.obtainMessage(MSG_CONNECT_TIMEOUT);
            handler.sendMessageDelayed(msg, connectTimeout);
        }
        state = State.CONNECT_PROCESS;
        lastTime = System.currentTimeMillis();
        return bluetoothDevice.connectGatt(this.context, autoConnect, gattCallback);
    }

    //根据BluetoothDevice进行连接
    public synchronized void connect(BleDevice bleDevice, boolean autoConnect) {
        if (bleDevice == null) {
            throw new IllegalArgumentException("this BleDevice is Null!");
        }
        this.mBleDevice = bleDevice;
        connect(bleDevice.getDevice(), autoConnect);
    }

    //根据设备名进行连接
    public void connectByName(String name, final boolean autoConnect) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Illegal Name!");
        }
        startScan(new NameBleScanCallback(name) {
            @Override
            public void onDeviceFound(final BleDevice bleDevice) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mBleDevice = bleDevice;
                        connect(bleDevice, autoConnect);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //  发消息 扫描设备超时
                        BleLog.e("scan  timeout so that the ble scan timeout");
                        // sendBleBroadcast(BaseAction.ACTION_DEVICE_SCAN_TIMEOUT);
                        Message msg = handler.obtainMessage(MSG_SCAN_TIMEOUT);
                        handler.sendMessage(msg);
                    }
                });
            }
        });
    }

    //根据设备mac地址进行连接
    public void connectByMac(String mac, final boolean autoConnect) {
        if (TextUtils.isEmpty(mac) || mac.split(":").length != 6) {
            throw new IllegalArgumentException("Illegal MAC!");
        }
        startScan(new MacBleScanCallback(mac) {
            @Override
            public void onDeviceFound(final BleDevice bluetoothLeDevice) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mBleDevice = bluetoothLeDevice;
                        connect(bluetoothLeDevice, autoConnect);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //  发消息 扫描设备超时
                        BleLog.e("scan  timeout so that the ble scan timeout");
                        //sendBleBroadcast(BaseAction.ACTION_DEVICE_SCAN_TIMEOUT);
                        Message msg = handler.obtainMessage(MSG_SCAN_TIMEOUT);
                        handler.sendMessage(msg);
                    }
                });
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void connectByLName(String name, final boolean autoConnect) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Illegal Name!");
        }
        List<ScanFilter> bleScanFilters = new ArrayList<>();
        bleScanFilters.add(new ScanFilter.Builder().setDeviceName(name).build());
        startScan(bleScanFilters, null, new NewBleScanCallBack() {
            @Override
            public void onDeviceFound(final BleDevice bluetoothLeDevice) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mBleDevice = bluetoothLeDevice;
                        connect(bluetoothLeDevice, autoConnect);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //  发消息 扫描设备超时
                        BleLog.e("scan  timeout so that the ble scan timeout");
                        // sendBleBroadcast(BaseAction.ACTION_DEVICE_SCAN_TIMEOUT);
                        Message msg = handler.obtainMessage(MSG_SCAN_TIMEOUT);
                        handler.sendMessage(msg);
                    }
                });
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void connectByLMac(String mac, final boolean autoConnect) {
        if (TextUtils.isEmpty(mac) || mac.split(":").length != 6) {
            throw new IllegalArgumentException("Illegal MAC!");
        }
        List<ScanFilter> bleScanFilters = new ArrayList<>();
        bleScanFilters.add(new ScanFilter.Builder().setDeviceAddress(mac).build());
        startScan(bleScanFilters, null, new NewBleScanCallBack() {
            @Override
            public void onDeviceFound(final BleDevice bluetoothLeDevice) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mBleDevice = bluetoothLeDevice;
                        connect(bluetoothLeDevice, autoConnect);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //  发消息 扫描设备超时
                        BleLog.e("scan  timeout so that the ble scan timeout");
                        //sendBleBroadcast(BaseAction.ACTION_DEVICE_SCAN_TIMEOUT);
                        Message msg = handler.obtainMessage(MSG_SCAN_TIMEOUT);
                        handler.sendMessage(msg);
                    }
                });
            }
        });
    }

    //===============================UUID================================================
    //根据UUID获得当前的服务、读 、写、通知等属性
    public BleManager withUUIDString(String serviceUUID, String readCharacteristicUUID,
                                     String writeCharacteristicUUID,
                                     String heartCharacteristicUUID) {
        return withUUID(formUUID(serviceUUID),
                formUUID(readCharacteristicUUID),
                formUUID(writeCharacteristicUUID),
                formUUID(heartCharacteristicUUID));
    }

    private UUID formUUID(String uuid) {
        return TextUtils.isEmpty(uuid) == true ? null : UUID.fromString(uuid);
    }

    public BleManager withUUID(UUID serviceUUID, UUID readCharacteristicUUID,
                               UUID writeCharacteristicUUID,
                               UUID heartCharacteristicUUID) {
        if (serviceUUID != null && checkBluetoothGatt()) {
            service = bluetoothGatt.getService(serviceUUID);
            if (service != null) {
                BleLog.e(" discover the named service ");
                readCharacteristic = service.getCharacteristic(readCharacteristicUUID);
                writeCharacteristic = service.getCharacteristic(writeCharacteristicUUID);
                heartCharacteristic = service.getCharacteristic(heartCharacteristicUUID);
            } else {
                BleLog.e("not discover the named service ");
                sendBleBroadcast(BaseAction.ACTION_BLE_SERVICE_DISCOVER_FAILURE);
                //断开重连
                reConnect();
                return this;
            }
            if (readCharacteristic != null && writeCharacteristic != null && heartCharacteristic != null) {
                //找到指定Characteristic,发通知订阅
                BleLog.e("discover the named Characteristic ");
                sendBleBroadcast(BaseAction.ACTION_BLE_SERVICE_DISCOVER_SUCCESS);
            } else {
                //找不到指定Characteristic ，发通知重连
                BleLog.e(" not discover the named Characteristic ");
                reConnect();
                sendBleBroadcast(BaseAction.ACTION_BLE_SERVICE_DISCOVER_FAILURE);
            }
        } else {
            BleLog.e("serviceUUID is " + serviceUUID + " or  bluetoothGatt is " + bluetoothGatt);
            //重新连接
            reConnect();
        }
        return this;
    }

    /*==========================notify and indicate=====================================*/
    //添加订阅
    public boolean enableCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            return setCharacteristicNotification(bluetoothGatt, characteristic, true);
        } else {
            //Characteristic不可读
            BleLog.e(" notify Characteristic  不可“读”!  or characteristic = " + characteristic);
            return false;
        }
    }

    //取消订阅
    public boolean disableCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            return setCharacteristicNotification(bluetoothGatt, characteristic, false);
        } else {
            BleLog.e(" notify Characteristic  不可“读”  or characteristic = " + characteristic);
            return false;
        }
    }


    public boolean setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enable) {
        if (gatt != null && characteristic != null) {
            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                BleLog.e("check characteristic property :false");
                return false;
            }
            state = State.SUBSCRIBE_PROCESS;
            boolean success = gatt.setCharacteristicNotification(characteristic, enable);
            if (success) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BleConstant.CLIENT_CHARACTERISTIC_CONFIG));
                if (descriptor != null) {
                    if (enable) {
                        BleLog.e("characteristic  订阅成功...");
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    } else {
                        BleLog.e("characteristic  取消订阅成功...");
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    //TODO 订阅暂时不设置超时（有待修改）
                    //listenAndTimer(MSG_WRITE_DES);
                    boolean isSuccess = gatt.writeDescriptor(descriptor);
                    if (!isSuccess) {//写入不成功，
                        BleLog.i("Characteristic set notification is " + false);
                        state = State.SUBSCRIBE_FAILURE;
                        sendBleBroadcast(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE);
                        //handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE);
                        return false;
                    }
                }
            } else {
                //发送广播，订阅失败
                BleLog.i("Characteristic set notification is " + false);
                state = State.SUBSCRIBE_FAILURE;
                sendBleBroadcast(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE);
                return false;
            }
            return success;
        } else {
            BleLog.e("BluetoothGatt is null or notify characteristic is null");
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
            reConnect();
            return false;
        }
    }

    public boolean enableCharacteristicIndication(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            return setCharacteristicIndication(bluetoothGatt, characteristic, true);
        } else {
            BleLog.e(" indicate Characteristic  不可“读”! or characteristic = " + characteristic);
            return false;
        }
    }

    public boolean disableCharacteristicIndication(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            return setCharacteristicIndication(bluetoothGatt, characteristic, false);
        } else {
            BleLog.e(" indicate Characteristic  不可“读” or characteristic = " + characteristic);
            return false;
        }
    }

    public boolean setCharacteristicIndication(BluetoothGatt gatt,
                                               BluetoothGattCharacteristic characteristic,
                                               boolean isIndication) {
        if (gatt != null && characteristic != null) {
            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0) {
                BleLog.e("check characteristic property :false");
                return false;
            }
            state = State.SUBSCRIBE_PROCESS;
            boolean success = gatt.setCharacteristicNotification(characteristic, isIndication);
            if (success) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BleConstant.CLIENT_CHARACTERISTIC_CONFIG));
                if (descriptor != null) {
                    if (isIndication) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    } else {
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    //TODO 订阅不添加超时
                    //listenAndTimer(MSG_WRITE_DES);
                    boolean isSuccess = gatt.writeDescriptor(descriptor);
                    if (!isSuccess) {//写入不成功
                        BleLog.i("Characteristic set notification is " + false);
                        state = State.SUBSCRIBE_FAILURE;
                        sendBleBroadcast(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE);
                        // handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE);
                    }
                }
            } else {
                //发送广播，订阅失败
                BleLog.i("Characteristic set notification is " + false);
                state = State.SUBSCRIBE_FAILURE;
                sendBleBroadcast(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE);
                return false;
            }
            return success;
        } else {
            BleLog.e("BluetoothGatt is null or indicate characteristic is null");
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
            return false;
        }
    }

    public boolean enableDescriptorNotification(BluetoothGattDescriptor descriptor) {
        return setDescriptorNotification(bluetoothGatt, descriptor, true);
    }

    public boolean setDescriptorNotification(BluetoothGatt gatt,
                                             BluetoothGattDescriptor descriptor,
                                             boolean enable) {
        if (gatt != null && descriptor != null) {
            BleLog.i("Descriptor set notification value: " + enable);
            if (enable) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            return gatt.writeDescriptor(descriptor);
        } else {
            BleLog.e("BluetoothGatt is null or notify descriptor is null");
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
        }
        return false;
    }

    /*======================================write=========================================*/
    public boolean writeCharacteristic(byte[] data) {
        return writeCharacteristic(writeCharacteristic, data);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data) {
        if (characteristic == null) {
            BleLog.e(" write characteristic  is null");
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
            return false;
        }
        if (checkBluetoothGatt()) {
            writeTime = System.currentTimeMillis();
            state = State.WRITE_PROCESS;
            BleLog.e(characteristic.getUuid() + " characteristic write bytes: "
                    + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
            listenAndTimer(MSG_WRITE_CHA);
            characteristic.setValue(data);
            boolean isSuccess = bluetoothGatt.writeCharacteristic(characteristic);
            if (!isSuccess) {//写入失败
                state = State.WRITE_FAILURE;
            }
            BleLog.e("写入 characteristic ：" + isSuccess);
            return handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_WRITE_FAILURE);
        } else {
            //TODO 重新连接
            BleLog.e("发送命令时蓝牙异常，重新连接");
            reConnect();
            return false;
        }
    }

    public boolean writeDescriptor(byte[] data) {
        return writeDescriptor(getDescriptor(), data);
    }

    public boolean writeDescriptor(BluetoothGattDescriptor descriptor, byte[] data) {
        if (descriptor == null) {
            BleLog.e(" write descriptor  is null");
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
            return false;
        }

        if (checkBluetoothGatt()) {
            state = State.WRITE_PROCESS;
            BleLog.e(descriptor.getUuid() + " descriptor write bytes: "
                    + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
            listenAndTimer(MSG_WRITE_DES);
            descriptor.setValue(data);
            boolean isSuccess = bluetoothGatt.writeDescriptor(descriptor);
            if (!isSuccess) {//写入失败
                state = State.WRITE_FAILURE;
            }
            return handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_WRITE_FAILURE);
        } else {
            reConnect();
            return false;
        }
    }

       /*========================================read===================================*/

    public boolean readCharacteristic() {
        return readCharacteristic(readCharacteristic);
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            state = State.READ_PROCESS;
            setCharacteristicNotification(getBluetoothGatt(), characteristic, false);
            listenAndTimer(MSG_READ_CHA);
            boolean isSuccess;
            if (checkBluetoothGatt()) {
                isSuccess = getBluetoothGatt().readCharacteristic(characteristic);
                if (!isSuccess) {//读取失败
                    state = State.READ_FAILURE;
                }
            } else {
                state = State.READ_FAILURE;
                return false;
            }
            return handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_READ_FAILURE);
        } else {
            BleLog.e("Characteristic [is not] readable!  or Characteristic is null");
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
            return false;
        }
    }

    public boolean readDescriptor() {
        return readDescriptor(getDescriptor());
    }

    public boolean readDescriptor(BluetoothGattDescriptor descriptor) {
        if (descriptor == null) {
            BleLog.e("descriptor is null");
            return false;
        }
        state = State.READ_PROCESS;
        listenAndTimer(MSG_READ_DES);
        boolean isSuccess;
        if (checkBluetoothGatt()) {
            isSuccess = getBluetoothGatt().readDescriptor(descriptor);
            if (!isSuccess) {
                state = State.READ_FAILURE;
            }
        } else {
            state = State.READ_FAILURE;
            return false;
        }

        return handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_READ_FAILURE);
    }

    /*=======================================other===========================================*/
    //重新连接
    public synchronized void reConnect() {
        if (BleUtil.isBleEnable(context) && reConnectTimes < RECONNECT_MAX_TIME) {
            clear();
            reConnectTimes++;
            BleLog.e("连接失败，重新连接" + reConnectTimes + "次 ");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connect(mBleDevice, false);
                }
            }, 100);
        } else {
            reConnectTimes = 0;//重置重连次数
            clear();
            sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_FAILURE);
            BleLog.e("连接次数超过" + RECONNECT_MAX_TIME + "次 ，连接失败");
        }
    }

    //根据设备mac进行重新连接
    public synchronized void reConnect(String address, boolean auto) {
        if (BleUtil.isBleEnable(context) && reConnectTimes < RECONNECT_MAX_TIME) {
            clear();
            reConnectTimes++;
            BleLog.e("连接失败，重新连接" + reConnectTimes + "次 ");
            connectByName(address, auto);
        } else {
            clear();
            sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_FAILURE);
            BleLog.e("连接次数超过" + RECONNECT_MAX_TIME + "次 ，连接失败");
        }

    }

    //断开连接
    public synchronized void disconnect() {
        state = State.DISCONNECT;
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public synchronized void close() {
        state = State.DISCONNECT;
        if (bluetoothGatt != null) {
            refreshDeviceCache();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    public synchronized void clear() {
        disconnect();
        close();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public synchronized boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && bluetoothGatt != null) {
                final boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
                BleLog.i("Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            BleLog.e("An exception occured while refreshing device", e);
        }
        return false;
    }


    //监听操作超时
    private synchronized void listenAndTimer(int what) {
        if (handler != null) {
            Message msg = handler.obtainMessage(what);
            handler.sendMessageDelayed(msg, operateTimeout);
        }
    }

    //操作立即返回处理
    private boolean handleAfterInitialed(boolean initiated, String action) {

        if (!initiated) {//s读取或写入失败，取消超时写入消息
            cancelWriteOrReadTimeout();
            //发广播：写入失败
            sendBleBroadcast(action);
        }
        return initiated;
    }

    /**
     * 取消读写操作超时
     */
    public void cancelWriteOrReadTimeout() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }


    public BleManager setScanTimeout(int scanTimeout) {
        this.scanTimeout = scanTimeout;
        return this;
    }

    public BleManager setState(State state) {
        this.state = state;
        return this;
    }

    public State getState() {
        return state;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public BluetoothGattDescriptor getDescriptor() {
        return descriptor;
    }

    public BluetoothGattCharacteristic getReadCharacteristic() {
        return readCharacteristic;
    }

    public BluetoothGattCharacteristic getWriteCharacteristic() {
        return writeCharacteristic;
    }

    public BluetoothGattCharacteristic getHeartCharacteristic() {
        return heartCharacteristic;
    }

    //蓝牙不稳定，每次用到BluetoothGatt就要判断
    private boolean checkBluetoothGatt() {
        if (bluetoothGatt != null) {
            return true;
        } else {
            BleLog.e(" bluetoothGatt  is null");
            //发送广播，蓝牙异常
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
            return false;
        }
    }


    //判断是不是在主线程
    public boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    //切换到主线程
    public void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            if (handler != null) {
                handler.post(runnable);
            }
        }
    }

    //发送广播
    public void sendBleBroadcast(String action) {
        if(context!=null){
            context.sendBroadcast(new Intent(action), BaseAction.RECEIVE_BROADCAST_PERMISSION);
        }
    }

    //发送带有数据的广播
    public void sendBleBroadcast(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.sendBroadcast(intent, BaseAction.RECEIVE_BROADCAST_PERMISSION);
    }
}
