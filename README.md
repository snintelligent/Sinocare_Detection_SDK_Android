
# 1. 多指标设备接入SDK说明
Sinocare_Detection_SDK_Android 是三诺生物传感股份有限公司的设备连接的SDK。

## 1.1 文件说明
Sinocare_Detection_SDK_Android 主要是通过aar方式提供给第三发软件开发使用.

## 1.2 使用设备的Android系统版本和蓝牙版本要求
		 设备（手机，平板，电视等）需支持android 5.0 及以上操作系统，支持蓝牙4.0，支持ble
    
# 2. 集成方法

## 2.1 接入前准备
提供app包名和keystore sha1指纹，用于生成sdk接入的access key; 由于debug keystore 和release keystore的证书指纹不一致，为了保证app调试与正式上线后都能正常的鉴权成功，建议利用release keystore改造出一个debug版keystore保证两者证书指纹一致；
``` shell
//获取keystore 指纹命令
keytool -v -list -keystore sinocare-debug.jks

// keystore指纹命令，这里选取sha1指纹；注意：需要移除分号：
证书指纹:
         MD5:  12:F8:35:F3:22:0722:D3:36:22:22:B4:33:0F:9F:05
         SHA1: 72:D2:12:98:33:D3:12:88:E0:CB:6A:2C:77:65:F2:15:25:AE:61:26
         SHA256: E2:01:25:14:57:12:3A:EF:91:F4:5B:3D:94:9A:A2:AA:D0:A9:54:D6:8F:12:25:56:FA:01:76:E9:AB:BA:92:AE
签名算法名称: SHA256withRSA

签名改造：
1、修改release keystore密码为 android： 
keytool -storepasswd -keystore  [path]/yourRelase.keystore
#2、修改别名密码为android：
keytool -keypasswd -keystore [path]/yourRelase.keystore -alias  your-alias
3、修改别名为androiddebugkey：
keytool -changealias -keystore [path]/yourRelase.keystore -alias your_alias -destalias androiddebugkey
4、重命名yourRelase.keystore为debug.keystore，然后替换默认的debug.keystore
```


## 2.2 SDK接入
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
  implementation 'com.sinocare.android_lib:multicriteriasdk:1.0.3'
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
sdk access key配置，示例代码如下，在application标签下配置meta-data, key值sino_minute_access_key，value为申请的access key
``` xml
<application
        android:allowBackup="true"
        android:name=".MyApplication"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <meta-data android:name="sino_minute_access_key"
            android:value="c3ecbb62344af7bbc6271aaabbcccc"/>
	    
</application>
```
# 3.接口说明

## 3.1 初始化SDK、鉴权（只有鉴权通过，sdk才可以正常使用）
```Java
     public class MyApplication extends Application {
		    public MyApplication() {
		        super();
		    }
		
		    @Override
		    public void onCreate() {
		        super.onCreate();
			//如果不需要处理鉴权状态，可以直接传null，sdk里面存储鉴权状态
			 // MulticriteriaSDKManager.initAndAuthentication(this,null)
		        MulticriteriaSDKManager.initAndAuthentication(this, new AuthStatusListener() {

            			@Override
            			public void onAuthStatus(AuthStatus authStatus) {

            			}
        		});
		    }
    }
```

也可以将初始化与鉴权分别调用：
```
  	MulticriteriaSDKManager.init(this); //初始化
        MulticriteriaSDKManager.authentication(new AuthStatusListener() { //鉴权
            @Override
            public void onAuthStatus(AuthStatus authStatus) {
                
            }
        });
```
AnthStatus鉴权状态说明：
```java
    /**
     * SDK鉴权成功
     */
    public static final int  SDK_AUTHENTICATION_SUCCESS = 10000;

    /**
     * accessKey 不正确
     */
    public static final int  SDK_ACCESS_KEY_INCORRECT = 10001;

    /**
     * 包名不正确
     */
    public static final int PACKAGE_NAME_INCORRECT = 10002;

    /**
     * 签名不正确
     */
    public static final int SINATURE_SHA1_INCORRECT = 10003;

    /**
     * 服务器或网络异常，服务器无法正常响应，具体错误查看msg
     */
    public static final int NETWORK_OR_SERVER_ERROR = 10004;


    /**
     * 接口返回服务器异常
     */
    public static final int API_SERVER_ERROR = 500;


    /**
     * AccessKey配置不正确
     */
    public static final int SDK_ACCESS_KEY_INCOORECT = 401;
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

### 6.1 蓝牙设备上显示蓝牙已被连接，但SnCallBack没有回调连接状态，和测量结果；
首先考虑鉴权是否通过，通过``` AuthUtils.isAuthValid()```查看当时鉴权是否成功，也可以在初始化鉴权过程中监听鉴权状态回调；

