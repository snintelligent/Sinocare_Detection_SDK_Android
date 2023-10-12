
### 注意：此版本为新版SDK精简版 2.0.x，如需使用旧版SDK 1.2.x,请前往[Gitee地址](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/tree/sinocare_ble_1.2.x/) 或者[Github地址](https://github.com/snintelligent/Sinocare_Detection_SDK_Android/tree/sinocare_ble_1.2.x)
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
## 2.3 SDK导包区别
### 2.3.1 在App 模块 build.gradle中配置 (注意此SDK必须要求项目支持Androidx)

```powershell
  implementation 'com.sinocare.androidx_lib:multicriteriasdk:2.1.0_lite'
```
### 2.3.2 在App 模块 build.gradle中配置 (如果要使用android support) 此sdk后续不再维护
```powershell
  implementation 'com.sinocare.android_lib:multicriteriasdk:2.0.9_lite'
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
sdk access key配置，示例代码如下，在application标签下配置meta-data, key值sino_minute_access_key，value为申请的access key

``` xml
<application ...>

        <meta-data android:name="sino_minute_access_key"
            android:value="xxxxxxxxxxxxxxx"/>
	    
</application>
```

## 2.4 动态权限申请

如果targetSdkVersion 小于23，不需要6.0权限处理。如果是targetSdkVersion 大于等于23，需要6.0权限处理，则需要在启获取权限后，再开始连接

```java
           //申请权限
      RxPermissions rxPermissions = new RxPermissions(this);
         if(isAndroid12()){
              rxPermissions.request(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
              .subscribe(granted -> {
           
              });
         }else{
              rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
              .subscribe(granted -> {
             
              });
         }
```



## 2.5 混淆说明

如果app进行混淆，请添加如下混淆配置，确保sdk中关键类不被混淆：

```xml
    -keep class com.sinocare.multicriteriasdk.utils.NoProguard

    -keep class * implements com.sinocare.multicriteriasdk.utils.NoProguard {*;}
```

# 3. 接口说明（公共接口）


## 3.1. 初始化SDK：

```Java
 
        MulticriteriaSDKManager.init(getApplication());
   
```

## 3.2 鉴权SDK：鉴权（只有鉴权通过，sdk才可以正常使用）；注意：不要反复鉴权或每次连接前都做一次鉴权，鉴权太频繁会增加鉴权失败的机率

```Java
 
        MulticriteriaSDKManager.authentication(AuthStatusListener authStatusListener);
   
```

####   AnthStatus鉴权返回状态码说明：

code | 说明
--- | --- 
10000 | SDK鉴权成功
10001 | accessKey 不正确
10002 | 包名不正确
10003 | 签名不正确
10004 | 服务器或网络异常，服务器无法正常响应，具体错误查看msg
500 | 接口返回服务器异常
401 | AccessKey配置不正确

## 3.3 连接设备，数据获取


目前仪器测试完，数据直接会通过SDK回传。

####  SnDevices入参说明  （可见以下 #5 设备信息说明）

字段 | 说明 | 是否必填
---|--- |--- 
productCode | 设备类型|  是
dataProtocolCode | 协议类型 | 是
mac | mac地址 |  是
machineCode | 机器码 |  是
productName | 设备名字| 否
bleNamePrefix | 蓝牙前缀名字 | 否
imageUrl | 设备图片地址 | 否
isOpenProcessData | 是打开过程数据 | 否 (仅限于血氧，血压计，身高体重体脂称)
##### 示例：安稳{"bleNamePrefix":"BDE_WEIXIN_TTM","dataProtocolCode":"safe_aq_air_ble","imageUrl":"","isOpenProcessData":false,"mac":"04:7F:0E:10:57:37","machineCode":"0012","name":"安稳+Air血糖仪","productCode":"100004"}

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
 * 第二种 内部关闭扫描过程，直连设备 isScanningBluetooth 是否扫描蓝牙 (注意 开启扫描连接比直连稳定，但是速度快)
 */
   MulticriteriaSDKManager.startConnect(snDevices,boolean isScanningBluetooth，new SnCallBack(){}

```
#### 返回参数 示例 BaseDetectionData

参数名称|备注| 
---|--- |
code|  02 错误值, 04 当前测试值, 05 历史数据值, 20 序列号, B7 测量过程值, 0E 历史数据补发| 
data|  返回内容| 
msg| 描述| 

#### 返回参数 data  (result返回详细字段描述请见以下示例)(result返回错误码请见6.4)
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

#### 仪器返回格式详细请见 [文件地址](https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android/blob/master/SDK%E8%AE%BE%E5%A4%87%E8%BF%94%E5%9B%9E%E6%A0%BC%E5%BC%8F.xlsx)
```json
 示例1 血糖 type=bloodGlucose
{
  "code": "04", // 04 当前测试值 , 05 历史数据值 ,02 错误值
  "data": {
  "result": {
    "GLU": {
      "value": "1.2",
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
        "value": "200",
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
        "value": "2.72",
        "unit": "mmol/L"
      },
      "HDLC": {
        "value": "2.42",
        "unit": "mmol/L"
      },
      "LDLC": {
        "value": "----",
        "unit": "mmol/L"
      },
      "LDLCHDLC": {
        "value": "----"
      },
      "NONHDLC": {
        "result": "0.30",
        "unit": "mmol/L"
      },
      "TCHDLC": {
        "value": "1.12"
      },
      "TG": {
        "value": "5.45",
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
			  "value":"95",
              "unit":"mmHg"
		    },
			"BloodMeasureLow ":{
              "value":"64",
              "unit":"mmHg"
			},
			"P":{
              "value":"81"
			}
		},
        "testTime ":"2022-07-13 15:30:47",
        "type":"bloodPressure"
	},
	"msg": "当前测试值"
}


```


##  给设备发送指令；

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
	//其他
	public static final String INDEX_9999_Other = "9999";

}

