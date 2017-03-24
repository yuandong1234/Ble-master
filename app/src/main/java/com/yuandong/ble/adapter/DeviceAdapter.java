package com.yuandong.ble.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.ble.model.BleDevice;
import com.ble.utils.HexUtil;
import com.yuandong.ble.R;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<BleDevice> deviceList;

    public DeviceAdapter(Context context) {
        this.context = context;
    }

    public DeviceAdapter setDeviceList(List<BleDevice> deviceList) {
        this.deviceList = deviceList;
        notifyDataSetChanged();
        return this;
    }

    @Override
    public int getCount() {
        return deviceList != null ? deviceList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return deviceList != null ? deviceList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_scan_layout, null);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.deviceMac = (TextView) convertView.findViewById(R.id.device_mac);
            viewHolder.deviceRssi = (TextView) convertView.findViewById(R.id.device_rssi);
            viewHolder.deviceScanRecord = (TextView) convertView.findViewById(R.id.device_scanRecord);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(deviceList != null && deviceList.get(position) != null && deviceList.get(position).getDevice() != null){
            String deviceName = deviceList.get(position).getDevice().getName();
            if(!TextUtils.isEmpty(deviceName)){
                viewHolder.deviceName.setText(deviceName);
            } else{
                viewHolder.deviceName.setText(context.getString(R.string.unknown_device));
            }

            viewHolder.deviceMac.setText(context.getString(R.string.label_address)+deviceList.get(position).getDevice().getAddress());
            viewHolder.deviceRssi.setText(context.getString(R.string.label_rssi)+deviceList.get(position).getmCurrentRssi()+"dB");
            viewHolder.deviceScanRecord.setText(context.getString(R.string.header_scan_record)+":"+ HexUtil.encodeHexStr(deviceList.get(position).getScanRecord()));
        }
        return convertView;
    }

    class ViewHolder{
        TextView deviceName;
        TextView deviceMac;
        TextView deviceRssi;
        TextView deviceScanRecord;
    }
}
