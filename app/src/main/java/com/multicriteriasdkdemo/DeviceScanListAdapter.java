package com.multicriteriasdkdemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sinocare.multicriteriasdk.entity.SNDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * @file_name DeviceScanListAdapter.java
 * @description:  蓝牙设备列表适配器
 * @author zhongzhigang
 * created at 2017/10/19
 */
public class DeviceScanListAdapter extends BaseAdapter {
    private Context context;
    public List<BluetoothDevice> deviceList;

    public DeviceScanListAdapter(Context context ) {
        this.context = context;
        deviceList = new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device) {
        for (BluetoothDevice bluetoothDevice : deviceList) {
            if (bluetoothDevice.getAddress().equals(device.getAddress())) {
                return;
            }
        }
        deviceList.add(device);
        notifyDataSetChanged();
    }

    public void clearData() {
        deviceList.clear();
        notifyDataSetChanged();
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
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_scan_layout, null);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.deviceMac = (TextView) convertView.findViewById(R.id.device_mac);
            viewHolder.deviceRssi = (TextView) convertView.findViewById(R.id.device_rssi);
            viewHolder.deviceScanRecord = (TextView) convertView.findViewById(R.id.device_scanRecord);
            viewHolder.frame_ll = (LinearLayout) convertView.findViewById(R.id.device_manage_ll);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice bluetoothDevice = deviceList.get(position);
        String deviceName = bluetoothDevice.getName();
        if (position % 2 == 0) {
            viewHolder.frame_ll.setBackgroundColor(Color.parseColor("#FAFAFA"));
        } else {
            viewHolder.frame_ll.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        if (deviceName != null && !deviceName.isEmpty()) {
            viewHolder.deviceName.setText(deviceName);
        } else {
            viewHolder.deviceName.setText("未知设备");
        }
        viewHolder.deviceMac.setText(bluetoothDevice.getAddress());
        return convertView;
    }

//    /**
//     * 给设备加上类型名称
//     * @param deviceName
//     * @return
//     */
//    private String chnageName(String deviceName){
//        for (int i = 0; i < SNDevice.SIZE; i++) {
//            SNDevice snDevice = new SNDevice(i);
//            String  name  = snDevice.getNickName();
//            String[] nicknames = name.split(",");
//            for (int j = 0; j < nicknames.length; j++) {
//                String nickname = nicknames[j];
//                if(deviceName.startsWith(nickname) || deviceName.contains(nickname)){
//                    if(snDevice.getType() == SNDevice.DEVICE_KA_11 || snDevice.getType() == SNDevice.DEVICE_EA_12 ||snDevice.getType() == SNDevice.DEVICE_EA_18|| snDevice.getType() == SNDevice.DEVICE_UG_11){//名称相同
//                        deviceName = deviceName + "(血酮或者血尿酸或UG-11)";
//                        return deviceName;
//                    }else {
//                        deviceName = deviceName+ "(" + snDevice.getDesc()+ ")";
//                    }
//                }
//            }
//        }
//        return deviceName;
//    }

    class ViewHolder {
        LinearLayout frame_ll;
        TextView deviceName;
        TextView deviceMac;
        TextView deviceRssi;
        TextView deviceScanRecord;
    }
}
