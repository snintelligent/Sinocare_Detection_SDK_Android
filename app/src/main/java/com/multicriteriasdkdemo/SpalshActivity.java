package com.multicriteriasdkdemo;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sinocare.multicriteriasdk.MulticriteriaSDKManager;
import com.sinocare.multicriteriasdk.auth.AuthStatusListener;
import com.sinocare.multicriteriasdk.db.SharedPreferencesUtils;
import com.sinocare.multicriteriasdk.entity.SNDevice;
import com.sinocare.multicriteriasdk.utils.AuthStatus;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SpalshActivity extends AppCompatActivity {

    private static final String TAG = SpalshActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private ArrayList<SNDevice> list = new ArrayList<>();
    private DeviceInfoListAdapter deviceAdapter;
    private boolean blueToothPermissFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalsh);
        MulticriteriaSDKManager.initAndAuthentication(getApplication(), new AuthStatusListener() {
            @Override
            public void onAuthStatus(AuthStatus authStatus) {
                Log.e(TAG, authStatus.toString());
            }
        });
        initData();
        initPermiss();

    }

    private void initData() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        list = new ArrayList<>();
        Map<String, ?> objectMap = SharedPreferencesUtils.getAll(this);

        for (String s : objectMap.keySet()) {
            String value = String.valueOf(objectMap.get(s));
            try {
                JSONObject jsonObject = new JSONObject(value);
                SNDevice snDevice = new SNDevice();
                snDevice.setMac(jsonObject.getString("mac"));
                snDevice.setMachineCode(jsonObject.getString("machineCode"));
                snDevice.setBleNamePrefix(jsonObject.getString("bleNamePrefix"));
                snDevice.setDataProtocolCode(jsonObject.getString("dataProtocolCode"));
                snDevice.setImageUrl(jsonObject.getString("imageUrl"));
                snDevice.setProductCode(jsonObject.getString("productCode"));
                snDevice.setName(jsonObject.getString("name"));
                list.add(snDevice);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        deviceAdapter = new DeviceInfoListAdapter(list, this);
        recyclerView.setAdapter(deviceAdapter);

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
        List<SNDevice> snDevices = deviceAdapter.getDeviceList();
        for (SNDevice snDevice : snDevices) {
            objectMap.put(snDevice.getMac(), JsonUtils.toJson(snDevice));
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
        Intent intent = new Intent(this, DeviceInfoListActivity.class);
        startActivityForResult(intent, 1);
    }




    public void goTestActivity(SNDevice snDevice) {
        Intent intent = new Intent(this, DeviceScanActivity.class);
        intent.putExtra("snDevice", snDevice);
        startActivityForResult(intent, 2);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 1) {
            SNDevice snDevice = data.getExtras().getParcelable("device");
            goTestActivity(snDevice);
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 2) {
            SNDevice snDevice = data.getExtras().getParcelable("device");
            for (SNDevice snDevice1 : deviceAdapter.getDeviceList()) {
                if (snDevice.getMac().equals(snDevice1.getMac())) {
                    return;
                }
            }
            deviceAdapter.addDevice(snDevice);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
