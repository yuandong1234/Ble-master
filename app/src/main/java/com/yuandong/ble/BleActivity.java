package com.yuandong.ble;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ble.BleManager;
import com.ble.callback.scan.BleScanCallback;
import com.ble.callback.scan.NewBleScanCallBack;
import com.ble.model.BleDevice;
import com.ble.model.BleDeviceStore;
import com.ble.utils.BleLog;
import com.ble.utils.BleUtil;
import com.yuandong.ble.adapter.DeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class BleActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "extra_device";
    private TextView supportTv, statusTv, scanCountTv;
    private ListView deviceLv;
    private BleDeviceStore bleDeviceStore;
    private volatile List<BleDevice> bleDeviceList = new ArrayList<>();
    private DeviceAdapter adapter;

    private BleScanCallback scanCallback = new BleScanCallback() {
        @Override
        public void scanTimeout() {
            BleLog.e("扫描结束");
        }

        @Override
        public void onDeviceFound(BleDevice bleDevice) {
            if (bleDeviceStore != null) {
                bleDeviceStore.addDevice(bleDevice);
                bleDeviceList = bleDeviceStore.getDeviceList();
            }
            adapter.setDeviceList(bleDeviceList);
            updateItemCount(adapter.getCount());
        }
    };
    private NewBleScanCallBack newScanCallBack = new NewBleScanCallBack() {
        @Override
        public void scanTimeout() {
            BleLog.e("扫描结束");
        }

        @Override
        public void onDeviceFound(BleDevice bleDevice) {
            if (bleDeviceStore != null) {
                bleDeviceStore.addDevice(bleDevice);
                bleDeviceList = bleDeviceStore.getDeviceList();
            }
            adapter.setDeviceList(bleDeviceList);
            updateItemCount(adapter.getCount());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_main);
        supportTv = (TextView) findViewById(R.id.scan_ble_support);
        statusTv = (TextView) findViewById(R.id.scan_ble_status);
        deviceLv = (ListView) findViewById(android.R.id.list);
        scanCountTv = (TextView) findViewById(R.id.scan_device_count);
        BleManager.getInstance().init(MyApplication.getInstance());
        bleDeviceStore = new BleDeviceStore();
        adapter = new DeviceAdapter(this);
        deviceLv.setAdapter(adapter);
        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                BleDevice device = (BleDevice) adapter.getItem(position);
                if (device == null) return;
                Intent intent = new Intent(BleActivity.this, DeviceDetailActivity.class);
                intent.putExtra(EXTRA_DEVICE, device);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isSupport = BleUtil.isSupportBle(this);
        boolean isOpenBle = BleUtil.isBleEnable(this);
        if (isSupport) {
            supportTv.setText(getString(R.string.supported));
        } else {
            supportTv.setText(getString(R.string.not_supported));
        }
        if (isOpenBle) {
            statusTv.setText(getString(R.string.on));
        } else {
            statusTv.setText(getString(R.string.off));
        }
        invalidateOptionsMenu();
        if (BleUtil.isBleEnable(this)) {
            startScan();
        } else {
            BleUtil.enableBluetooth(this, 1);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            statusTv.setText(getString(R.string.on));
            if (requestCode == 1) {
                startScan();
            }
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startScan() {
        updateItemCount(0);
        if (bleDeviceStore != null) {
            bleDeviceStore.clear();
        }
        if (adapter != null && bleDeviceList != null) {
            bleDeviceList.clear();
            adapter.setDeviceList(bleDeviceList);
        }
        //不同版本的ble 扫描API不同
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            BleManager.getInstance().setScanTimeout(10000).startScan(newScanCallBack);
//        } else {
//            BleManager.getInstance().setScanTimeout(10000).startScan(scanCallback);
//        }
        BleManager.getInstance().setScanTimeout(10000).startScan(scanCallback);
    }

    private void stopScan() {

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            BleManager.getInstance().stopScan(newScanCallBack);
//        } else {
//            BleManager.getInstance().stopScan(scanCallback);
//        }

        BleManager.getInstance().stopScan(scanCallback);

    }

    private void updateItemCount(int count) {
        scanCountTv.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