```

## 3.4 获取仪器历史测量结果；注意：仪器在滴血状态和测量状态可能无法响应此指令；

```java

/**
 * 获取设备历史数据
 * @param snDevice
 * @param SampleType   SampleType.XXX   要获取的样本类型
 */
MulticriteriaSDKManager.getHistoryData(SNDevice snDevice,String sampleType);

```


## 3.5 设置设备时间
```java

/**
 * 设置设备时间
 * @param snDevice
 * @param millisconds   时间戳
 */
MulticriteriaSDKManager.setDeviceTime(SNDevice snDevice, long millisconds);

```

## 3.6 断开连接

```java
   MulticriteriaSDKManager.disConectDevice(snDevices);
```
## 3.7 退出App


```java
     MulticriteriaSDKManager.finishAll();
```
# 4 差异设备调用方法

## 4.1 碳系列平台 见 （目录5设备信息说明）

### 4.1.0 获取历史序号

```java
/**
 * 获取历史序号
 * @param snDevice
 * @param sampleType   样本类型见以上SampleType
 */
 
 MulticriteriaSDKManager.getHistoryOrderNumber(SNDevice snDevice, String sampleType);
```

### 4.1.1 通过序号来获取指定历史数据

```java

/**
 * 获取设备历史数据
 * @param snDevice
 * @param SampleType   SampleType.XXX   要获取的样本类型
  @param orderNumber   历史数据序号
 */
 MulticriteriaSDKManager.getHistoryData(SNDevice snDevice, String sampleType, int orderNumber);

```

### 4.1.2 关闭声音

```java

/**
 * 获取设备历史数据
 * @param snDevice
 * @param isTurnSoundOff   true 关闭，false 打开   是否关闭声音
 */
 MulticriteriaSDKManager.setVoiceSwitch(SNDevice snDevice, boolean isTurnSoundOff);

```

## 4.2 安诺心诺凡血压计

### 4.2.0 开始测量
```java

 MulticriteriaSDKManager.startMeasuring();

