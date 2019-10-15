package com.multicriteriasdkdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sinocare.multicriteriasdk.entity.BoothDeviceConnectState;
import com.sinocare.multicriteriasdk.entity.SNDevice;
import com.sinocare.multicriteriasdk.utils.TimerHelper;

import java.util.ArrayList;

public class MsgListAdapter extends BaseAdapter {
    private ArrayList<DeviceListItem> list;
    private LayoutInflater mInflater;
    private Context context;
    public MsgListAdapter(Context context, ArrayList<DeviceListItem> l) {
    	list = l;
		mInflater = LayoutInflater.from(context);
		this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder viewHolder = null;
        DeviceListItem item=list.get(position);
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
        
        if(item.isSiri)
        {
        	viewHolder.child.setBackgroundResource(R.drawable.msgbox_rec);
        }
        else 
        {
        	viewHolder.child.setBackgroundResource(R.drawable.msgbox_send);
        }
        viewHolder.msg.setText("时间：" + item.date + "\n" + item.message);
        //LogUtil.log("data", item.message.trim());
        System.out.println(item.message.trim());
        return convertView;
    }

    public void clear(){
        list.clear();
        notifyDataSetChanged();
    }

    public void addMsgItem(DeviceListItem item){
        list.add(0,item);
        notifyDataSetChanged();
    }

    class ViewHolder {
    	  protected View child;
          protected TextView msg;
  
          public ViewHolder(View child, TextView msg){
              this.child = child;
              this.msg = msg;
              
          }
    }

    public static class DeviceListItem {
        String message;
        boolean isSiri;//是否为命令开始
         String date;
         SNDevice snDevice;
        BoothDeviceConnectState state ;

        public DeviceListItem(String msg, boolean siri) {
            message = msg;
            isSiri = siri;
            date = TimerHelper.getNowSystemTimeHHMMSS();
        }

        public SNDevice getSnDevice() {
            return snDevice;
        }

        public void setSnDevice(SNDevice snDevice) {
            this.snDevice = snDevice;
        }

        public BoothDeviceConnectState getState() {
            return state;
        }

        public void setState(BoothDeviceConnectState state) {
            this.state = state;
        }
    }

}
