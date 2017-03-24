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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.BleManager;
import com.ble.common.BaseAction;
import com.ble.model.BleDevice;
import com.ble.model.GattAttributeResolver;
import com.ble.utils.BleLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperateActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = OperateActivity.class.getSimpleName();
    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";
    private TextView deviceAddress;
    private TextView connectState;
    private Button read, write, heart, subscribe;
    private TextView readValue, writeValue, heartValue;
    private BleDevice mDevice;
    private BleReceiver receiver;
    private BluetoothGatt gatt;
    private SimpleExpandableListAdapter simpleExpandableListAdapter;
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate);
        mDevice = getIntent().getParcelableExtra(MainActivity.EXTRA_DEVICE);
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
        readValue = (TextView) findViewById(R.id.redValue);
        writeValue = (TextView) findViewById(R.id.writeValue);
        heartValue = (TextView) findViewById(R.id.heartValue);
        read.setOnClickListener(this);
        write.setOnClickListener(this);
        heart.setOnClickListener(this);
        subscribe.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BleManager.getInstance().connect(mDevice, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BleManager.getInstance().disconnect();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().clear();
        unregisterReceiver(receiver);
    }

    //ble 广播接收器
    private class BleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BaseAction.ACTION_DEVICE_CONNECTING:
                    Log.e(TAG, "正在连接...");
                    break;
                case BaseAction.ACTION_DEVICE_CONNECTED:
                    Log.e(TAG, "连接成功");
                    deviceAddress.setText(mDevice.getAddress());
                    connectState.setText("true");
                    gatt = BleManager.getInstance().getBluetoothGatt();
                    simpleExpandableListAdapter=displayGattServices(gatt.getServices());
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


            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.red:
                showGattServices(readValue);
                break;
            case  R.id.write:
                showGattServices(writeValue);
                break;
            case  R.id.heart:
                showGattServices(heartValue);
                break;
            case  R.id.subscribe:
                subscribe();
                break;
        }
    }

    //注册广播
    private void registerBleBroadcast() {
        receiver = new BleReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseAction.ACTION_DEVICE_CONNECTING);
        filter.addAction(BaseAction.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(BaseAction.ACTION_DEVICE_CONNECTED);
        filter.addAction(BaseAction.ACTION_BLE_CONNECT_TIME_OUT);
        filter.addAction(BaseAction.ACTION_BLE_CONNECT_FAILURE);
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

    //订阅
    private void subscribe(){
        if(TextUtils.isEmpty(readValue.getText().toString().trim())){
            Toast.makeText(this,"读服务不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(writeValue.getText().toString().trim())){
            Toast.makeText(this,"写服务不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(heartValue.getText().toString().trim())){
            Toast.makeText(this,"心跳包服务不能为空",Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
