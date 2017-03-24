package com.yuandong.ble;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.BleManager;
import com.ble.callback.BleCallBack;
import com.ble.exception.BleException;
import com.ble.model.BleDevice;
import com.ble.model.GattAttributeResolver;
import com.ble.utils.BleLog;
import com.ble.utils.HexUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceControlActivity extends AppCompatActivity {

    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";

    private SimpleExpandableListAdapter simpleExpandableListAdapter;
    private TextView mConnectionState;
    private EditText mInput;
    private BleDevice mDevice;
    private BluetoothGattCharacteristic mCharacteristic;
    private StringBuilder mOutputInfo = new StringBuilder();
    private List<List<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

//    private ConnectCallBack connectCallback=new ConnectCallBack() {
//        @Override
//        public void onConnectSuccess(BluetoothGatt gatt, int status) {
//            BleLog.e("Connect Success!");
//            Toast.makeText(DeviceControlActivity.this, "Connect Success!", Toast.LENGTH_SHORT).show();
//            mConnectionState.setText("true");
//            simpleExpandableListAdapter=displayGattServices(gatt.getServices());
//        }
//
//        @Override
//        public void onConnectFailure(BleException exception) {
//            BleLog.e("Connect Failure: "+exception.toString());
//            Toast.makeText(DeviceControlActivity.this, "Connect Failure!", Toast.LENGTH_SHORT).show();
//            mConnectionState.setText("false");
//        }
//
//        @Override
//        public void onDisconnect() {
//            BleLog.e("Disconnect!");
//            Toast.makeText(DeviceControlActivity.this, "Disconnect!", Toast.LENGTH_SHORT).show();
//            mConnectionState.setText("false");
//        }
//    };
    private BleCallBack bleCallBack=new BleCallBack() {
        @Override
        public void onSuccess(Object o, int type) {
         BleLog.e("notify success...");
        }

        @Override
        public void onFailure(BleException exception) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        init();
    }

    private void init() {
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mInput = (EditText) findViewById(R.id.input);
        mDevice = getIntent().getParcelableExtra(MainActivity.EXTRA_DEVICE);
        if(mDevice != null){
            ((TextView) findViewById(R.id.device_address)).setText(mDevice.getAddress());
        }

        findViewById(R.id.select_write_characteristic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGattServices();
            }
        });
        findViewById(R.id.select_notify_characteristic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGattServices();
            }
        });
        findViewById(R.id.select_read_characteristic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGattServices();
            }
        });
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {//写入命令发送
            @Override
            public void onClick(View v) {
                if (mCharacteristic == null) {
                    Toast.makeText(DeviceControlActivity.this, "Please select enable write characteristic!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mInput.getText() == null || mInput.getText().toString() == null) {
                    Toast.makeText(DeviceControlActivity.this, "Please input command!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isHexData(mInput.getText().toString())){
                    Toast.makeText(DeviceControlActivity.this, "Please input hex data command!", Toast.LENGTH_SHORT).show();
                    return;
                }
//                BleManager.getInstance().writeCharacteristic(mCharacteristic, HexUtil.decodeHex(mInput.getText().toString().toCharArray()), new BleCallBack<BluetoothGattCharacteristic>() {
//                    @Override
//                    public void onSuccess(BluetoothGattCharacteristic characteristic, int type) {
//                        BleLog.i("Send onSuccess!");
//                    }
//
//                    @Override
//                    public void onFailure(BleException exception) {
//                        BleLog.i("Send onFail!");
//                    }
//                });
            }
        });
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


    private void showGattServices() {
        if (simpleExpandableListAdapter == null) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(DeviceControlActivity.this);
        View view = LayoutInflater.from(DeviceControlActivity.this).inflate(R.layout.item_gatt_services, null);
        ExpandableListView expandableListView = (ExpandableListView) view.findViewById(R.id.dialog_gatt_services_list);
        expandableListView.setAdapter(simpleExpandableListAdapter);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                dialog.dismiss();
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {//写入
                    BleLog.e("write");
                    mCharacteristic = characteristic;
                    ((EditText) findViewById(R.id.show_write_characteristic)).setText(characteristic.getUuid().toString());
                }
                if((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0){
                    BleLog.e("notify");
                    ((EditText) findViewById(R.id.show_notify_characteristic)).setText(characteristic.getUuid().toString());
                  //  BleManager.getInstance().enableCharacteristicNotification(characteristic, bleCallBack);
                }
                if((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0){
                    BleLog.e("indication");
                    ((EditText) findViewById(R.id.show_notify_characteristic)).setText(characteristic.getUuid().toString());
                   // BleManager.getInstance().enableCharacteristicIndication(characteristic, bleCallBack);
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                    BleManager.getInstance().readCharacteristic(characteristic, new BleCallBack<BluetoothGattCharacteristic>() {
//                        @Override
//                        public void onSuccess(final BluetoothGattCharacteristic characteristic, int type) {
//                            if (characteristic == null) {
//                                return;
//                            }
//                            BleLog.i("readCharacteristic onSuccess:"+ HexUtil.encodeHexStr(characteristic.getValue()));
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                   // showInfo(characteristic.getUuid().toString(), characteristic.getValue());
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onFailure(BleException exception) {
//                            if (exception == null) {
//                                return;
//                            }
//                            BleLog.i("readCharacteristic onFailure:"+exception.getDescription());
//                        }
//                    });
                }
                return true;
            }
        });
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
    }

    private boolean isHexData(String str){
        if (str == null) {
            return false;
        }
        char[] chars = str.toCharArray();
        for(char ch : chars){
            if (ch >='0' && ch <='9')
                continue;
            if (ch >='A' && ch <='F')
                continue;
            if (ch >='a' && ch <='f')
                continue;
            return false;
        }
        return true;
    }
}
