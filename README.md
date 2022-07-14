

### (注意)此版本为旧版本SDK 1.X.X, 需要新版SDK请点击[新版SDK 2.X.X](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/tree/new_sdk/)




# 1. 多指标设备接入SDK说明

Sinocare_Detection_SDK_Android
是三诺生物传感股份有限公司开发的设备连接的SDK，目前已接入包括三诺自有设备在内的20多款检测设设备；可以便捷的实现多款蓝牙设备的同时连接与数据接收，极大的简化了蓝牙设备的接入流程，接入用户只需要专注自身业务的开发，降低了用户接入蓝牙设备的开发成本；同时会为接入用户提供对应的技术支持，及时的响应客户的需求；

## 1.1 文件说明

Sinocare_Detection_SDK_Android 主要是通过aar方式提供给第三发软件开发使用.

## 1.2 使用设备的Android系统版本和蓝牙版本要求

		 设备（手机，平板，电视等）需支持android 5.0 及以上操作系统，支持蓝牙4.0，支持ble

# 2. 集成方法

## 2.1 接入前准备

提供app包名和keystore sha1指纹，用于生成sdk接入的access key; 由于debug keystore 和release
keystore的证书指纹不一致，为了保证app调试与正式上线后都能正常的鉴权成功，建议利用release keystore改造出一个debug版keystore保证两者证书指纹一致；

``` shell
//获取keystore 指纹命令
keytool -v -list -keystore sinocare-debug.jks

// keystore指纹命令，这里选取sha1指纹；注意：需要移除冒号：
证书指纹:
         MD5:  12:F8:35:F3:22:0722:D3:36:22:22:B4:33:0F:9F:05
         SHA1: 72:D2:12:98:33:D3:12:88:E0:CB:6A:2C:77:65:F2:15:25:AE:61:26
         SHA256: E2:01:25:14:57:12:3A:EF:91:F4:5B:3D:94:9A:A2:AA:D0:A9:54:D6:8F:12:25:56:FA:01:76:E9:AB:BA:92:AE
签名算法名称: SHA256withRSA

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
           maven { url 'https://jitpack.io' }
    }
}
```

在App 模块 build.gradle中配置

```powershell
  implementation 'com.sinocare.android_lib:multicriteriasdk:1.2.73'
```

## 2.3 配置manifest

manifest的配置主要包括添加权限,代码示例如下：

```powershell
    <!--蓝牙相关权限-->
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true" /> //只能安装在有蓝牙ble设备上
    <uses-permission android:name="android.permission.BLUETOOTH" /> // 声明蓝牙权限
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" /> //允许程序发现和配对蓝牙设备
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> //允许程序获取网络信息状态，如当前的网络连接是否有效
```

## 2.4 动态权限申请 

如果targetSdkVersion 小于23，不需要6.0权限处理。如果是targetSdkVersion 大于等于23，需要6.0权限处理，则需要在启获取权限后，再开始连接

```java
           //申请权限
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},1);
```

sdk access key配置，示例代码如下，在application标签下配置meta-data, key值sino_minute_access_key，value为申请的access key

``` xml
<application ...>

        <meta-data android:name="sino_minute_access_key"
            android:value="c3ecbb62344af7bbc6271aaabbcccc"/>
	    
</application>
```

## 2.5 混淆说明

如果app进行混淆，请添加如下混淆配置，确保sdk中关键类不被混淆：

```xml
    -keep class com.sinocare.multicriteriasdk.utils.NoProguard

    -keep class * implements com.sinocare.multicriteriasdk.utils.NoProguard {*;}
```

# 3. 接口说明

## 3.1 初始化SDK、鉴权（只有鉴权通过，sdk才可以正常使用）；注意：不要反复鉴权或每次连接前都做一次鉴权，鉴权太频繁会增加鉴权失败的机率；

## 初始化SDK、鉴权分两种 如下：

```Java
     public class MyApplication extends Application {
    
        @Override
        public void onCreate() {
          super.onCreate();
          // 第一种鉴权接口和默认设置蓝牙连接间隔时间
           MulticriteriaSDKManager.initAndAuthentication(this, new AuthStatusListener() {

               @Override
               public void onAuthStatus(AuthStatus authStatus) {
  
               }
           });
        
         //第二种鉴权接口和手动设置蓝牙连接间隔时间 最低3秒 3000 = 3秒 (注意 此方法在等于大于SDK 1.0.18 才支持)
//           MulticriteriaSDKManager.initAndAuthentication(3000, this, new AuthStatusListener() {
//              @Override
//               public void onAuthStatus(AuthStatus authStatus) {
//
//               }
//           });
    }
}
```


