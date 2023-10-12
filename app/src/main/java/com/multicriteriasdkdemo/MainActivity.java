package com.multicriteriasdkdemo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sinocare.multicriteriasdk.DeviceCmdS;
import com.sinocare.multicriteriasdk.MulticriteriaSDKManager;
import com.sinocare.multicriteriasdk.SnCallBack;
import com.sinocare.multicriteriasdk.auth.AuthUtils;
import com.sinocare.multicriteriasdk.bean.BaseDetectionData;
import com.sinocare.multicriteriasdk.bean.DeviceDetectionData;
import com.sinocare.multicriteriasdk.bean.StandardBodyData;
import com.sinocare.multicriteriasdk.entity.BoothDeviceConnectState;
import com.sinocare.multicriteriasdk.entity.DeviceDetectionState;
import com.sinocare.multicriteriasdk.entity.SNDevice;
import com.sinocare.multicriteriasdk.entity.SampleType;
import com.sinocare.multicriteriasdk.entity.SnDeviceOrder;
import com.sinocare.multicriteriasdk.entity.SnPrintInfo;
import com.sinocare.multicriteriasdk.utils.LogUtils;
import com.sinocare.multicriteriasdk.utils.TimerHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    List<SNDevice> snDevices = new ArrayList<>();
    private ListView listViewStatus;
    private MsgListAdapter statusAdapter;
    private ListView listViewData;
    private MsgListAdapter dataAdapter;
    public Map<String, BoothDeviceConnectState> stateHashMap = new HashMap<>();
    private final int count = 0;
    private TextView tv;
    //    private String anwenMac;
//    private Map<String, String> map;

    private Disposable mKangTaiSpO2Disposable;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        map = new HashMap<>();
        Log.d(TAG, "鉴权结果： " + AuthUtils.isAuthValid() + "Token值： " + AuthUtils.getAccessToken());
        setTitle("测量界面");
        listViewStatus = findViewById(R.id.list_status);
        statusAdapter = new MsgListAdapter(this, new ArrayList<>());
        listViewStatus.setAdapter(statusAdapter);

        listViewData = findViewById(R.id.list_data);
        tv = findViewById(R.id.tv);
        dataAdapter = new MsgListAdapter(this, new ArrayList<>());
        listViewData.setAdapter(dataAdapter);
        ArrayList<SNDevice> bleDevices = (  ArrayList<SNDevice> )getIntent().getSerializableExtra("snDevices");
        if (bleDevices == null) {
            Toast.makeText(this, "设备选择不正确", Toast.LENGTH_SHORT);
            finish();
            return;
        }
        snDevices.addAll(bleDevices);
        startConnect();


    }


    /**
     * 开始测量
     *
     * @return void
     * @author zhongzhigang
     * @time 2019/2/26 20:50
     */
    private void startConnect() {
        MulticriteriaSDKManager.startConnect(snDevices, false, new SnCallBack() {
            @Override
            public void onDataComing(SNDevice device, BaseDetectionData data) {
                LogUtils.d(TAG, "onDataComing: ------snDevice---" + data.getData());
                String msg = device.getName() + "收到数据：" + JsonUtils.toJson(data);
                MsgListAdapter.DeviceListItem deviceListItem = new MsgListAdapter.DeviceListItem(msg, false);
                dataAdapter.addMsgItem(deviceListItem);
                listViewData.setSelection(0);
            }

            @Override
            public void onDeviceStateChange(SNDevice device, BoothDeviceConnectState state) {
                LogUtils.d(TAG, "onDeviceStateChange: -----snDevice----" + device.toString());
                switch (state.getmState()) {
                    case BoothDeviceConnectState.DEVICE_STATE_CONNECTED:
                        if (device.getDataProtocolCode() == SNDevice.DEVICE_CMS5D_OXIMETER_BLE) {
                            if (mKangTaiSpO2Disposable != null && !mKangTaiSpO2Disposable.isDisposed()) {
                                mKangTaiSpO2Disposable.dispose();
                            }
                            mKangTaiSpO2Disposable = Observable.interval(1, 15, TimeUnit.SECONDS).observeOn(Schedulers.single()).subscribe(aLong -> {
                                MulticriteriaSDKManager.exeCmd(device, DeviceCmdS.KANGTAI_SPO2_START_REALTIME_DATA_CMD);
                            });
                        }
                        break;
                    case BoothDeviceConnectState.DEVICE_STATE_DISCONNECTED:
                        if (mKangTaiSpO2Disposable != null && !mKangTaiSpO2Disposable.isDisposed()) {
                            mKangTaiSpO2Disposable.dispose();
                        }
                        break;
                }
                MsgListAdapter.DeviceListItem deviceList;
                deviceList = new MsgListAdapter.DeviceListItem(device.getName() + "(" + state.toString() + ")", false);
                deviceList.setSnDevice(device);
                statusAdapter.addMsgItem(deviceList);
                listViewStatus.setSelection(0);


            }

            @Override
            public void onDetectionStateChange(SNDevice device, DeviceDetectionState detectionState) {
                MsgListAdapter.DeviceListItem deviceList;
                if (detectionState.getStatus() == DeviceDetectionState.DetectionStateEnum.DEVICEINFO_SN) {
                    if (TextUtils.isEmpty(device.getSn()) || device.getSn().contains("00000000000")) {
                        return;
                    } else {
                        deviceList = new MsgListAdapter.DeviceListItem(device.getName() + "(" + detectionState.getStatus() + ")" + "sn:" + device.getSn(), false);

                    }
                } else {
                    deviceList = new MsgListAdapter.DeviceListItem(device.getName() + "(" + detectionState.getStatus() + ")", false);
                }
                deviceList.setSnDevice(device);
                deviceList.setDeviceDetectionState(detectionState);
                statusAdapter.addMsgItem(deviceList);
                listViewStatus.setSelection(0);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mKangTaiSpO2Disposable != null && !mKangTaiSpO2Disposable.isDisposed()) {
            mKangTaiSpO2Disposable.dispose();
        }
    }


    @Override
    public void finish() {
        super.finish();
        LogUtils.d(TAG, "finish: ");
        //TODO 断开连接
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
