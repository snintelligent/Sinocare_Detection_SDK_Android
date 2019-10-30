
# 1. 多指标设备接入SDK说明
Sinocare_Detection_SDK_Android 是三诺生物传感股份有限公司的设备连接的SDK。

## 1.1 文件说明
Sinocare_Detection_SDK_Android 主要是通过aar方式提供给第三发软件开发使用.

## 1.2 使用设备的Android系统版本和蓝牙版本要求
		 设备（手机，平板，电视等）需支持android 5.0 及以上操作系统，支持蓝牙4.0，支持ble
    
# 2. 集成方法

## 2.1 SDK接入
在根目录build.gradle中加入如下配置
```powershell
allprojects {
    repositories {
        maven {
            url 'https://maven.aliyun.com/repository/public'
        }
        maven {
            credentials {
                username 'sEvpvj'
                password 'yKIOo1lt6V'
            }
            url 'https://repo.rdc.aliyun.com/repository/107484-release-klJuvE/'
        }
        maven {
            credentials {
                username 'sEvpvj'
                password 'yKIOo1lt6V'
            }
            url 'https://repo.rdc.aliyun.com/repository/107484-snapshot-ZvSq3g/'
        }
    }
}
```
在App 模块 build.gradle中配置

```powershell
  implementation 'com.sinocare.android_lib:multicriteriasdk:1.0.1-SNAPSHOT'
```

## 2.2 配置manifest
manifest的配置主要包括添加权限,代码示例如下：

```powershell
    <!--蓝牙相关权限-->
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true" /> //只能安装在有蓝牙ble设备上
    <uses-permission android:name="android.permission.BLUETOOTH" /> // 声明蓝牙权限
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" /> //允许程序发现和配对蓝牙设备
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!--打印机需要权限-->
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <!--打印机需要权限-->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> //允许程序获取当前WiFi接入的状态以及WLAN热点的信息
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> //允许程序获取网络信息状态，如当前的网络连接是否有效
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />//允许程序改变WiFi状态
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />//允许程序改变网络状态,如是否联网
```

# 3.接口说明

## 3.1 初始化SDK
```Java
     public class MyApplication extends Application {
		    public MyApplication() {
		        super();
		    }
		
		    @Override
		    public void onCreate() {
		        super.onCreate();
		        MulticriteriaSDKManager.init(this);
		    }
    }
```


## 3.2  连接与断开连接，数据获取
如果targetSdkVersion 小于23，不需要6.0权限处理。
如果是targetSdkVersion 大于等于23，需要6.0权限处理，则需要在启获取权限后，再开始连接
```java
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
```
真正发起连接代码，及连接状态 和 数据解析
```Java
    private void startConnect() {
        SNDevice snDevice = new SNDevice(SNDevice.DEVICE_EA_12, "DC:DC:DC:DC:DC");// 设备类型 ，和 mac地址
        snDevices.add(snDevice);
        MulticriteriaSDKManager.startConnect(snDevices, new SnCallBack() {
            @Override
            public void onDataComing(SNDevice device, DeviceDetectionData data) {
              //设备数据回调,
            }

            @Override
            public void onDeviceStateChange(SNDevice device, BoothDeviceConnectState state) {
             //连接状态回调
        });
    }
```

连接页面停止连接 ，恢复连接
```java
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
```
断开连接：

```java
   MulticriteriaSDKManager.disConectDevice(snDevices);
```

退出App
```java
     MulticriteriaSDKManager.finishAll();
```

## 3.3 数据解析
目前仪器测试完，数据直接会通过SDK回传。
```java
    MulticriteriaSDKManager.startConnect(snDevices, new SnCallBack() {
            @Override
            public void onDataComing(SNDevice device, DeviceDetectionData data) {
              //设备数据回调，解析见后面数据结构
            }

            @Override
            public void onDeviceStateChange(SNDevice device, BoothDeviceConnectState state) {
             //连接状态回调
        });
```