###  3.1.1 AnthStatus鉴权状态码说明：

code | 说明 
--- | --- 
10000 | SDK鉴权成功
10001 | accessKey 不正确
10002 | 包名不正确
10003 | 签名不正确
10004 | 服务器或网络异常，服务器无法正常响应，具体错误查看msg
500 | 接口返回服务器异常
401 | AccessKey配置不正确


## 3.2 连接与断开连接，数据获取
###  3.2.1 连接与数据获取
目前仪器测试完，数据直接会通过SDK回传。在回传前可以设置蓝牙是否开启扫描，开启扫描就是扫描到了蓝牙再去连接，如果不开启扫描，就是直接连接设备，跳过扫面。

```Java

/**
 * 第一种 默认就是开启扫描设备
 */
    MulticriteriaSDKManager.startConnect(snDevices,new SnCallBack(){
         @Override
         public void onDataComing(SNDevice device,DeviceDetectionData data){
            //设备数据回调，解析见后面数据结构，实时测量数据与历史测量数据均在此处回调；
         }

         @Override
         public void onDetectionStateChange(SNDevice device,DeviceDetectionState state){
             //设备数据状态：时间同步成功、历史数据获取成功、清除成功等等
         }

         @Override
         public void onDeviceStateChange(SNDevice device,BoothDeviceConnectState state){
             //连接连接状态 目前只回调连接成功与断开连接
         }
    });

        /**
         * 第二种  isScanningBluetooth  ture 扫描  false不扫描   (注意 此方法在等于大于SDK 1.0.18 才支持)
         */
//        MulticriteriaSDKManager.startConnect(snDevices,isScanningBluetooth,new SnCallBack(){
//          @Override
//          public void onDataComing(SNDevice device,DeviceDetectionData data){
//             //设备数据回调，解析见后面数据结构，实时测量数据与历史测量数据均在此处回调；
//          }
//
//          @Override
//          public void onDetectionStateChange(SNDevice device,DeviceDetectionState state){
//              //设备数据状态：时间同步成功、历史数据获取成功、清除成功等等
//          }
//
//          @Override
//          public void onDeviceStateChange(SNDevice device,BoothDeviceConnectState state){
//           //连接连接状态 目前只回调连接成功与断开连接
//        
//          }
//        });
```
   连接状态 BoothDeviceConnectState
```java
    public class BoothDeviceConnectState implements Parcelable {
    /**
     * 连接已经断开
     */
    public static final int DEVICE_STATE_DISCONNECTED = 0;
    /**
     * 连接成功
     */
    public static final int DEVICE_STATE_CONNECTED = 2;
}
  
```

###  3.2.2 断开连接

```java
   MulticriteriaSDKManager.disConectDevice(snDevices);
```

退出App

```java
     MulticriteriaSDKManager.finishAll();
```


## 3.3数据结构 DeviceDetectionData

###  3.3.1 血糖，血酮，血尿酸测量结果统一采用此类封装，相关设备：安稳+, EA-12，金准+，金准+air ug_11，真睿二代  

```java
public class SnDataEaka extends BaseDetectionData {

    /******************************************************
     *  String glucose;血糖值
     *  String uaResult;血尿酸
     *  String ketResult;血酮
     *  Unit glucoseUnit;血糖值单位，国内仪器为mmmol/L; 国际版的仪器存在mg/dl
     *  Unit uaResultUnit;血尿酸单位
     *  Unit ketResultUnit;血酮单位
     *  boolean Lo; 是否低于最低值,低于最低值，仪器上面一般显示低值L
     *  boolean HI;是否高于最高值，高于最高值，仪器上面一般显示高低H
     *  String testTime;测量时间，2020-10
     *  DataSources mDataSources; 数据来源（实时数据、历史数据）
     *  SampleType sampleType; 检测样本（对于金稳+Air的测量结果会存在血糖质控液样本、血糖样本）
     */ 
       **********************************************/
}
```
###  3.3.2  血脂测量结果统一用此类封装；相关设备：卡迪克，SLX-120（掌越）

