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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.ble.callback.BleCallBack;
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
    public static final int DEFAULT_OPERATE_TIME = 500;//最大操作时间（读、写、订阅 ）

    private static final int MSG_WRITE_CHA = 1;
    private static final int MSG_WRITE_DES = 2;
    private static final int MSG_READ_CHA = 3;
    private static final int MSG_READ_DES = 4;
    private static final int MSG_READ_RSSI = 5;
    private static final int MSG_CONNECT_TIMEOUT = 6;

    private static final int RECONNECT_MAX_TIME = 3;//重连最大次数

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic readCharacteristic;//读 (需要添加订阅)
    private BluetoothGattCharacteristic writeCharacteristic;//写
    private BluetoothGattCharacteristic heartCharacteristic;//写(心跳包)
    private BluetoothGattDescriptor descriptor;
    private State state = State.DISCONNECT;
    private int scanTimeout = DEFAULT_SCAN_TIME;
    private int connectTimeout = DEFAULT_CONN_TIME;
    private int operateTimeout = DEFAULT_OPERATE_TIME;
    private int connectTimes;//重连次数
    private BleDevice bleDevice;

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
                case MSG_CONNECT_TIMEOUT://连接超时
                    if (state != State.CONNECT_SUCCESS) {
                        state = State.CONNECT_TIMEOUT;
                        BleLog.e("ble 连接超时  ");
                        //发送广播通知连接超时
                        sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_TIME_OUT);
                        //重新连接
                        reConnect();
                    }
                    break;
                case MSG_WRITE_CHA://写入超时
                case MSG_WRITE_DES:
                    if (state != State.WRITE_SUCCESS) {
                        state = State.WRITE_TIMEOUT;
                        sendBleBroadcast(BaseAction.ACTION_BLE_WRITE_TIME_OUT);
                    }
                    break;
                case MSG_READ_CHA://读取超时
                case MSG_READ_DES:
                    if(state != State.READ_SUCCESS){
                        state=State.READ_TIMEOUT;
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
                        state = State.CONNECT_PROCESS;
                        //发广播通知正在连接
                        sendBleBroadcast(BaseAction.ACTION_DEVICE_CONNECTING);
                        break;
                    case BluetoothGatt.STATE_CONNECTED:
                        if (bluetoothGatt == null) {
                            bluetoothGatt = gatt;
                        }
                        if (bluetoothGatt != null) {
                            //to search the service
                            bluetoothGatt.discoverServices();
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
                if (gatt != null) {
                    bluetoothGatt = gatt;
                }
                state = State.CONNECT_SUCCESS;
                connectTimes = 0;
                // 发广播通知连接成功
                sendBleBroadcast(BaseAction.ACTION_DEVICE_CONNECTED);
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
            if(status == BluetoothGatt.GATT_SUCCESS){
                state=State.READ_SUCCESS;
                sendBleBroadcast(BaseAction.ACTION_BLE_READ_SUCCESS);
            }else{
                state=State.READ_FAILURE;
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
            if(status == BluetoothGatt.GATT_SUCCESS){
                state=State.READ_SUCCESS;
                sendBleBroadcast(BaseAction.ACTION_BLE_READ_SUCCESS);
            }else{
                state=State.READ_FAILURE;
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
                state = State.WRITE_SUCCESS;
                sendBleBroadcast(BaseAction.ACTION_BLE_WRITE_SUCCESS);
            } else {
                state = State.WRITE_FAILURE;
                sendBleBroadcast(BaseAction.ACTION_BLE_WRITE_FAILURE);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
             super.onCharacteristicChanged(gatt, characteristic);
            BleLog.i("onCharacteristicChanged data:" + HexUtil.encodeHexStr(characteristic.getValue()));
            //TODO 待处理。。。。。。。。
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
//                    if (receiveBleCallback != null) {
//                        receiveBleCallback.onSuccess(characteristic, 0);
//                    }
                }
            });

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
        return bluetoothDevice.connectGatt(this.context, autoConnect, gattCallback);
    }

    //根据BluetoothDevice进行连接
    public synchronized void connect(BleDevice bleDevice, boolean autoConnect) {
        if (bleDevice == null) {
            throw new IllegalArgumentException("this BleDevice is Null!");
        }
        this.bleDevice = bleDevice;
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
                        connect(bleDevice, autoConnect);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //  发送广播连接超时
                        sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_TIME_OUT);
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
                        connect(bluetoothLeDevice, autoConnect);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //  发送广播连接超时
                        sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_TIME_OUT);


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
                        connect(bluetoothLeDevice, autoConnect);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //  发送广播连接超时
                        sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_TIME_OUT);


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
                        connect(bluetoothLeDevice, autoConnect);
                    }
                });
            }

            @Override
            public void scanTimeout() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        //  发送广播连接超时
                        sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_TIME_OUT);
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
            }else{
                BleLog.e("not discover the named service ");
            }
            if(readCharacteristic!=null&&writeCharacteristic!=null&&heartCharacteristic!=null){
                //找到指定Characteristic,发通知订阅
                BleLog.e("discover the named Characteristic ");
                sendBleBroadcast(BaseAction.ACTION_BLE_SERVICE_DISCOVER_SUCCESS);
            }else{
               //找不到指定Characteristic ，发通知重连
                BleLog.e(" not discover the named Characteristic ");
                reConnect();
                sendBleBroadcast(BaseAction.ACTION_BLE_SERVICE_DISCOVER_FAILURE);
            }

        }else{
           BleLog.e("serviceUUID is "+serviceUUID +" or  bluetoothGatt is "+bluetoothGatt);
            //TODO 重新连接
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
            BleLog.e(" notify Characteristic  不可“读”!  or characteristic = "+characteristic);
            return false;
        }
    }

    //取消订阅
    public boolean disableCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                     final BleCallBack<BluetoothGattCharacteristic> bleCallback) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            return setCharacteristicNotification(bluetoothGatt, characteristic, false);
        } else {
            BleLog.e(" notify Characteristic  不可“读”  or characteristic = "+characteristic);
            return false;
        }
    }


    public boolean setCharacteristicNotification(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean enable) {
        if (gatt != null && characteristic != null) {
            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                BleLog.e("check characteristic property :false");
                return false;
            }
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
                    gatt.writeDescriptor(descriptor);
                }
            }
            BleLog.i("Characteristic set notification is " + success);
            return success;
        }else{
            BleLog.e("BluetoothGatt is null or notify characteristic is null");
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
        }
        return false;
    }

    public boolean enableCharacteristicIndication(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            return setCharacteristicIndication(bluetoothGatt, characteristic, true);
        } else {
            BleLog.e(" indicate Characteristic  不可“读”! or characteristic = "+characteristic);
            return false;
        }
    }

    public boolean disableCharacteristicIndication(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            return setCharacteristicIndication(bluetoothGatt, characteristic, false);
        } else {
            BleLog.e(" indicate Characteristic  不可“读” or characteristic = "+characteristic);
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

            boolean success = gatt.setCharacteristicNotification(characteristic, isIndication);
            if (success) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BleConstant.CLIENT_CHARACTERISTIC_CONFIG));
                if (descriptor != null) {
                    if (isIndication) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    } else {
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    gatt.writeDescriptor(descriptor);
                }
            }
            BleLog.i("Characteristic set notification is Success!");
            return success;
        }else{
            BleLog.e("BluetoothGatt is null or indicate characteristic is null");
            sendBleBroadcast(BaseAction.ACTION_BLE_ERROR);
        }
        return false;
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
        }else {
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
        if(checkBluetoothGatt()){
            state = State.WRITE_PROCESS;
            BleLog.e(characteristic.getUuid() + " characteristic write bytes: "
                    + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
            listenAndTimer(MSG_WRITE_CHA);
            characteristic.setValue(data);
            boolean  isSuccess = bluetoothGatt.writeCharacteristic(characteristic);
            if(!isSuccess){//写入失败
                state = State.WRITE_FAILURE;
            }
            BleLog.e("写入 characteristic ：" + isSuccess);
            return handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_WRITE_FAILURE);
        }else{
            state = State.WRITE_FAILURE;
            //TODO 重新连接
            reConnect();
            return false;
        }

//        boolean isSuccess;
//        if(checkBluetoothGatt()){
//             isSuccess = bluetoothGatt.writeCharacteristic(characteristic);
//            if (!isSuccess) {//写入失败
//                state = State.WRITE_FAILURE;
//            }
//        }else{
//            state = State.WRITE_FAILURE;
//            return false;
//        }
//
//        BleLog.e("写入 characteristic ：" + isSuccess);
//        return handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_WRITE_FAILURE);
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
        state=State.WRITE_PROCESS;
        BleLog.e(descriptor.getUuid() + " descriptor write bytes: "
                + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
        listenAndTimer(MSG_WRITE_DES);
        descriptor.setValue(data);
        boolean isSuccess;
        if(checkBluetoothGatt()){
             isSuccess = bluetoothGatt.writeDescriptor(descriptor);
            if (!isSuccess) {//写入失败
                state = State.WRITE_FAILURE;
            }
        }else{
            state = State.WRITE_FAILURE;
            return false;
        }
        return handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_WRITE_FAILURE);
    }
       /*========================================read===================================*/

    public boolean readCharacteristic() {
        return readCharacteristic(readCharacteristic);
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && (characteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            state=State.READ_PROCESS;
            setCharacteristicNotification(getBluetoothGatt(), characteristic, false);
            listenAndTimer(MSG_READ_CHA);
            boolean isSuccess;
            if(checkBluetoothGatt()){
                isSuccess = getBluetoothGatt().readCharacteristic(characteristic);
                if (!isSuccess) {//读取失败
                    state=State.READ_FAILURE;
                }
            }else{
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
        if(descriptor==null){
            BleLog.e("descriptor is null");
            return  false;
        }
        state=State.READ_PROCESS;
        listenAndTimer( MSG_READ_DES);
        boolean isSuccess;
        if(checkBluetoothGatt()){
            isSuccess=getBluetoothGatt().readDescriptor(descriptor);
            if(!isSuccess){
                state=State.READ_FAILURE;
            }
        }else{
            state = State.READ_FAILURE;
            return false;
        }

        return handleAfterInitialed(isSuccess, BaseAction.ACTION_BLE_READ_FAILURE);
    }

    /*=======================================other===========================================*/
    //重新连接
    public synchronized  void reConnect(){
        if(BleUtil.isBleEnable(context)&&connectTimes<DEFAULT_CONN_TIME){
            clear();
            connectTimes++;
            BleLog.e("连接失败，重新连接"+connectTimes+"次 ");
            connect(bleDevice,false);
        }else{
            clear();
            sendBleBroadcast(BaseAction.ACTION_BLE_CONNECT_FAILURE);
            BleLog.e("连接次数超过"+DEFAULT_CONN_TIME+"次 ，连接失败");
        }
    }
    //断开连接
    public synchronized void disconnect() {
        state=State.DISCONNECT;
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public synchronized void close() {
        state=State.DISCONNECT;
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

        if (!initiated) {//写入失败，取消超时写入消息
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            //发广播：写入失败
            sendBleBroadcast(action);
        }
        return initiated;
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

    //蓝牙不稳定，每次用到BluetoothGatt就要判断
    private boolean checkBluetoothGatt(){
        if(bluetoothGatt!=null){
            return true;
        }else{
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
        context.sendBroadcast(new Intent(action), BaseAction.RECEIVE_BROADCAST_PERMISSION);
    }

}