##  3.4 连接状态
```java
public class BoothDeviceConnectState implements Parcelable {
    /**
     * 连接已经断开
     */
    public static final int DEVICE_STATE_DISCONNECTED = 0;
    /**
     * 连接中
     */
    public static final int DEVICE_STATE_CONNECTING = 1;
    /**
     * 已经连接
     */
    public static final int DEVICE_STATE_CONNECTED = 2;
    /**
     * 断开连接中
     */
    public static final int DEVICE_STATE_DISCONNECTING = 3;
   
    //以下目前只支持安稳air+设备
     /**
     * 开始测试
     */
    public static final int DEVICE_STATE_START_TEST = 4;
     /**
     * 关机
     */
    public static final int DEVICE_STATE_SHUTDOWN = 5;
     /**
     * 滴血闪烁
     */
    public static final int DEVICE_STATE_BLOOD_SPARKLING = 6;
     /**
     * 连接成功
     */
    public static final int DEVICE_STATE_CONNECTION_SUCCESS = 7;
     /**
     * 时间设置成功
     */
    public static final int DEVICE_STATE_TIME_SET_SUCCESS = 8;
      /**
     * 清除历史数据成功
     */
    public static final int DEVICE_STATE_CLEAN_DATA_SUCCESS = 9;
     /**
     * 清除历史数据失败
     */
    public static final int DEVICE_STATE_CLEAN_DATA_FAIL = 10;
      /**
     * 无历史数据
     */
    public static final int DEVICE_STATE_NO_DATA = 11;
    }
```

## 3.5 数据结构
### 3.5.1 ea_12 (血糖和尿酸仪)及 ka_11 （血糖和血酮仪）(安稳air+)
```java
public class SnDataEaka extends BaseDetectionData {

    /***************************zzg***************************
     *  String glucose;血糖值
     *  String uaResult;血尿酸
     *  String ketResult;血酮
     *  Unit glucoseUnit;血糖值单位
     *  Unit uaResultUnit;血尿酸单位
     *  Unit ketResultUnit;血酮单位
     *  boolean Lo; 是否低于最低值
     *  boolean HI;是否高于最高值
     ***************************zzg*******************/
     }
```
### 3.5.2 卡迪克（CardioCbek）
```java
public class SnDataCardioCbek extends BaseDetectionData {

    /***************************zzg***************************
     *  String testTime;检测时间
     *  String valueChol; CHOL 总胆固醇
     *  String valueTrig: TRIG 甘油三酯
     *  String valueHdlChol;  HDL CHOL 高密度
     *  String valueCalcLdl; CALC LDL  低密度
     *  String valueTcHdl; TC/HDL   总胆与高密比值
     *  String glucose;血糖
     *  Unit cardioCbekUnit; 单位
     ***************************zzg*******************/
     }

```
### 3.5.3 血压计（Maibobo）
```java
public class SnDataBp extends BaseDetectionData{

    /***************************zzg***************************
     * int bloodMeasureLow :  舒张压
     * int bloodMeasureHigh :  收缩压
     * int checkHeartRate :  心率
     ***************************zzg*******************/
     }
```
## 4 给设备发送指令
### 4.1 安稳air+
	 /**
    	 * 测试连接  对应回调状态码 BoothDeviceConnectState.DEVICE_STATE_CONNECTION_SUCCESS
     	*/
	 MulticriteriaSDKManager.exeCmd(snDevice, SnDeviceOrder.TESTCONNECT);
	
	  /**
    	 * 查询历史数据  
     	*/
	  MulticriteriaSDKManager.exeCmd(snDevice, SnDeviceOrder.HISTORYDATA);
 	 /**
    	 * 设置时间(自动获取系统时间)  对应回调状态码 BoothDeviceConnectState.DEVICE_STATE_TIME_SET_SUCCESS
     	*/
	 MulticriteriaSDKManager.exeCmd(snDevice, SnDeviceOrder.SETTIME);
	 /**
    	 * 清除历史数据 对应回调状态码 		BoothDeviceConnectState.DEVICE_STATE_CLEAN_DATA_SUCCESS\
	 *BoothDeviceConnectState.DEVICE_STATE_CLEAN_DATA_FAIL
     	 */
	  MulticriteriaSDKManager.exeCmd(snDevice, SnDeviceOrder.CLEANHISTORYDATA);

## 5 常见错误码


## 6 常见问题  

