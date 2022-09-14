
### 注意：此版本为新版SDK 2.0.x，如需使用旧版SDK 1.2.x,请前往[Gitee地址](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/tree/sinocare_ble_1.2.x/) 或者[Github地址](https://github.com/snintelligent/Sinocare_Detection_SDK_Android/tree/sinocare_ble_1.2.x)
### 温馨提示：若您仍在使用旧版SDK，建议您更新为新版SDK，连接更方便，数据更清晰，拓展更便捷。



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
         SHA1: 72:D2:12:98:33:D3:12:88:E0:CB:6A:2C:77:65:F2:15:25:AE:61:26
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
  implementation 'com.sinocare.android_lib:multicriteriasdk:2.0.2_lite'
```

## 2.3 配置manifest

manifest的配置主要包括添加权限,代码示例如下：

```powershell
    <!--蓝牙相关权限-->
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true" /> //只能安装在有蓝牙ble设备上
    <uses-permission android:name="android.permission.BLUETOOTH" /> // 声明蓝牙权限
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" /> //允许程序发现和配对蓝牙设备
          <!--Android12 的蓝牙权限 如果您的应用与已配对的蓝牙设备通信或者获取当前手机蓝牙是否打开-->
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
    <!--Android12 的蓝牙权限 如果您的应用查找蓝牙设备（如蓝牙低功耗 (BLE) 外围设备）-->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN"
        android:usesPermissionFlags="neverForLocation"
        />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> //允许程序获取网络信息状态，如当前的网络连接是否有效
```

## 2.4 动态权限申请

如果targetSdkVersion 小于23，不需要6.0权限处理。如果是targetSdkVersion 大于等于23，需要6.0权限处理，则需要在启获取权限后，再开始连接

```java
           //申请权限
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

### 3.1.1 AnthStatus鉴权状态码说明：

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

### 3.2.1 连接与数据获取

目前仪器测试完，数据直接会通过SDK回传。在回传前可以设置蓝牙是否开启扫描，开启扫描就是扫描到了蓝牙再去连接，如果不开启扫描，就是直接连接设备，跳过扫面。

####  SnDevices参数说明

字段 | 说明 | 是否必填
---|--- |--- 
productCode | 设备类型|  是
dataProtocolCode | 协议类型 | 是
mac | mac地址 |  是
machineCode | 机器码 |  是
productName | 设备名字| 否
bleNamePrefix | 蓝牙前缀名字 | 否
imageUrl | 设备图片地址 | 否
isOpenProcessData | 是打开过程数据 | 否(个别机器有过程数据)

```Java
/**
 * 第一种 默认就是开启扫描设备
 */
    MulticriteriaSDKManager.startConnect(snDevices,new SnCallBack(){
        @Override
        public void onDataComing(SNDevice device,BaseDetectionData data){
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
//          public void onDataComing(SNDevice device,BaseDetectionData data){
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
#### 返回参数 示例 BaseDetectionData
```json
 示例1 血糖 type=bloodGlucose
{
  "code": "04", // 04 当前测试值 , 05 历史数据值 ,02 错误值
  "data": {
  "result": {
    "GLU": {
      "result": "1.2",
      "unit": "mmol/L"
    }
  },
  "sampleType": "血糖",
  "testTime": "2022-07-13 15:41:07",
  "type": "bloodGlucose"

  },
  "msg": "当前测试值"
}

示例2 尿酸 type=uricAcid
{
  "code": "04", // 04 当前测试值 , 05 历史数据值 ,02 错误值
  "data": {
    "result": {
      "UA": {
        "result": "200",
        "unit": "μmol/L"
      }
    },
    "sampleType": "血尿酸",
    "testTime": "2022-07-13 15:41:07",
    "type": "uricAcid"

  },
  "msg": "当前测试值"
}

示例3    血脂 type=bloodLipids
{
  "code": "04",
  "data": {
    "result": {
      "CHOL": {
        "result": "2.72",
        "unit": "mmol/L"
      },
      "HDLC": {
        "result": "2.42",
        "unit": "mmol/L"
      },
      "LDLC": {
        "result": "----",
        "unit": "mmol/L"
      },
      "LDLCHDLC": {
        "result": "----"
      },
      "NONHDLC": {
        "result": "0.30",
        "unit": "mmol/L"
      },
      "TCHDLC": {
        "result": "1.12"
      },
      "TG": {
        "result": "5.45",
        "unit": "mmol/L"
      }
    },
    "testTime": "2020-07-03 09:28:01",
    "type": "bloodLipids"
  },
  "msg": "当前测试值"
}


示例4 血压计 type=bloodPressure

