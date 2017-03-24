package com.yuandong.ble;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ble.model.BleDevice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DeviceDetailActivity extends AppCompatActivity {
    private TextView tvName;
    private TextView tvAddress;
    //    private TextView tvClass;
//    private TextView tvMajorClass;
//    private TextView tvServices;
    private TextView tvBondingState;

    private TextView tvFirstTimestamp;
    private TextView tvFirstRssi;
    private TextView tvLastTimestamp;
    private TextView tvLastRssi;
    // private TextView tvRunningAverageRssi;

    private BleDevice bleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        bleDevice = getIntent().getParcelableExtra(MainActivity.EXTRA_DEVICE);

        initView();
    }

    private void initView() {
        tvName = (TextView) findViewById(R.id.deviceName);
        tvAddress = (TextView) findViewById(R.id.deviceAddress);
//        tvClass = (TextView) findViewById(R.id.deviceClass);
//        tvMajorClass = (TextView)findViewById(R.id.deviceMajorClass);
//        tvServices = (TextView)findViewById(R.id.deviceServiceList);
        tvBondingState = (TextView) findViewById(R.id.deviceBondingState);
        tvFirstTimestamp = (TextView) findViewById(R.id.firstTimestamp);
        tvFirstRssi = (TextView) findViewById(R.id.firstRssi);
        tvLastTimestamp = (TextView) findViewById(R.id.lastTimestamp);
        tvLastRssi = (TextView) findViewById(R.id.lastRssi);
        //tvRunningAverageRssi = (TextView)findViewById(R.id.runningAverageRssi);
        tvName.setText(bleDevice.getName());
        tvAddress.setText(bleDevice.getAddress());
        //tvClass.setText(bleDevice.getBluetoothDeviceClassName());
        // tvMajorClass.setText(bleDevice.getBluetoothDeviceMajorClassName());
        tvBondingState.setText(bleDevice.getBleBondStatue());

        tvFirstTimestamp.setText(formatTime(bleDevice.getmFirstTimestamp()));
        tvFirstRssi.setText(formatRssi(bleDevice.getmFirstRssi()));
        tvLastTimestamp.setText(formatTime(bleDevice.getmCurrentTimestamp()));
        tvLastRssi.setText(formatRssi(bleDevice.getmCurrentRssi()));
        //  tvRunningAverageRssi.setText(formatRssi(device.getRunningAverageRssi()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                if (bleDevice == null) return false;
                //Intent intent = new Intent(DeviceDetailActivity.this, DeviceControlActivity.class);
                Intent intent = new Intent(DeviceDetailActivity.this, OperateActivity.class);
                intent.putExtra(MainActivity.EXTRA_DEVICE, bleDevice);
                startActivity(intent);
                break;
        }
        return true;
    }

    private String formatRssi(final int rssi) {
        return getString(R.string.formatter_db, String.valueOf(rssi));
    }

    private static String formatTime(final long time) {
        String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ISO_FORMAT, Locale.CHINA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(new Date(time));
    }
}
