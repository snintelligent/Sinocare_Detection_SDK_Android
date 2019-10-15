package com.multicriteriasdkdemo;

/**
 * Created by tangpanxing on 2017/6/9.
 */
public class Action {
    //设备连接模块
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECTING = "com.example.bluetooth.le.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_BLUETOOTH_STATUS = "android.bluetooth.adapter.action.STATE_CHANGED";
    //设备扫描模块
    public final static String ACTION_GATT_SCANRESULT = "com.sanocare.minute.clinic.scanresult";
    public final static String DEVICE_DATA = "com.tang.bluetooth.device";
    public final static String DEVICE_REFRESH = "com.tang.bluetooth.refresh";
}