{
	"code": "04",  
	"data": {
		"result": {
			"BloodMeasureHigh ":{
			  "result":"95",
              "unit":"mmHg"
		    },
			"BloodMeasureLow ":{
              "result":"64",
              "unit":"mmHg"
			},
			"P":{
              "result":"81"
			}
		},
        "testTime ":"2022 - 07 - 13 15: 30: 47",
        "type":"bloodPressure"
	},
	"msg": "当前测试值"
}


```

#### 返回参数 
参数名称|备注| 
---|--- |
code|  02 错误值, 04 当前测试值, 05 历史数据值, 20 序列号, B7 测量过程值, 0E 历史数据补发| 
data|  返回内容| 
msg| 描述| 

#### 返回参数 data  (result返回详细字段描述请见6.3)(result返回错误码请见6.4)
type | 指标类型说明 | 
---|--- |
bloodGlucose | 血糖| 
bloodLipids | 血脂| 
bloodPressure | 血压| 
uricAcid | 血尿酸| 
bloodKetone | 血酮| 
urinalysis | 尿常规| 
bloodOxygen | 血氧| 
temperature | 温度| 
acr | ACR| 
ferritin | 铁蛋白| 
HbA1c | 糖化血红蛋白| 
IDCard | 身份证| 



## 3.3 给设备发送指令，支持获取历史数据与清除历史数据；

SampleType 样本类型

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

### 3.3.1 获取仪器历史测量结果；注意：仪器在滴血状态和测量状态可能无法响应此指令；

```java

/**
 * 获取设备历史数据
 * @param snDevice
 * @param SampleType   SampleType.XXX   要获取的样本类型
 */
MulticriteriaSDKManager.getHistoryData(SNDevice snDevice,String sampleType);

```

### 3.3.2 清除设备历史数据；注意：仪器在滴血状态和测量状态可能无法响应此指令；

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

# 4 断开连接

```java
   MulticriteriaSDKManager.disConectDevice(snDevices);
```

退出App

```java
     MulticriteriaSDKManager.finishAll();
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
糖化血红蛋白分析仪 PCH-100 | 糖化血红蛋白 | BDE_WEIXIN_TTM | BLE | ![PCH-100](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/raw/master/deviceImages/img_device_pch_100.png?)

# 6 常见问题

## 6.1 蓝牙设备上显示蓝牙已被连接，但SnCallBack没有回调连接状态，和测量结果；

首先考虑鉴权是否通过，通过``` AuthUtils.isAuthValid()```查看当时鉴权是否成功，也可以在初始化鉴权过程中监听鉴权状态回调；

## 6.2 SncallBack 会重复回调多次测量结果；

考虑多次调用了连接startConnect(List<SNDevice> snDevices, SnCallBack snCallBack)
，每次都设置了callback；由于Callback是采用添加模式，会添加到列表回调列表里面，多次设置CallBack，导致回调多次；全局只调用一次带callback的连接，其它地方再次连接时不再传入callback，这样可以保证收到数据全局只回调一次;
后续版本会考虑在连接过程中采用单一回调的模式，避免出现多次回调；

## 6.3 测量指标字段；
result | 指标名称 | result | 指标名称 |result | 指标名称 |
---|--- |---|--- |---|--- |
GLU|血糖|MALB|尿微量白蛋白|Ca|尿钙|
KET|血酮|UCr|尿肌酐|MA|微白蛋白|
UA|血尿酸|ACR|尿微量白蛋白/尿肌酐|PEF|呼气流量峰值，仪器测量原始值|
HbA1c|糖化血红蛋白|FER|铁蛋白|FEV1|第一秒用力呼气量，仪器测量原始值|
TG|甘油三脂|WBC|尿白细胞|FVC|用力肺活量，仪器测量原始值|
CHOL|总胆固醇|UKET|尿酮体|MEF75|MEF75值|
HDLC|高密度脂蛋白胆固醇|NIT|尿亚硝酸盐|MEF50|MEF50值|
LDLC|低密度脂蛋白胆固醇|URO|尿胆原|MEF25|MEF25值|
TCHDLC|总胆/高密|BIL|尿胆红素|MMEF|MMEF值|
LDLCHDLC|低密度/高密度比值|PRO|尿蛋白质|DATA500|500组曲线数组|
NONHDLC|非高密度脂蛋白胆固醇|UGLU|尿糖|
BloodMeasureHigh|收缩压|SG|尿比重|
BloodMeasureLow|舒张压|BLD|隐血|
P|脉博|pH|酸碱度|
SPO2|血氧饱和度|VC|维生素 C|
T|体温|CR|尿肌酐|

## 6.4 错误码对照表,请下载到本地；

[错误码对照表](https://github.com/snintelligent/Sinocare_Detection_SDK_Android/wiki)

