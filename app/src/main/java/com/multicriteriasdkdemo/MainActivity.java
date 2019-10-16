package com.multicriteriasdkdemo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sinocare.multicriteriasdk.MulticriteriaSDKManager;
import com.sinocare.multicriteriasdk.SnCallBack;
import com.sinocare.multicriteriasdk.bean.DeviceDetectionData;
import com.sinocare.multicriteriasdk.entity.BoothDeviceConnectState;
import com.sinocare.multicriteriasdk.entity.SNDevice;
import com.sinocare.multicriteriasdk.entity.SnPrintInfo;
import com.sinocare.multicriteriasdk.utils.LogUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    List<SNDevice> snDevices = new ArrayList<>();
    private ListView listViewStatus;
    private MsgListAdapter statusAdapter;
    private ListView listViewData;
    private MsgListAdapter dataAdapter;
    public Map<String, BoothDeviceConnectState> stateHashMap = new HashMap<>();
    private int count= 0;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("测量界面");
        listViewStatus = findViewById(R.id.list_status);
        statusAdapter = new MsgListAdapter(this, new ArrayList<>());
        listViewStatus.setAdapter(statusAdapter);
        listViewStatus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MsgListAdapter.DeviceListItem deviceListItem = ((MsgListAdapter.DeviceListItem) statusAdapter.getItem(position));
                if (deviceListItem.getState().getmState() != BoothDeviceConnectState.DEVICE_STATE_CONNECTED) {
                    Toast.makeText(MainActivity.this, "设备未连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                SNDevice snDevice = ((MsgListAdapter.DeviceListItem) statusAdapter.getItem(position)).getSnDevice();
                SnPrintInfo snPrintInfo = new SnPrintInfo();
                snPrintInfo.setAge(45);
                snPrintInfo.setName("三诺糖友");
                snPrintInfo.setSex(1);
                snPrintInfo.setPrintTitle("三诺");
                snPrintInfo.setTestTime("2019-10-10 14:30:24");
                List<SnPrintInfo.TestItem> testItems = new ArrayList<>();
                SnPrintInfo.TestItem testItem = new SnPrintInfo.TestItem();
                testItem.setMedicalCode("HbA1c");
                testItem.setMedicalName("糖化血红蛋白");
                testItem.setMedicalMethod("亲和色谱法");
                testItem.setMedicalUnits("%");
                testItem.setMedicalResult("3");
                testItems.add(testItem);
                snPrintInfo.setTestItemList(testItems);
                MulticriteriaSDKManager.exeCmd(snDevice, snPrintInfo);
            }
        });
        listViewData = findViewById(R.id.list_data);
        tv = findViewById(R.id.tv);
        dataAdapter = new MsgListAdapter(this, new ArrayList<>());
        listViewData.setAdapter(dataAdapter);
        ArrayList<SNDevice> bleDevices = getIntent().getParcelableArrayListExtra("snDevices");
        if (bleDevices == null) {
            Toast.makeText(this, "设备选择不正确", Toast.LENGTH_SHORT);
            finish();
            return;
        }
        snDevices.addAll(bleDevices);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.READ_PHONE_STATE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
                    startConnect();
                } else {
                    Toast.makeText(MainActivity.this, "请先给设备赋权限", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    /**
     * 开始测量
     *
     * @return void
     * @author zhongzhigang
     * @time 2019/2/26 20:50
     */
    private void startConnect() {
        MulticriteriaSDKManager.startConnect(snDevices, new SnCallBack() {
            @Override
            public void onDataComing(SNDevice device, DeviceDetectionData data) {
                LogUtils.d(TAG, "onDataComing: ------snDevice---" + device.toString());
                LogUtils.d(TAG, "onDataComing: -----data----" + data);
                String msg = device.getDesc() + "收到数据：(" + data.toString() + ")";
                MsgListAdapter.DeviceListItem deviceListItem = new MsgListAdapter.DeviceListItem(msg, false);
                dataAdapter.addMsgItem(deviceListItem);
                listViewData.setSelection(0);
            }

            @Override
            public void onDeviceStateChange(SNDevice device, BoothDeviceConnectState state) {
                LogUtils.d(TAG, "onDeviceStateChange: -----snDevice----" + device.toString());
                if (state.getmState() != BoothDeviceConnectState.DEVICE_STATE_CONNECTED
                        && state.getmState() != BoothDeviceConnectState.DEVICE_STATE_DISCONNECTED) {
                    return;
                }
                BoothDeviceConnectState state1 = stateHashMap.get(device.getMac());
                if (state1 != null && state1.getmState() == state.getmState()) {
                    return;
                }
                stateHashMap.put(device.getMac(), state);
//                if(state.getmState() == BoothDeviceConnectState.DEVICE_STATE_CONNECTED){
//                    count++;
//                }else {
//                    count--;
//                }
//                tv.setText("已连接设备数：" + count + "个");
                String extendString = "";
                if (device.getType() == SNDevice.DEVICE_GPRINT) {
                    extendString = "（点击打印样板）";
                }
                String msg = device.getDesc() + "(" + state.getDesc() + ")";
                boolean isSiri = false;
                MsgListAdapter.DeviceListItem deviceListItem = new MsgListAdapter.DeviceListItem(msg + extendString, isSiri);
                deviceListItem.setSnDevice(device);
                deviceListItem.setState(state);
                statusAdapter.addMsgItem(deviceListItem);
                listViewStatus.setSelection(0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MulticriteriaSDKManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MulticriteriaSDKManager.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        MulticriteriaSDKManager.disConectDevice(snDevices);
        snDevices.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy: ");
    }

    public void clearStatusRecord(View view) {
        statusAdapter.clear();
    }

    public void clearDataRecord(View view) {
        dataAdapter.clear();
    }

}
