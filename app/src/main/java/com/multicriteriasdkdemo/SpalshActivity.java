package com.multicriteriasdkdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sinocare.multicriteriasdk.MulticriteriaSDKManager;
import com.sinocare.multicriteriasdk.auth.AuthStatusListener;
import com.sinocare.multicriteriasdk.db.SharedPreferencesUtils;
import com.sinocare.multicriteriasdk.entity.SNDevice;
import com.sinocare.multicriteriasdk.entity.SnBoothType;
import com.sinocare.multicriteriasdk.utils.AuthStatus;
import com.sinocare.multicriteriasdk.utils.LogUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SpalshActivity extends AppCompatActivity implements PopupWindowChooseType.OpClick {

    private static final String TAG = SpalshActivity.class.getSimpleName();
    private ListView mListView;
    private ArrayList<SNDevice> list = new ArrayList<>();
    private DeviceAdapter deviceAdapter;
    private boolean blueToothPermissFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);
        MulticriteriaSDKManager.initAndAuthentication(getApplication(), new AuthStatusListener() {
            @Override
            public void onAuthStatus(AuthStatus authStatus) {
                Log.d(TAG,authStatus.getMsg());
            }
        });
        MulticriteriaSDKManager.setLogHandler(new LogUtils.LogListener() {
            @Override
            public void d(String s, String s1) {
                Log.d(s, s1);
            }

            @Override
            public void e(String s, String s1) {
                Log.e(s, s1);
            }

            @Override
            public void i(String s, String s1) {
                Log.i(s, s1);
            }

            @Override
            public void v(String s, String s1) {
                Log.v(s, s1);
            }

            @Override
            public void w(String s, String s1) {
                Log.w(s, s1);
            }
        });
        initData();
        initPermiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            SNDevice snDevice = data.getExtras().getParcelable("device");
            SNDevice comfirmDevice = deviceAdapter.addDevice(snDevice, false);
            if (comfirmDevice != null) {//去掉原来的，直接替换为新设备，每种类型设备只能保留一个
                deviceAdapter.removeItem(comfirmDevice);
                deviceAdapter.addDevice(snDevice, true);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initData() {
        mListView = findViewById(R.id.list);
        list = new ArrayList<>();
        Map<String, ?> objectMap = SharedPreferencesUtils.getAll(this);
        for (String s : objectMap.keySet()) {
            Object value = objectMap.get(s);
            if (value instanceof Integer) {
                list.add(new SNDevice((Integer) value, s));
            }
        }
        deviceAdapter = new DeviceAdapter(list, this);
        mListView.setAdapter(deviceAdapter);
        mListView.setOnItemLongClickListener((parent, view, position, id) -> {
            deviceAdapter.removeItem(deviceAdapter.getItem(position));
            return false;
        });
    }

    private void initPermiss() {
        RxPermissions rxPermissions = new RxPermissions(this);
        if(isAndroid12()){
            rxPermissions.request(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                    .subscribe(granted -> {
                        blueToothPermissFlag = granted;
                    });
        }else{
            rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(granted -> {
                        blueToothPermissFlag = granted;
                    });
        }
    }
    private boolean isAndroid12(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ;
    }
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferencesUtils.clear(this);
        Map<String, Object> objectMap = new HashMap<>();
        ArrayList<SNDevice> snDevices = deviceAdapter.getDeviceList();
        for (SNDevice snDevice : snDevices) {
            objectMap.put(snDevice.getMac(), snDevice.getType());
        }
        SharedPreferencesUtils.putMap(objectMap, this);
    }

    @Override
    public void finish() {
        super.finish();
        MulticriteriaSDKManager.finishAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void addDevice(View view) {
        if (!blueToothPermissFlag) {
            initPermiss();
            return;
        }
        PopupWindowChooseType popupWindowChooseType = new PopupWindowChooseType(this, this);
        popupWindowChooseType.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    @Override
    public void goTestActivity(int snDeviceType) {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        intent.putExtra("snDeviceType", snDeviceType);
        SNDevice snDevice = new SNDevice(snDeviceType);
        int deviceType = 1;
        if (SnBoothType.BLE.equals(snDevice.getSnBoothType().getDesc())) {
            deviceType = 1;
        } else if (SnBoothType.UN_BLE.equals(snDevice.getSnBoothType().getDesc())) {
            deviceType = 0;
        } else if (SnBoothType.OTHER.equals(snDevice.getSnBoothType().getDesc())) {
            if(snDevice.getType() == SNDevice.DEVICE_ID_CARD || snDevice.getType() == SNDevice.DEVICE_GPRINT){
                deviceType = 0;
            }
        }
        intent.putExtra("deviceType", deviceType);
        startActivityForResult(intent, 1);
    }

    public void startTest(View view) {
        if (deviceAdapter.getDeviceList().size() == 0) {
            Toast.makeText(this, "请先绑定设备", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("snDevices", deviceAdapter.getDeviceList());
        startActivity(intent);
    }
}