```java
public class SnDataCardioCbek extends BaseDetectionData {

    /***************************zzg***************************
     *  String testTime;检测时间
     *  String valueChol; CHOL 总胆固醇
     *  String valueTrig: TRIG 甘油三酯
     *  String valueHdlChol;  HDL CHOL 高密度
     *  String valueCalcLdl; CALC LDL  低密度
     *  String valueTcHdl; TC/HDL   总胆与高密比值
     *	String ldlcHdlc;  ldl/hdl  低密与高密比值
     *  String nonHdlc；            非高密度脂蛋白胆固醇
     *  String glucose;血糖
     *  Unit cardioCbekUnit; 单位
     ***************************zzg*******************/
}

```

###  3.3.3  血压结果类；相关设备：三诺蓝牙血压计（誉康、安诺心）， 脉搏波医用血压计RBP_9000，脉搏波BP-88B（臂式ble版），脉搏波RBP-9804（座式）

```java
public class SnDataBp extends BaseDetectionData {

    /***************************zzg***************************
     * int bloodMeasureLow :  舒张压
     * int bloodMeasureHigh :  收缩压
     * int checkHeartRate :  心率
     * Unit unit：统一单位类，血压对应的单位值是 "88"，描述是"mmHg"
     */
    
     ***************************zzg*******************/
}
```

###  3.3.4 糖化血红蛋白结果类； 相关设备：相关设备PCH-100

```java
public class SnDataPch extends BaseDetectionData {

/**
 * String testResult; 糖化结果，单位是 %
 * Boolean isLo;   是否低于最低值，低于最低值时，设备无法给出测量值，会显示低值标识
 * Boolean isHI;  是否高于最高值，高于最高值时，设备无法给出测量值，会显示高值标识
 * Unit unit;     糖化单位描述 %
 */
}
```

###  3.3.5  身份证信息类；相关设备：华大互联网HD-100

``` java
public class SnDataIdCard extends BaseDetectionData{

    /**
     * String name;   	 姓名
     * int sex;      	 性别 1：男，2：女
     * String idCard; 	 身份证号
     * String address;	 地址
     * int age;       	 年龄
     * String birthday;  出生日期；格式 yyyy-MM-dd
     */
    }
```

###  3.3.6  尿14项结果类；相关设备：优利特URIT-31，恩普生半自动尿液分析仪ui，ui-10c,

```java
public class SnDataUrit extends BaseDetectionData {

/****
 * String leu;	 白细胞
 * String ket;    酮体
 * String nit;  	 亚硝酸盐
 * String uro;	 尿胆原
 * String bil; 	 胆红素
 * String pro;	 蛋白质
 * String glu;  	 葡萄糖
 * String sg;	 尿比重
 * String bld; 	 隐血
 * String ph; 	 酸碱度
 * String vc; 	 维生素 C
 * String cr;	 肌酐
 * String ca; 	尿钙
 * String ma; 微白蛋白
 * String response; 16进制字符串，命令执行后，设备的返回确认指令，比如：指令执行成功或失败；
 */
}
```

###  3.3.7  尿生化（微量白蛋白、肌酐、ACR）；相关设备：三诺全自动生化分析仪PABA-100

```java
public class SnDataACR extends BaseDetectionData {

    /**
     * 数据类型： 目前只处理了测量类型的数据，没处理质控数据
     * 尿微：0x00 0x01;
     * 尿微质控液：0x00 0x02 （未处理）
     * 肌酐：0x00 0x03       
     * 肌酐质控液：0x00 0x04 （未处理）
     * ACR：0x00 0x05
     * 质控结论：0x00 0x06
     * String type;
     * String uint; 此字段暂未使用
     * String time; 此字段暂未使用
     * String device; 此字段暂未使用
     * String malb; 尿微量白蛋白
     * String ucr;  尿肌酐
     * String acr; 微量白蛋白/尿肌酐
     * boolean malbLow; 尿微量白蛋白是否低于测量范围
     * boolean malbHi; 尿微量白蛋白是否高于测量范围
     * boolean ucrLow; 尿肌酐是否低于测量范围
     * boolean ucrbHi; 尿微量白蛋白是否高于测量范围
     * boolean acrbLow; ACR是否低于测量范围
     * boolean acrbHi;	ACR是否高于测量范围
     */
}

```

###  3.3.8  糖化血红蛋白指标；相关设备：手持式胶体金试纸分析仪

```java
public class SnDataAnemia extends BaseDetectionData {

    /**
     * String uint; 单位，目前是ng/ml
     * String fer; 糖化血红蛋白结果
     */
}
```

# 4 给设备发送指令，支持获取历史数据与清除历史数据；

## 4.1 SampleType 样本类型

