package com.yuandong.ble;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.BleManager;
import com.ble.common.BaseAction;
import com.ble.common.BleConstant;
import com.ble.model.BleDevice;
import com.ble.model.CommandQueue;
import com.ble.model.GattAttributeResolver;
import com.ble.model.HeartRate;
import com.ble.model.Sleep;
import com.ble.model.Sport;
import com.ble.utils.BleLog;
import com.ble.utils.CommandUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OperateActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = OperateActivity.class.getSimpleName();
    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    private TextView deviceAddress;
    private TextView connectState;
    private Button read, write, heart, subscribe, syncData;
    private TextView readValue, writeValue, heartValue, stateValue, blankValue;
    private BleDevice mDevice;
    private BleReceiver receiver;
    private BluetoothGatt gatt;
    private SimpleExpandableListAdapter simpleExpandableListAdapter;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();
    private BleManager bleManager;
    private ArrayList<Sport>sportList;
    private ArrayList<Sleep>sleepList;
    private ArrayList<HeartRate>heartRateList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate);
        mDevice = getIntent().getParcelableExtra(BleActivity.EXTRA_DEVICE);
        initView();
        registerBleBroadcast();
    }

    private void initView() {
        deviceAddress = (TextView) findViewById(R.id.device_address);
        connectState = (TextView) findViewById(R.id.connection_state);
        read = (Button) findViewById(R.id.red);
        write = (Button) findViewById(R.id.write);
        heart = (Button) findViewById(R.id.heart);
        subscribe = (Button) findViewById(R.id.subscribe);
        syncData = (Button) findViewById(R.id.syncData);
        readValue = (TextView) findViewById(R.id.redValue);
        writeValue = (TextView) findViewById(R.id.writeValue);
        heartValue = (TextView) findViewById(R.id.heartValue);
        stateValue = (TextView) findViewById(R.id.stateValue);
        blankValue = (TextView) findViewById(R.id.blankValue);
        read.setOnClickListener(this);
        write.setOnClickListener(this);
        heart.setOnClickListener(this);
        subscribe.setOnClickListener(this);
        syncData.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBleManager().connect(mDevice, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BleManager.getInstance().clear();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //ble 广播接收器
    private class BleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BaseAction.ACTION_BLE_ERROR:
                    Log.e(TAG, "ble 发生错误");
                    break;
                case BaseAction.ACTION_DEVICE_CONNECTING:
                    Log.e(TAG, "正在连接...");
                    break;
                case BaseAction.ACTION_DEVICE_CONNECTED:
                    Log.e(TAG, "连接成功");
                    deviceAddress.setText(mDevice.getAddress());
                    connectState.setText("true");
                    gatt = BleManager.getInstance().getBluetoothGatt();
                    if (gatt != null) {
                        simpleExpandableListAdapter = displayGattServices(gatt.getServices());
                    }
                    break;
                case BaseAction.ACTION_DEVICE_DISCONNECTED:
                    Log.e(TAG, "连接断开");
                    connectState.setText("false");
                    break;
                case BaseAction.ACTION_BLE_CONNECT_TIME_OUT:
                    Log.e(TAG, "连接超时");
                    connectState.setText("false");
                    break;
                case BaseAction.ACTION_BLE_CONNECT_FAILURE:
                    Log.e(TAG, "连接失败");
                    connectState.setText("false");
                    break;
                case BaseAction.ACTION_BLE_SERVICE_DISCOVER_SUCCESS:
                    Log.e(TAG, "发现指定服务成功");
                    //发现服务后 添加订阅
                    getBleManager().enableCharacteristicNotification(getBleManager().getReadCharacteristic());
                    break;
                case BaseAction.ACTION_BLE_SERVICE_DISCOVER_FAILURE:
                    Log.e(TAG, "发现指定服务失败");
                    break;
                case BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_SUCCESS:
                    Log.e(TAG, "订阅成功");
                    //添加订阅成功后
                    subscribe.setText("订阅成功");
                    LinkedList<String> commandList = new LinkedList<>();
                    commandList.add(CommandQueue.QUERY_STATE);
                    commandList.add(CommandQueue.SET_SEND_BLANK);
                    commandList.add(CommandQueue.SYNC_SPORTS_DATA);
                    commandList.add(CommandQueue.SYNC_SLEEP_DATA);
                    commandList.add(CommandQueue.SYNC_HEART_RATE_DATA);

                    // CommandUtil.getInstance().addCommand(CommandQueue.QUERY_STATE);
                    CommandUtil.getInstance().addCommandList(commandList);
                    break;
                case BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE:
                    Log.e(TAG, "订阅失败");
                    subscribe.setText("订阅失败");
                    break;
                case BaseAction.ACTION_BLE_STATE_AVAILABLE:
                  //  Log.e(TAG, "ble设备正常");
                    stateValue.setText("正常");
                    break;
                case BaseAction.ACTION_BLE_STATE_UNAVAILABLE:
                   // Log.e(TAG, "ble设备异常");
                    stateValue.setText("异常");
                    break;
                case BaseAction.ACTION_BLE_SET_CONNECT_BLANK_SUCCESS:
                  //  Log.e(TAG, "设置间隔时间成功");
                    blankValue.setText("成功");
                    break;
                case BaseAction.ACTION_BLE_SET_CONNECT_BLANK_FAILURE:
                    //Log.e(TAG, "设置间隔时间失败");
                    blankValue.setText("失败");
                    break;
                case BaseAction.ACTION_BLE_SEND_COMMAND_TIME_OUT:
                    Log.e(TAG, "发送命令超时");
                    break;
                case BaseAction.ACTION_BLE_SYNC_DATA_SUCCESS:
                    Log.e(TAG, "当前类型数据同步成功");
                    //获得运动数据
                    ArrayList<Sport> sports=intent.getParcelableArrayListExtra(CommandUtil.TYPE_SYNC_DATA_SPORT);
                    if(sports!=null){
                        sportList=sports;
                        BleLog.e("运动数据："+sportList.toString());
                    }
                    //获得睡眠数据
                    ArrayList<Sleep> sleeps=intent.getParcelableArrayListExtra(CommandUtil.TYPE_SYNC_DATA_SLEEP);
                    if(sleeps!=null){
                        sleepList=sleeps;
                        BleLog.e("睡眠数据："+sleepList.toString());
                    }
                    //获得心率数据
                    ArrayList<HeartRate> heartRates=intent.getParcelableArrayListExtra(CommandUtil.TYPE_SYNC_DATA_HEART_RATE);
                    if(heartRates!=null){
                        heartRateList=heartRates;
                        BleLog.e("心率数据："+heartRateList.toString());
                    }
                    break;
                case BaseAction.ACTION_BLE_SYNC_TOTAL_DATA_SUCCESS:
                    Log.e(TAG, "全部同步成功");
                    break;

            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.red:
                showGattServices(readValue);
                break;
            case R.id.write:
                showGattServices(writeValue);
                break;
            case R.id.heart:
                showGattServices(heartValue);
                break;
            case R.id.subscribe:
                subscribe();
                break;
            case R.id.syncData:
                break;
        }
    }

    //注册广播
    private void registerBleBroadcast() {
        receiver = new BleReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseAction.ACTION_BLE_ERROR);
        filter.addAction(BaseAction.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseAction.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(BaseAction.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseAction.ACTION_BLE_CONNECT_TIME_OUT);
        filter.addAction(BaseAction.ACTION_BLE_CONNECT_FAILURE);
        filter.addAction(BaseAction.ACTION_BLE_SERVICE_DISCOVER_SUCCESS);
        filter.addAction(BaseAction.ACTION_BLE_SERVICE_DISCOVER_FAILURE);
        filter.addAction(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_SUCCESS);
        filter.addAction(BaseAction.ACTION_BLE_CHARACTERISTIC_SUBSCRIBE_FAILURE);
        filter.addAction(BaseAction.ACTION_BLE_STATE_AVAILABLE);
        filter.addAction(BaseAction.ACTION_BLE_STATE_UNAVAILABLE);
        filter.addAction(BaseAction.ACTION_BLE_SEND_COMMAND_TIME_OUT);
        filter.addAction(BaseAction.ACTION_BLE_SET_CONNECT_BLANK_SUCCESS);
        filter.addAction(BaseAction.ACTION_BLE_SET_CONNECT_BLANK_FAILURE);
        filter.addAction(BaseAction.ACTION_BLE_SYNC_DATA_SUCCESS);
        filter.addAction(BaseAction.ACTION_BLE_SYNC_TOTAL_DATA_SUCCESS);
        registerReceiver(receiver, filter);
    }

    private SimpleExpandableListAdapter displayGattServices(final List<BluetoothGattService> gattServices) {
        if (gattServices == null) return null;
        String uuid;
        final String unknownServiceString = getResources().getString(R.string.unknown_service);
        final String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        final List<Map<String, String>> gattServiceData = new ArrayList<>();//服务service信息集合
        final List<List<Map<String, String>>> gattCharacteristicData = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();//所有特征集合

        // Loops through available GATT Services.
        for (final BluetoothGattService gattService : gattServices) {
            final Map<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            final List<Map<String, String>> gattCharacteristicGroupData = new ArrayList<>();
            final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            final List<BluetoothGattCharacteristic> charas = new ArrayList<>();

            // Loops through available Characteristics.
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                final Map<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, GattAttributeResolver.getAttributeName(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }

            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        final SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        return gattServiceAdapter;
    }


    private void showGattServices(final TextView textView) {
        if (simpleExpandableListAdapter == null) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(OperateActivity.this);
        View view = LayoutInflater.from(OperateActivity.this).inflate(R.layout.item_gatt_services, null);
        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.dialog_gatt_services_list);
        expandableListView.setAdapter(simpleExpandableListAdapter);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                dialog.dismiss();
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                textView.setText(characteristic.getUuid().toString());
                return true;
            }
        });
    }

    //添加服务和属性
    private void subscribe() {
        if (TextUtils.isEmpty(readValue.getText().toString().trim())) {
            Toast.makeText(this, "读服务不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(writeValue.getText().toString().trim())) {
            Toast.makeText(this, "写服务不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(heartValue.getText().toString().trim())) {
            Toast.makeText(this, "心跳包服务不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        getBleManager().withUUIDString(BleConstant.SERVICE_UUID,
                readValue.getText().toString().trim(),
                writeValue.getText().toString().trim(),
                heartValue.getText().toString().trim());
    }

    private void syncData() {

    }

    private BleManager getBleManager() {
        if (bleManager == null) {
            bleManager = BleManager.getInstance();
        }
        return bleManager;
    }
}
