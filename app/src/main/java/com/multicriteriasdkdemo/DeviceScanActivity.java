package com.multicriteriasdkdemo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sinocare.multicriteriasdk.MulticriteriaSDKManager;
import com.sinocare.multicriteriasdk.ScanCallBack;
import com.sinocare.multicriteriasdk.entity.SNDevice;
import com.sinocare.multicriteriasdk.entity.SnBoothType;
import com.sinocare.multicriteriasdk.utils.LogUtils;


/**
 * @author zhongzhigang
 * created at 2017/10/19
 * @file_name DeviceScanActivity.java
 * @description: 扫描蓝牙设备基类
 */
@SuppressLint("NewApi")
public class DeviceScanActivity extends AppCompatActivity {
    protected String uuid;
    private String TAG = this.getClass().getSimpleName();
    protected boolean isEnable;
    private static final int REQUEST_ENABLE_BT = 1;
    // 10秒后停止查找搜索.
    private static final long SCAN_PERIOD = 100000;
    private SNDevice snDevice;
    int snDeviceType;
    ListView deviceLv;
    DeviceScanListAdapter adapter;


    //Bluetooth Device scan callback.
    private final BroadcastReceiver mBlueToothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            LogUtils.d(TAG, "onReceive: " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                adapter.addDevice(device);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_test);
        snDeviceType = getIntent().getIntExtra("snDeviceType", 1);
         snDevice = new SNDevice(snDeviceType);
        setTitle(snDevice.getDesc());
        deviceLv = (ListView) findViewById(R.id.list);
        adapter = new DeviceScanListAdapter(this);
        deviceLv.setAdapter(adapter);
        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showBundleDialog(position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        scanDevice(snDevice.getSnBoothType().getDesc().equals(SnBoothType.BLE) || snDevice.getSnBoothType().getDesc().equals(SnBoothType.BLE_NO_CONNECT));
    }

    protected void scanDevice(boolean isBle) {
        scanBlueTooth(isBle);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    /**
     * 停止扫描
     */
    protected void stopScan() {
        MulticriteriaSDKManager.stopScan();
    }

    private void showBundleDialog(final int position) {
        BluetoothDevice device = adapter.deviceList.get(position);
        new AlertDialog.Builder(DeviceScanActivity.this, R.style.common_dialog).setTitle("提示").setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SNDevice snDevice = new SNDevice(snDeviceType, device.getAddress());
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putParcelable("device", snDevice);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        }).setMessage("您确定绑定该设备吗？\n" + "  mac:" + device.getAddress())
                .setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * 扫描传统蓝牙设备
     *
     * @param enable
     */
    public void scanBlueTooth(final boolean enable) {
            MulticriteriaSDKManager.scanDevice(getApplication(),"", enable,100, new ScanCallBack() {
                @Override
                public void getScanResult(BluetoothDevice scanResult) {
                    LogUtils.d(TAG, "getScanResult: " + scanResult.toString());
                    adapter.addDevice(scanResult);
                }

                @Override
                public void complete() {

                }

                @Override
                public void getData(BluetoothDevice bluetoothDevice, byte[] data) {

                }

            });
            LogUtils.d("查找经典蓝牙", "开始扫描");
    }

    public void startScan(View view) {
        stopScan();
        adapter.clearData();
        scanDevice(snDevice.getSnBoothType().getDesc().equals(SnBoothType.BLE));
    }
}