```java
public class SampleType implements Parcelable {
    //血糖
    public static final String INDEX_1_BLOOD = "0001";
    //血糖质控液
    public static final String INDEX_2_SUGER_SIMULATED_FLUID = "0002";
    //血酮
    public static final String INDEX_3__KETONE_BLOOD = "0003";
    //血酮质控液
    public static final String INDEX_4__KETONE_SIMULATED_FLUID = "0004";
    //尿酸
    public static final String INDEX_5_URIC_ACID_BLOOD = "0005";
    //尿酸质控液
    public static final String INDEX_6_URIC_ACID_SIMULATED_FLUID = "0006";

}

```

## 4.2 获取仪器历史测量结果；注意：仪器在滴血状态和测量状态可能无法响应此指令；

```java
   
/**
 * 获取设备历史数据
 * @param snDevice
 * @param SampleType   SampleType.XXX   要获取的样本类型
 */
MulticriteriaSDKManager.getHistoryData(SNDevice snDevice,String sampleType);

```

## 4.3 清除设备历史数据；注意：仪器在滴血状态和测量状态可能无法响应此指令；

```java
   // 清除成功后，会回调至连接方法设置的回调中sendDeviceDetectionStatus(SNDevice device, DeviceDetectionData data)
/**
 * 清除设备历史数据
 * @param snDevice
 * @param SampleType   SampleType.XXX   要清除的样本类型
 */
MulticriteriaSDKManager.clearHistoryData(SNDevice snDevice,String sampleType);

/**
 * 清除所有设备历史数据  (包含血糖,质控液)部分机子有血酮的，尿酸的 一概清除 
 * @param snDevice
 */
MulticriteriaSDKManager.clearHistoryData(SNDevice snDevice);

```


# 5 设备信息说明

设备名称型号 | 设备指标 | 蓝牙名称 | 蓝牙类别 | 设备图片
---|--- | --- | --- | ---
UG-11| 血尿酸、血糖 | BDE_WEIXIN_TTM | BLE | ![UG-11](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_ug_11.png?)
EA-12 | 血尿酸、血糖 | BDE_WEIXIN_TTM | BLE | ![EA-12](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_ea_12.png?)
EA-18 | 血尿酸、血糖 | BDE_WEIXIN_TTM | BLE | ![EA-18](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_ea_18.png?)
KA-11 | 血酮、血糖 | BDE_WEIXIN_TTM | BLE | ![KA-11](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_ka_11.png?)
卡迪克 | 血脂 | CardioChek | BLE | ![CardioChek](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_cardiochek.png?)
WL-1 | 血糖 | Sinocare | BLE | ![WL-1](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_wl_1.png?)
金准+ | 血糖 | BDE_WEIXIN_TTM | BLE | ![金准+](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_gold_aq.png?)
金准+Air | 血糖 | BDE_WEIXIN_TTM | BLE | ![金准+Air](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_gold_aq.png?)
掌越SLX120 | 血脂、血糖 | SLX120 | BLE | ![SXL120](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_sxl.png?)
安稳+Air | 血糖 | BDE_WEIXIN_TTM | BLE | ![安稳+Air](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_anwen_air.png?)
三诺血压计 | 血压、脉搏 | ClinkBlood | BLE | ![三诺血压计](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_yukang.png?)
便携式全自动生化分析仪 | 尿微量白蛋白、尿肌酐、ACR | OSTRAN | 经典蓝牙 配对码 0000| ![生化分析仪](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_biochemical.png?)
糖化血红蛋白分析仪 PCH-100 | 糖化血红蛋白 | OSTRAN | 经典蓝牙 外置 配对码0000 | ![PCH-100](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_pch_100.png?)

# 6 常见问题

## 6.1 蓝牙设备上显示蓝牙已被连接，但SnCallBack没有回调连接状态，和测量结果；

首先考虑鉴权是否通过，通过``` AuthUtils.isAuthValid()```查看当时鉴权是否成功，也可以在初始化鉴权过程中监听鉴权状态回调；

## 6.2 SncallBack 会重复回调多次测量结果；

考虑多次调用了连接startConnect(List<SNDevice> snDevices, SnCallBack snCallBack)
，每次都设置了callback；由于Callback是采用添加模式，会添加到列表回调列表里面，多次设置CallBack，导致回调多次；全局只调用一次带callback的连接，其它地方再次连接时不再传入callback，这样可以保证收到数据全局只回调一次;
后续版本会考虑在连接过程中采用单一回调的模式，避免出现多次回调；