```

# 5 设备信息说明

设备名称型号 | 设备指标 | 蓝牙名称 | 设备类型(productCode) |协议类型(dataProtocolCode)  | 设备图片 |
---|--- |---|--- |---|--- |
（碳系列）臻准2000     | 血糖     | SN-4Y | sino_standard_ble_01 | 2000 （以项目json文件为准）| ![臻准2000](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/247f818a18cc469b94ab49937e39c93f.png)|
（碳系列）优智SC301    | 血糖     | SN-5L | sino_standard_ble_01 | 2000 （以项目json文件为准）| ![优智SC301](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/3cea1615c3a5b186ea1e4b95ec289082.png?)
（碳系列）智惠GM501    | 血糖     | SN-5J | sino_standard_ble_01 | 2000 （以项目json文件为准）| ![智惠GM501](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/b6c557d91f8e14392af0a547ba01cc98.png?)
（碳系列）UA Pro      | 尿酸     | SN-6T | sino_standard_ble_01 | 2000 （以项目json文件为准）| ![UA Pro](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220826/4d2f73b3c403c4c1a9fcbce652559d24.png?)
（碳系列）捷巧UA Plus  | 尿酸     | SN-6U |  sino_standard_ble_01 | 2000 （以项目json文件为准）| ![UA Plus](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220826/e079c849bfdf566e619fc1a6e66e35c7.png?)
（碳系列）H102 Air    | 血红蛋白  | SN-7D |  sino_standard_ble_01 | 2000 （以项目json文件为准）| ![H102 Air](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230106/d1128d393eef289ce2bcf8a7cdc22dd9.png?)
（碳系列）Cr1 Air     | 血肌酐    | SN-7R |  sino_standard_ble_01 | 2000 （以项目json文件为准）| ![Cr1 Air](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230209/3509d6712c9e23aec025146726266b25.png?)
（碳系列）UG Plus     | 血糖、尿酸 | SN-6W |  sino_standard_ble_01 | 2000 （以项目json文件为准）| ![UG Plus](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220826/d1fda7e4e311787468ee0ec04ec7cd8e.png?)
（碳系列）EA-19       | 血糖、尿酸 | SN-6X |  sino_standard_ble_01 | 2000 （以项目json文件为准）| ![EA-19](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220615/21caf0cb294192f4e3960adaf7d0c1fe.png?)
（碳系列）EA-23       | 血糖、尿酸 | SN-6Y |  sino_standard_ble_01 | 2000 （以项目json文件为准）| ![EA-23](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220615/abb5a4e5e88c9d346def18bf2fcfdc2e.png?)
（碳系列）UG-23       | 血糖、尿酸 | SN-6Z |  sino_standard_ble_01 | 2000 （以项目json文件为准）| ![UG-23](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220906/a6bc34ea2fa7cbcf87cf5068819dab6f.png?)
（碳系列）三诺优准      | 血糖、尿酸 | SN-7B |  sino_standard_ble_01 | 2002 （以项目json文件为准）| ![三诺优准](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230524/a5ae1fef5d7025886d9a1db961dec1fe.png?)
（碳系列）臻准3000     | 血糖、尿酸 | SN-7A |  sino_standard_ble_01 | 2000 （以项目json文件为准）| ![臻准3000](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230209/807e00267051d165e3e74a9390abfa86.png?)
金稳+Air     | 血糖        | JW+AIR,SN-JW+Air        |  jin_wen_air_ble      | 001B | ![金稳+Air](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/8ea47e075e34a9ac5c8c3a9a07e51676.png?)
真睿         | 血糖        | TMX2                    |  true_metrix_air_ble  | 0022 | ![真睿](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/290bd68b58e257722aaa669cb8f12bc1.png?)
UG-11 Air   | 血尿酸、血糖  | UG11 Air,BDE_WEIXIN_TTM |  ug_11_ble            | 0020 | ![UG-11 Air](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/1dbaefe3e2a6f5600f05a2e08934d04f.png?)
UG-11 Code  | 血尿酸、血糖  | BDE_WEIXIN_TTM,UG11Code |  ug_11_ble            | 0024 | ![UG-11 Code](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/97a4a32ad2c05c3518a3ae273ed0a567.png?)
EA-12       | 血尿酸、血糖  | BDE_WEIXIN_TTM          |  ea_ka_ble            | 000A | ![EA-12](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/1b231e0b9db4133ce35d0cccbbe995c9.png?)
EA-18       | 血尿酸、血糖  | BDE_WEIXIN_TTM          |  ea_ka_ble            | 000A | ![EA-18](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/81e50cbde31c03a075b3319d4c118d96.png?)
KA-11       | 血酮、血糖   | BDE_WEIXIN_TTM           |  ea_ka_ble            | 0006 | ![KA-11](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/5630169fdbedfb446fc39963405e78c4.png?)
卡迪克       | 血脂        | CardioChek               |  Cardio_Chek_ble      |      | ![卡迪克](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220908/aabcc387e60b69d2a79b1dfe22058eae.png)
WL-1        | 血糖        | Sinocare                 |  wl_one_general_ble   | 0004 | ![WL-1](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/2cdb3e72ded0af8bd9e0ae468b4cef7b.png?)
金准+        | 血糖       | BDE_WEIXIN_TTM            |  gold_aq_ble          | 0009 | ![金准+](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/eeeb6b4c14aaa0ddf4ded9564e5145eb.png?)
金准+Air     | 血糖       | BDE_WEIXIN_TTM,JZ+Air     |  jin_wen_air_ble      | 001C | ![金准+Air](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/8070a39c43015b9d71a5dbf4114fd5ac.png?)
掌越SLX120   | 血脂、血糖  | SLX120                    |  slx_120_ble          | 0018 | ![SXL120](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/0b7079bb7dd0a3dbd947f52feca71817.png?)
安稳+Air     | 血糖       | AW+AIR,BDE_WEIXIN_TTM     |  safe_aq_air_ble      | 0012 | ![安稳+Air](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/0123f469c2f3b2a4eb5af79debc5d5e3.png?)
安诺心CF523  | 体脂秤      | HeartRate                 |  sino_body_scale_ble  | CF  | ![安诺心CF523](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/5372d14b96653514d34be47f7da5325f.png?)
安诺心CF516  | 体脂秤      | BodyFat Scale1            |  sino_body_scale_ble  | CF  | ![安诺心CF516](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220831/28d0056e2795246fa0d1bb59d38167e2.png?)
安诺心CF530  | 体脂秤      | BodyFat Scale1            | sino_body_scale_ble  | CF  | ![安诺心CF530](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220831/12161fca2c18b07c66ce4e24929b2fe6.png?)
安诺心CW286  | 体重秤      | Weight                    | sino_body_scale_ble  | CE  | ![安诺心CW286](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220623/f748a9e1512178d87d46d657642cd6ca.png?)
安诺心CK793  | 厨房秤      | Kitchen Scale             |  sino_body_scale_ble  | CA  | ![安诺心CK793](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220913/224fca38394d76fb77dd472284508344.png?)
诺凡BA-806   | 血压、脉搏  | ClinkBlood                |  one_test_BPG_ble     |     | ![诺凡BA-806](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/ce2b6fc3365905c65a7f91cc66db5ee0.png?)
诺凡BA-801   | 血压、脉搏  | ClinkBlood                |  one_test_BPG_ble     |    |![诺凡BA-801](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230630/6cf280841fb360e00e9a4a7903f4cfba.png?)
诺凡BA-803   | 血压、脉搏  | ClinkBlood                |  one_test_BPG_ble     |    | ![诺凡BA-803](https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220908/cb726a94a76990281b03f95a31b14088.png?)
# 6 常见问题

## 6.1 蓝牙设备上显示蓝牙已被连接，但SnCallBack没有回调连接状态，和测量结果；

首先考虑鉴权是否通过，通过``` AuthUtils.isAuthValid()```查看当时鉴权是否成功，也可以在初始化鉴权过程中监听鉴权状态回调；

## 6.2 测量指标字段；
result | 指标名称 | result | 指标名称 |result | 指标名称 |
---|--- |---|--- |---|--- |
GLU|血糖|MALB|尿微量白蛋白|Ca|尿钙|
KET|血酮|UCr|尿微量白蛋白/尿肌酐|MA|微白蛋白|
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
T|体温|Crea| 血肌酐|

## 6.3错误码对照表,请下载到本地；

[错误码对照表](https://github.com/snintelligent/Sinocare_Detection_SDK_Android/wiki)



