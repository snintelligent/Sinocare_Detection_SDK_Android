package com.multicriteriasdkdemo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sinocare.multicriteriasdk.entity.SNDevice;

import java.util.ArrayList;

/**
 * @author zhongzhigang
 * @Description:
 * @date 2019/1/27
 */
public class DeviceAdapter extends BaseAdapter {
    private ArrayList<SNDevice> deviceList;
    private LayoutInflater mInflater;
    private Context context;

    public DeviceAdapter(ArrayList<SNDevice> list, Context context) {
        this.deviceList = list;
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    public ArrayList<SNDevice> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(ArrayList<SNDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public SNDevice getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public SNDevice addDevice(SNDevice device ,boolean flag) {
        if(flag){
            deviceList.add(device);
            notifyDataSetChanged();
            return null;
        }
        for (SNDevice bluetoothDevice : deviceList) {
            if (bluetoothDevice.getType() == device.getType() || bluetoothDevice.getMac().equals(device.getMac())) {
                return bluetoothDevice;
            }
        }
        deviceList.add(device);
        notifyDataSetChanged();
        return null;
    }

    public void removeItem(SNDevice device){
        deviceList.remove(device);
        notifyDataSetChanged();
    }

    public void clearData() {
        deviceList.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
       SNDevice item= deviceList.get(position);
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_item, null);
            viewHolder=new ViewHolder(
                    (View) convertView.findViewById(R.id.list_child),
                    (TextView) convertView.findViewById(R.id.chat_msg)
            );
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        viewHolder.child.setBackgroundResource(R.drawable.msgbox_send);

        String des = item.getDesc() +"--"+ item.getSnBoothType().getDesc();
        if(!TextUtils.isEmpty(item.getMac())){
            des += "(mac:"+item.getMac()+")";
        }
        des += "(型号:"+item.getDeviceName()+")";
        viewHolder.msg.setText(des);

        return convertView;
    }

    class ViewHolder {
        protected View child;
        protected TextView msg;

        public ViewHolder(View child, TextView msg){
            this.child = child;
            this.msg = msg;
        }
    }
}
