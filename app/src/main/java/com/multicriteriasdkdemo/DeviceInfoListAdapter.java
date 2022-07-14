package com.multicriteriasdkdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sinocare.multicriteriasdk.entity.SNDevice;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**************************************************
 * @file_name
 * @description:
 * @author baojiji
 * created : 2022/4/21
 ********************************************/
public class DeviceInfoListAdapter extends RecyclerView.Adapter<DeviceInfoListAdapter.ViewHolder> {
    private ArrayList<SNDevice> dataList;
    private Context context;
    private OnItemClickListener mOnItemClickListener;

    DeviceInfoListAdapter(ArrayList<SNDevice> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }
    public ArrayList<SNDevice> getDeviceList(){
        return dataList;
    }
    public void addDevice(SNDevice snDevice){
         dataList.add(snDevice);
         notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adpter_itme_device_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SNDevice snDevice = dataList.get(position);
        holder.name.setText(snDevice.getName());
        holder.mac.setText(TextUtils.isEmpty(snDevice.getMac()) ? "" : snDevice.getMac());
        if(snDevice.getImageUrl() != null && !TextUtils.isEmpty(snDevice.getImageUrl())){
            setNetworkBitmap(snDevice.getImageUrl(),holder.imageView);
        }else{
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.ic_launcher_foreground));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener!=null){
                    //当传入的mOnItemClickListener不为空就执行其中的方法
                    mOnItemClickListener.onItemClick(snDevice);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View view) {
                dataList.remove(holder.getAdapterPosition());
                notifyDataSetChanged();
                return false;
            }
        });
    }
    public void setNetworkBitmap(String url,ImageView imageView) {
        Runnable networkImg = new Runnable() {
            @Override
            public void run() {
                try {
                    URL conn = new URL(url);
                    InputStream in = conn.openConnection().getInputStream();
                   Bitmap bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                    Activity activity = (Activity)context;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(networkImg).start();

    }
    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView name;
        private TextView mac;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            mac = itemView.findViewById(R.id.tvMac);
            imageView = itemView.findViewById(R.id.image);
        }
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
    public interface OnItemClickListener{
        void onItemClick(SNDevice snDevice);
    }
}