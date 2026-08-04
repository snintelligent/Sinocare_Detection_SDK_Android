# 三诺多指标设备接入SDK开发文档

## 1. SDK简介

### 1.1 概述
Sinocare_Detection_SDK_Android 是三诺生物传感股份有限公司开发的医疗设备连接SDK，用于快速接入各类蓝牙医疗检测设备。

### 1.2 主要特性
- ✅ 支持20多款医疗检测设备同时连接
- ✅ 简化蓝牙设备接入流程，降低开发成本
- ✅ 支持BLE和经典蓝牙设备
- ✅ 提供完整的设备状态和数据回调
- ✅ 支持多种医疗指标检测（血糖、血压、血脂、尿酸等）

### 1.3 系统要求
- **最低Android版本**: Android 5.0 (API Level 21)
- **蓝牙要求**: 支持蓝牙4.0及以上，支持BLE
- **依赖库**: 必须支持AndroidX

---

## 2. 快速开始

### 2.1 接入前准备

**申请Access Key**
1. 提供您的应用包名（Package Name）
2. 提供您的KeyStore的SHA1指纹

**获取SHA1指纹命令：**
```bash
keytool -v -list -keystore your-keystore.jks
```

**注意**: 移除SHA1指纹中的冒号，例如：
- 原始: `72:D2:12:98:33:D3:12:88:E0:CB:6A:2C:77:65:F2:15:25:AE:61:26`
- 提交: `72D21298333D1288E0CB6A2C7765F21525AE6126`

### 2.2 五分钟快速集成

#### 步骤1: 配置Maven仓库
在项目根目录的 `build.gradle` 中添加：

```gradle
allprojects {
    repositories {
        maven {
            url 'https://maven.aliyun.com/repository/public'
        }
        maven {
            credentials {
                username '64afa24d86937ebe0c2e4ce8'
                password 'wiOtEg=Cml2J'
            }
            url 'https://packages.aliyun.com/maven/repository/2446577-release-KBjwhA/'
        }
    }
}
```

#### 步骤2: 添加SDK依赖
在App模块的 `build.gradle` 中添加：

```gradle
dependencies {
    implementation 'com.sinocare.android_lib:multicriteriasdk:2.3.8'
}
```

#### 步骤3: 配置Manifest权限
在 `AndroidManifest.xml` 中添加：

```xml
<!-- 蓝牙基础权限 -->
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<!-- Android 12+ 蓝牙权限 -->
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />

<!-- 定位权限 (蓝牙扫描需要) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- 网络权限 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### 步骤4: 配置Access Key
在 `AndroidManifest.xml` 的 `<application>` 标签内添加：

```xml
<application ...>
    <meta-data 
        android:name="sino_minute_access_key"
        android:value="你申请的AccessKey" />
</application>
```

#### 步骤5: 初始化SDK并鉴权
在Application的onCreate方法中：

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化并鉴权
        MulticriteriaSDKManager.initAndAuthentication(this, new AuthStatusListener() {
            @Override
            public void onAuthStatus(AuthStatus authStatus) {
              switch (code) {
                case 10000:
                  Log.e("SDK", "鉴权成功");
                  break;
                case 1080031007:
                  Log.e("SDK", "AccessKey错误");
                  break;
                case 1080031008:
                  Log.e("SDK", "包名不匹配");
                  break;
                case 1080031009:
                  Log.e("SDK", "签名不匹配");
                  break;
                default:
                  Log.e("SDK", "鉴权失败: " + msg);
              }
            }
        });
    }
}
```
##### 步骤5.1 鉴权说明

**重要提示**:
- ⚠️ 只有鉴权成功后，SDK才能正常使用
- ⚠️ 不要频繁鉴权，避免每次连接前都鉴权

**鉴权状态码：**

| 状态码 | 说明 |
|--------|------|
| 10000 | 鉴权成功 |
| 1080031007 | AccessKey不正确 |
| 1080031008 | 包名不正确 |
| 1080031009 | 签名不正确 |
| 10004 | 服务器或网络异常 |


#### 步骤6: 申请运行时权限
在Activity中申请权限：

```java
private void requestPermissions() {
    RxPermissions rxPermissions = new RxPermissions(this);
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+
        rxPermissions.request(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ).subscribe(granted -> {
            if (granted) {
                // 权限已授予，可以开始连接设备
                connectDevice();
            }
        });
    } else {
        // Android 12以下
        rxPermissions.request(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).subscribe(granted -> {
            if (granted) {
                connectDevice();
            }
        });
    }
}
```
---

## 3 混淆配置

在 `proguard-rules.pro` 中添加：

```proguard
# 三诺SDK混淆规则
-keep class com.sinocare.multicriteriasdk.utils.NoProguard
-keep class * implements com.sinocare.multicriteriasdk.utils.NoProguard {*;}
```

---


## 4. 设备连接与数据获取

### 4.1 设备对象（SNDevice） dataProtocolCode和machineCode 在本文档7目录设备列表可以找到协议类型和机器猫

**必填字段：**

| 字段 | 说明 | 示例 |
|------|------|------|
| dataProtocolCode | 协议类型 | "safe_aq_air_ble" |
| mac | 设备MAC地址 | "04:7F:0E:10:57:37" |
| machineCode | 机器码/协议版本 | "0012" |

**可选字段：**

| 字段 | 说明 |
|------|------|
| productCode | 设备类型编码 |
| productName | 设备名称 |
| bleNamePrefix | 蓝牙名称前缀 |
| imageUrl | 设备图片URL |
| isOpenProcessData | 是否开启过程数据（血氧、血压、体脂秤） |

**设备对象示例：**
```json
{
  "productCode": "100004",
  "dataProtocolCode": "safe_aq_air_ble",
  "mac": "04:7F:0E:10:57:37",
  "machineCode": "0012",
  "productName": "安稳+Air血糖仪",
  "bleNamePrefix": "BDE_WEIXIN_TTM",
  "imageUrl": "https://example.com/device.png",
  "isOpenProcessData": false
}
```

### 4.2 连接设备

**方式1: 基础连接（推荐-带扫描）**
```java
List<SNDevice> devices = new ArrayList<>();
// 添加要连接的设备
devices.add(createDevice());

MulticriteriaSDKManager.startConnect(devices, new SnCallBack() {
    @Override
    public void onDataComing(SNDevice device, BaseDetectionData data) {
        // 接收测量数据（实时数据和历史数据）
        handleData(device, data);
    }

    @Override
    public void onDetectionStateChange(SNDevice device, DeviceDetectionState state) {
        // 设备检测状态变化（时间同步、历史数据获取等）
        Log.d("SDK", "状态变化: " + state);
    }

    @Override
    public void onDeviceStateChange(SNDevice device, BoothDeviceConnectState state) {
        // 设备连接状态变化
        if (state == BoothDeviceConnectState.CONNECTED) {
            Log.d("SDK", "设备已连接");
        } else if (state == BoothDeviceConnectState.DISCONNECTED) {
            Log.d("SDK", "设备已断开");
        }
    }
});
```

**方式2: 直连模式（不扫描，速度快但稳定性略低）**
```java
boolean isScanningBluetooth = false; // 关闭扫描，直连模式
MulticriteriaSDKManager.startConnect(devices, isScanningBluetooth, new SnCallBack() {
    // ... 回调实现
});
```

**方式3: 全局回调（推荐用于多处监听）**
```java
// 先注册全局回调
MulticriteriaSDKManager.addSnCallBack(new SnCallBack() {
    // ... 实现回调
});

// 然后连接设备
MulticriteriaSDKManager.startConnect(devices);
```

### 4.3 数据回调详解

**BaseDetectionData 数据结构：**

| 字段 | 说明 |
|------|------|
| code | 数据类型码 |
| data | 具体数据内容 |
| msg | 描述信息 |

**数据类型码（code）：**

| Code | 说明 | Code | 说明 |
|------|------|------|------|
| 01 | 获取版本号 | 02 | 错误值 |
| 04 | 当前测试值（实时数据） | 05 | 历史数据值 |
| 20 | 历史数据序号 | B7 | 测量过程值 |
| 0E | 历史数据补发 | 0C | 获取时间 |
| H01 | 通信测试 | 11 | TLV数据（随机、餐前、餐后、声音关、声音小、声音中、声音大） |
| 1F | 波形数据 |  |  |

**项目类型（type）：**

| Type | 说明 | Type | 说明 |
|------|------|------|------|
| bloodGlucose | 血糖 | bloodLipids | 血脂 |
| bloodPressure | 血压 | uricAcid | 尿酸 |
| bloodKetone | 血酮 | urinalysis | 尿常规 |
| bloodOxygen | 血氧 | temperature | 体温 |
| acr | 生化 | ferritin | 铁蛋白 |
| HbA1c | 糖化血红蛋白 | Crea | 肌酐 |
| HGB | 血红蛋白 | HeightAndWeight | 身高体重体脂 |
| multipleTypes | 多类型指标 | lac | 血乳酸 |
| ecg | 心电 | ruler | 电子尺 |

### 4.4 数据解析示例

**示例1: 血糖数据**
```json
{
  "code": "04",
  "data": {
    "result": {
      "GLU": {
        "value": "5.6",
        "unit": "mmol/L"
      }
    },
    "sampleType": "血糖",
    "testTime": "2025-12-26 15:30:45",
    "type": "bloodGlucose"
  },
  "msg": "当前测试值"
}
```

**Java解析代码：**
```java
@Override
public void onDataComing(SNDevice device, BaseDetectionData data) {
    String code = data.getCode();
    String msg = data.getMsg();
    
    if ("04".equals(code)) {
        // 当前测试值
        JSONObject dataObj = new JSONObject(data.getData());
        String type = dataObj.getString("type");
        
        if ("bloodGlucose".equals(type)) {
            JSONObject result = dataObj.getJSONObject("result");
            JSONObject glu = result.getJSONObject("GLU");
            String value = glu.getString("value");
            String unit = glu.getString("unit");
            String testTime = dataObj.getString("testTime");
            
            Log.d("SDK", String.format("血糖值: %s %s, 测试时间: %s", 
                value, unit, testTime));
        }
    } else if ("02".equals(code)) {
        // 错误值
        Log.e("SDK", "设备返回错误: " + msg);
    }
}
```

**示例2: 血压数据**
```json
{
  "code": "04",
  "data": {
    "result": {
      "BloodMeasureHigh": {
        "value": "120",
        "unit": "mmHg"
      },
      "BloodMeasureLow": {
        "value": "80",
        "unit": "mmHg"
      },
      "P": {
        "value": "75"
      }
    },
    "testTime": "2025-12-26 15:35:20",
    "type": "bloodPressure"
  },
  "msg": "当前测试值"
}
```

**示例3: 血脂数据**
```json
{
  "code": "04",
  "data": {
    "result": {
      "CHOL": {"value": "4.5", "unit": "mmol/L"},
      "HDLC": {"value": "1.2", "unit": "mmol/L"},
      "LDLC": {"value": "2.8", "unit": "mmol/L"},
      "TG": {"value": "1.5", "unit": "mmol/L"},
      "TCHDLC": {"value": "3.75"},
      "LDLCHDLC": {"value": "2.33"}
    },
    "testTime": "2025-12-26 16:00:00",
    "type": "bloodLipids"
  },
  "msg": "当前测试值"
}
```

### 4.5 断开设备

```java
// 断开单个或多个设备
List<SNDevice> devicesToDisconnect = new ArrayList<>();
devicesToDisconnect.add(device);
MulticriteriaSDKManager.disConectDevice(devicesToDisconnect);
```

---

## 5. 高级功能

### 5.1 获取历史数据

```java
// 获取指定样本类型的历史数据
MulticriteriaSDKManager.getHistoryData(device, SampleType.INDEX_1_BLOOD);
```

**样本类型常量：**
```java
public class SampleType {
    public static final String INDEX_1_BLOOD = "0001";                      // 血糖
    public static final String INDEX_2_SUGER_SIMULATED_FLUID = "0002";      // 血糖质控液
    public static final String INDEX_3__KETONE_BLOOD = "0003";              // 血酮
    public static final String INDEX_4__KETONE_SIMULATED_FLUID = "0004";    // 血酮质控液
    public static final String INDEX_5_URIC_ACID_BLOOD = "0005";            // 尿酸
    public static final String INDEX_6_URIC_ACID_SIMULATED_FLUID = "0006";  // 尿酸质控液
    public static final String INDEX_9_URINE = "0009";                      // 尿液
    public static final String INDEX_10_CREATINE_BLOOD = "0010";            // 肌酐
    public static final String INDEX_11_CREATINE_SIMULATED_FLUID = "0011";  // 肌酐质控液
    public static final String INDEX_12_HGB_BLOOD = "0012";                 // 血红蛋白
    public static final String INDEX_13_HGB_SIMULATED_FLUID = "0013";       // 血红蛋白质控液
    public static final String INDEX_14_LAC_BLOOD = "0014";                 // 血乳酸
    public static final String INDEX_15_LAC_SIMULATED_FLUID = "0015";       // 血乳酸质控液
    public static final String INDEX_16_BLOOD_PRESSURE = "0016";            // 血压
    public static final String INDEX_17_CHOL_PRESSURE = "0017";             // 总胆固醇
    public static final String INDEX_18_CHOL_SIMULATED_FLUID = "0018";      // 总胆固醇质控液
    public static final String INDEX_19_TG_PRESSURE = "0019";               // 甘油三酯
    public static final String INDEX_20_TG_SIMULATED_FLUID = "0020";        // 甘油三酯质控液
    public static final String INDEX_21_GLU_UA_PRESSURE = "0021";           // 血糖+尿酸
    public static final String INDEX_22_GLU_UA_SIMULATED_FLUID = "0022";    // 血糖+尿酸质控液
    public static final String INDEX_23_GLU_KET_PRESSURE = "0023";          //血糖+血酮
    public static final String INDEX_24_GLU_KET_SIMULATED_FLUID = "0024";   //血糖+血酮质控液
    public static final String INDEX_9999_Other = "9999";                   // 其他
}
```

### 5.2 设备时间同步

```java
// 设置设备时间为当前系统时间
long currentTime = System.currentTimeMillis();
MulticriteriaSDKManager.setDeviceTime(device, currentTime);
```

### 5.3 碳系列平台特殊功能

**适用设备：**
-5.1目录 设备对象（SNDevice）machineCode 大于等于2000以上的机型

**功能1: 获取历史数据序号**（注意：如果是多功能及会返回所有的样本类型序号）
```java
MulticriteriaSDKManager.getHistoryOrderNumber(device);
```

**功能2: 通过序号获取历史数据**
```java
int orderNumber = 5; // 历史记录序号
MulticriteriaSDKManager.getHistoryData(device, SampleType.INDEX_1_BLOOD, orderNumber);
```

**功能3: 声音开关控制**
```java
// 关闭声音
MulticriteriaSDKManager.setVoiceSwitch(device, false);

// 打开声音
MulticriteriaSDKManager.setVoiceSwitch(device, true);
```

**功能4: TLV参数设置**
```java
List<TLVBean> tlvList = new ArrayList<>();
// 添加TLV参数
MulticriteriaSDKManager.setTLV(device, tlvList);
```

**功能5: 读取TLV参数**
```java
MulticriteriaSDKManager.readTLV(device);
```

### 5.4 血压计测量控制

**适用设备：** 安诺心、诺凡血压计

```java
// 开始测量
MulticriteriaSDKManager.startMeasuring(device);

// 停止测量
MulticriteriaSDKManager.stopMeasuring(device);
```


### 5.5 生命周期管理

```java
public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onPause() {
        super.onPause();
        // 暂停所有蓝牙连接
        MulticriteriaSDKManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 恢复所有蓝牙连接
        MulticriteriaSDKManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放所有资源
        MulticriteriaSDKManager.finishAll();
    }
}
```

### 5.6 日志监听

```java
// 设置日志监听器
MulticriteriaSDKManager.setLogHandler(new LogUtils.LogListener() {
    @Override
    public void onLog(String tag, String message, int level) {
        // 处理SDK日志
        Log.d("SDKLog", tag + ": " + message);
    }
});
```


### 5.7 获取错误码详情

```java
MulticriteriaSDKManager.getErrorCodeDetails(
    "E001",           // 错误码
    "bloodGlucose",   // 指标类型
    "AA:BB:CC:DD:EE:FF", // 设备MAC
    new BaseCallBack() {
        @Override
        public void onSuccess(Object result) {
            // 获取错误详情成功
        }

        @Override
        public void onFail(int code, String msg) {
            // 获取失败
        }
    }
);
```

---

## 6. 设备列表

### 6.1 全量设备列表（共87款设备）

| 序号 | 设备名称 | 指标类型 | 蓝牙名称前缀 | 协议类型 | 机器码 | 产品类型 | 设备图片 |
|------|---------|---------|------------|---------|--------|---------|---------|
| 1 | UA Pro尿酸测试仪 | 尿酸 | SN-6T | sino_standard_ble_01 | 2000 | 尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220826/4d2f73b3c403c4c1a9fcbce652559d24.png" width="80"/> |
| 2 | 三诺捷巧UA Plus尿酸测试仪 | 尿酸 | SN-6U | sino_standard_ble_01 | 2000 | 尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220826/e079c849bfdf566e619fc1a6e66e35c7.png" width="80"/> |
| 3 | UG Plus血糖尿酸测试仪 | 血糖/尿酸 | SN-6W | sino_standard_ble_01 | 2000 | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220826/d1fda7e4e311787468ee0ec04ec7cd8e.png" width="80"/> |
| 4 | EA-19血糖尿酸测试仪 | 血糖/尿酸 | SN-6X | sino_standard_ble_01 | 2000 | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220615/21caf0cb294192f4e3960adaf7d0c1fe.png" width="80"/> |
| 5 | EA-23血糖尿酸测试仪 | 血糖/尿酸 | SN-6Y | sino_standard_ble_01 | 2000 | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220615/abb5a4e5e88c9d346def18bf2fcfdc2e.png" width="80"/> |
| 6 | UG-23血糖尿酸测试仪 | 血糖/尿酸 | SN-6Z | sino_standard_ble_01 | 2000 | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220906/a6bc34ea2fa7cbcf87cf5068819dab6f.png" width="80"/> |
| 7 | 安诺心CF523智能体脂秤 | 身高/体重/体脂 | HeartRate | sino_body_scale_ble | CF | 智能秤 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/5372d14b96653514d34be47f7da5325f.png" width="80"/> |
| 8 | 三诺806隧道式血压计 | 血压 | ClinkBlood | one_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/ce2b6fc3365905c65a7f91cc66db5ee0.png" width="80"/> |
| 9 | 安诺心CW286智能体重秤 | 身高/体重 | Weight | sino_body_scale_ble | CE | 智能秤 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220623/f748a9e1512178d87d46d657642cd6ca.png" width="80"/> |
| 10 | PCH-100便携式蓝牙糖化血红蛋白分析仪 | HbA1c | BDE_WEIXIN_TTM,PCH | pch_100_ble | 0064 | 糖化血红蛋白分析仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/a2623aa2dffa49034d0e520b8e5a1d2d.png" width="80"/> |
| 11 | PCH-50便携式糖化血红蛋白分析仪 | HbA1c | PCH50 | pch_50_ble | 0029 | 糖化血红蛋白分析仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/93e407c3f9abf34387c8ebe7b1f335de.png" width="80"/> |
| 12 | Safe AQ Akso Air血糖仪 | 血糖 | - | sino_standard_ble_01 | 2000 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220906/b611cefa6dc6457d6fecb499fb9c096c.png" width="80"/> |
| 13 | 安诺心CK793BLE厨房秤 | 体重/营养 | Kitchen Scale | sino_body_scale_ble | CA | 智能秤 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220913/224fca38394d76fb77dd472284508344.png" width="80"/> |
| 14 | 安诺心CF516智能体脂秤 | 身高/体重/体脂 | BodyFat Scale1 | sino_body_scale_ble | CF | 智能秤 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220831/28d0056e2795246fa0d1bb59d38167e2.png" width="80"/> |
| 15 | 安诺心CF530BLE智能体脂秤 | 身高/体重/体脂 | BodyFat Scale1 | sino_body_scale_ble | CF | 智能秤 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220831/12161fca2c18b07c66ce4e24929b2fe6.png" width="80"/> |
| 16 | Safe AQ max Ⅱ | 血糖 | SN-6C | sino_standard_ble_01 | 2002 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230508/98bcf1c965e4f5a42c13269de58750a1.png" width="80"/> |
| 17 | H102 Air血红蛋白分析仪 | 血红蛋白 | SN-7D | sino_standard_ble_01 | 2000 | 血红蛋白分析仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230106/d1128d393eef289ce2bcf8a7cdc22dd9.png" width="80"/> |
| 18 | 安稳+Pro血糖仪 | 血糖 | SN-7T | safe_aq_air_ble | 0012 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230508/2e2472f74781e228e9e0fe677505c45f.png" width="80"/> |
| 19 | Cr1 Air肌酐测试仪 | 肌酐 | SN-7R | sino_standard_ble_01 | - | 肌酐测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230209/3509d6712c9e23aec025146726266b25.png" width="80"/> |
| 20 | 三诺健优血糖尿酸血酮仪 | 血糖/尿酸/血酮 | SN-7B | sino_standard_ble_01 | 2002 | 血糖尿酸血酮仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230524/a5ae1fef5d7025886d9a1db961dec1fe.png" width="80"/> |
| 21 | 三诺臻准3000血糖尿酸血酮仪 | 血糖/尿酸/血酮 | SN-7A | sino_standard_ble_01 | - | 血糖尿酸血酮仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230209/807e00267051d165e3e74a9390abfa86.png" width="80"/> |
| 22 | 测试产品202304132031 | 血糖 | BLT | safe_aq_air_ble | K3901 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220719/81ede22b1a5607b83f353e11e0234b7e.png" width="80"/> |
| 23 | 安稳+语音大屏SE201血糖测试仪 | 血糖 | SN-8W | sino_standard_ble_01 | 2002 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20231124/ba23aece19b98303f9706fc665a02707.png" width="80"/> |
| 24 | 安稳+语音大屏SP201血糖测试仪 | 血糖 | SN-8X | sino_standard_ble_01 | 2002 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20231124/e0deddba79d490e81b126205f4827cd2.png" width="80"/> |
| 25 | 安稳+语音大屏SZ202血糖测试仪 | 血糖 | SN-9A | sino_standard_ble_01 | 2002 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20231124/d6409760c220dd7a2ee00447179706c0.png" width="80"/> |
| 26 | 三诺852臂式血压计 | 血压 | ClinkBlood | one_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230523/f46d4f3b2213625f1e12ccdce96f53c7.png" width="80"/> |
| 27 | 三诺健准血糖尿酸血酮仪 | 血糖/尿酸/血酮 | SN-9B | sino_standard_ble_01 | 2002 | 血糖尿酸血酮仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230524/ea3c5cedd3acbe7d656edc2fbb4b87b7.png" width="80"/> |
| 28 | 三诺801臂式血压计 | 血压 | ClinkBlood | one_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20230630/6cf280841fb360e00e9a4a7903f4cfba.png" width="80"/> |
| 29 | SC402血糖仪 | 血糖 | SN-8S | sino_standard_ble_01 | 2002 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20231129/1b724336e7bb3cefc13faaa12a97a943.png" width="80"/> |
| 30 | Multi-Monitoring Meter | 血糖/尿酸/血酮 | SN-9C | sino_standard_ble_01 | 2002 | 血糖尿酸血酮仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20231124/6e14c1fac431ae0ff869696c2ffbc191.png" width="80"/> |
| 31 | BD102血糖仪 | 血糖 | SN-8K | sino_standard_ble_01 | 0011 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220719/81ede22b1a5607b83f353e11e0234b7e.png" width="80"/> |
| 32 | KA102血糖仪 | 血糖 | SN-8L | sino_standard_ble_01 | 0011 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220719/81ede22b1a5607b83f353e11e0234b7e.png" width="80"/> |
| 33 | 三诺819隧道式血压计 | 血压 | ClinkBlood | one_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20231010/c6890c0b747936e6b400f189537c5c9f.png" width="80"/> |
| 34 | 三诺822隧道式血压计 | 血压 | ClinkBlood | one_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20231010/f67bce7448b1e7eb29070dcb7b605ca5.png" width="80"/> |
| 35 | 掌优血糖尿酸血脂仪 | 血糖/尿酸/血脂 | SN-9R | sino_standard_ble_01 | - | 血糖尿酸血脂仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250103/9287da863d948b9d6df4e4ce478a0503.png" width="80"/> |
| 36 | 三诺稳护血糖尿酸血脂仪 | 血糖/尿酸/血脂 | SN-9S | sino_standard_ble_01 | - | 血糖尿酸血脂仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250103/d86a61bbea5ec43334f54188ed6a57db.png" width="80"/> |
| 37 | EA-19 PRO血糖尿酸测试仪 | 血糖/尿酸 | SN-AA | sino_standard_ble_01 | 2000 | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20240618/ea5e04e19ff12fcad8a0f43b023cf1ca.png" width="80"/> |
| 38 | SC801 Air血糖仪 | 血糖 | SN-8U | sino_standard_ble_01 | - | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20240620/194af60effdb6a5eff92c570e1c17cac.png" width="80"/> |
| 39 | GL-11 Air血糖乳酸分析仪 | 血糖/血乳酸 | SN-AJ | sino_standard_ble_01 | 2002 | 血糖乳酸分析仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250214/8c9d0e7f2fdcf95ef29f95314e6b4086.png" width="80"/> |
| 40 | GL-12 Air血糖乳酸分析仪 | 血糖/血乳酸 | SN-AK | sino_standard_ble_01 | 2002 | 血糖乳酸分析仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250214/2a61cd7ffb4cf0fff3659ebd5531ce6f.png" width="80"/> |
| 41 | KUG-12血糖尿酸血酮仪 | 血糖/尿酸/血酮 | SN-AS | sino_standard_ble_01 | 2000 | 血糖尿酸血酮仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250424/dbc7e8adc3f6bf06e9d866ab3a5be8f7.png" width="80"/> |
| 42 | 三诺BA-842臂式血压计 | 血压 | ClinkBlood | one_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250428/0b3fa8543367433fb8aea93d90914de5.png" width="80"/> |
| 43 | 安稳+语音大屏SE202血糖测试仪 | 血糖 | SN-AV | sino_standard_ble_01 | 2002 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250519/1e7c19b69ea52d86d346a6d030b4fff5.png" width="80"/> |
| 44 | YTN11血压血糖尿酸测试仪 | 血糖/尿酸/血压 | SN-9W | one_test_multi_ble | 2002 | 血糖尿酸血压仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250805/45afc33e90fd3bb52e9729b5b3e88b2f.png" width="80"/> |
| 45 | YTN12血压血糖尿酸测试仪 | 血糖/尿酸/血压 | SN-9X | one_test_multi_ble | 2002 | 血糖尿酸血压仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20250805/74ddd0be7eb6a3da5e4182bf214913f4.png" width="80"/> |
| 46 | 三诺优智SC301 Pro血糖仪 | 血糖 | SN-BC | sino_standard_ble_01 | - | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260205/29939667639b14297a5feb6d46745fe7.png" width="80"/> |
| 47 | M101 Air | 血糖/尿酸/血脂 | SN-BR | sino_standard_ble_01 | 2002 | 血糖尿酸血脂仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260401/7bc64a8682aa8837a08a9abead61455e.png" width="80"/> |
| 48 | M101 Pro Air | 血糖/尿酸/血脂 | SN-BU | sino_standard_ble_01 | 2002 | 血糖尿酸血脂仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260401/5251795f54a14231053df7e9deb5b43d.png" width="80"/> |
| 49 | YTN13血压血糖尿酸测试仪 | 血糖/尿酸/血压 | SN-9Y | one_test_multi_ble | 2002 | 血糖尿酸血压仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20251110/93294dabf61214822bd0e6c1bd41b717.png" width="80"/> |
| 50 | YTN15血压血糖尿酸测试仪 | 血糖/尿酸/血压 | SN-9Z | one_test_multi_ble | 2002 | 血糖尿酸血压仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20251110/568bb1b2d81812c5b5f66bf0d07f4c92.png" width="80"/> |
| 51 | S22Air隧道式血压计 | 血压 | SN-CE | S21_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260206/f2b52f3ac529603b7264c3c81a766ce5.jpg" width="80"/> |
| 52 | EA-26 Pro | 血糖/尿酸 | SN-BH | sino_standard_ble_01 | - | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260331/e267dfca717261a540c4421387a59679.png" width="80"/> |
| 53 | UG-26 Air | 血糖/尿酸 | SN-BJ | sino_standard_ble_01 | - | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260331/178f0b17794f242f39621faf3b5c1ecf.png" width="80"/> |
| 54 | UG-25 | 血糖/尿酸 | SN-BL | sino_standard_ble_01 | - | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260331/71c419b2c21dcaaf001e5d2aa1fdc7d4.png" width="80"/> |
| 55 | 三诺臻准UG-2 | 血糖/尿酸 | SN-BM | sino_standard_ble_01 | - | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220719/81ede22b1a5607b83f353e11e0234b7e.png" width="80"/> |
| 56 | GV-100 | 血糖 | SN-BX | sino_standard_ble_01 | - | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260310/5a6677ef98cbb50bb7e352174e5ae87a.png" width="80"/> |
| 57 | GV-200 | 血糖 | SN-BY | sino_standard_ble_01 | - | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260119/35678ae6a4aefb81794ca018a4c841c1.png" width="80"/> |
| 58 | S21Air隧道式血压计 | 血压 | SN-CD | S21_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260206/ccc2f54e9d8cd0ea5afad162aeed7c3f.jpg" width="80"/> |
| 59 | GKT101 Air | 血糖/血酮 | SN-CA | sino_standard_ble_01 | - | 血糖血酮测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260206/48a44483808bf471715a8cff6d5360bf.png" width="80"/> |
| 60 | YTN21 | 血糖/尿酸/血压/血酮/总胆 | SN-AZ | one_test_multi_ble | - | 血糖尿酸血压血酮总胆仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260318/4de9e6729d6140c35d65892e771e7bd9.png" width="80"/> |
| 61 | YTN PRO | 血糖/尿酸/血压/血酮/总胆 | SN-BB | one_test_multi_ble | - | 血糖尿酸血压血酮总胆仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260318/252bde9692e4273597c03bcb78e3c15d.png" width="80"/> |
| 62 | M101 Air | 血糖/尿酸/血脂 | SN-BS | sino_standard_ble_01 | 2002 | 血糖尿酸血脂仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20260401/d80180b649471c7aa01d359272a11663.png" width="80"/> |
| 63 | 安稳+Air血糖仪 | 血糖 | AW+AIR,BDE_WEIXIN_TTM,SN-2L | safe_aq_air_ble | 0012 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/0123f469c2f3b2a4eb5af79debc5d5e3.png" width="80"/> |
| 64 | 安稳+海外版 | 血糖 | AW+AIR,BDE_WEIXIN_TTM | safe_aq_air_ble | 0012 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220906/e7aab3be8af780ce5b404c7bd0f154ab.png" width="80"/> |
| 65 | UG-11 Air血糖尿酸测试仪 | 血糖/尿酸 | UG11 Air,BDE_WEIXIN_TTM,SN-3Q,Sinocare | ug_11_ble | 0020 | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/1dbaefe3e2a6f5600f05a2e08934d04f.png" width="80"/> |
| 66 | UG-11 Code血糖尿酸测试仪 | 血糖/尿酸 | UG11 Code,BDE_WEIXIN_TTM,UG11Code,SN-4C,Sinocare | ug_11_ble | 0024 | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/97a4a32ad2c05c3518a3ae273ed0a567.png" width="80"/> |
| 67 | 二代真睿TRUE METRIX AIR血糖仪 | 血糖 | TMX2,SN-4A | true_metrix_air_ble | 0022 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/290bd68b58e257722aaa669cb8f12bc1.png" width="80"/> |
| 68 | 金稳+Air血糖仪 | 血糖 | JW+AIR,SN-JW+Air | jin_wen_air_ble | 001B | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/8ea47e075e34a9ac5c8c3a9a07e51676.png" width="80"/> |
| 69 | 掌越SLX-120血脂血糖仪 | 血糖/血脂 | SLX120 | slx_120_ble | 0018 | 血脂血糖测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/0b7079bb7dd0a3dbd947f52feca71817.png" width="80"/> |
| 70 | 金准+Air血糖仪 | 血糖 | BDE_WEIXIN_TTM,JZ+Air | jin_wen_air_ble | 001C | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/8070a39c43015b9d71a5dbf4114fd5ac.png" width="80"/> |
| 71 | 安捷+血糖仪 | 血糖 | SN-4W | sino_standard_ble_01 | - | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220906/161a4f423f99f16120824510dc5d280a.png" width="80"/> |
| 72 | 三诺臻准2000血糖仪 | 血糖 | SN-4Y | sino_standard_ble_01 | 2000 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/247f818a18cc469b94ab49937e39c93f.png" width="80"/> |
| 73 | 三诺智惠GM501 Air血糖仪 | 血糖 | SN-5J,Sinocare | sino_standard_ble_01 | 2000 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/b6c557d91f8e14392af0a547ba01cc98.png" width="80"/> |
| 74 | 三诺优智SC301 Air血糖仪 | 血糖 | SN-5L,Sinocare | sino_standard_ble_01 | 2000 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/3cea1615c3a5b186ea1e4b95ec289082.png" width="80"/> |
| 75 | SG901 Air血糖仪 | 血糖 | SN-5M,Sinocare | sino_standard_ble_01 | 2000 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220906/56932703a131e67209e6f36b19a856c0.png" width="80"/> |
| 76 | 三诺803臂式血压计 | 血压 | ClinkBlood | one_test_BPG_ble | - | 血压计 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220908/cb726a94a76990281b03f95a31b14088.png" width="80"/> |
| 77 | UA Air尿酸测试仪 | 尿酸 | SN-5R | sino_standard_ble_01 | 2000 | 尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220831/dc986c7ef4d026363f5f402ff2154472.png" width="80"/> |
| 78 | Safe AQ UA Air Ⅰ | 尿酸 | - | sino_standard_ble_01 | 2000 | 尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220719/81ede22b1a5607b83f353e11e0234b7e.png" width="80"/> |
| 79 | Safe AQ UA Air Ⅱ | 尿酸 | - | sino_standard_ble_01 | 2000 | 尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220719/81ede22b1a5607b83f353e11e0234b7e.png" width="80"/> |
| 80 | UA Ring尿酸测试仪 | 尿酸 | SN-5N | sino_standard_ble_01 | 2000 | 尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220906/656e73272755b4190e73956fb6f8db3b.png" width="80"/> |
| 81 | 蓝牙WL-1血糖仪 | 血糖 | Sinocare,SN-1G | wl_one_general_ble | 0004 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220509/2cdb3e72ded0af8bd9e0ae468b4cef7b.png" width="80"/> |
| 82 | 蓝牙WL-2血糖仪 | 血糖 | Sinocare,SN-1H | wl_one_general_ble | 0004 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20240911/f5cb387315d62c6015ed09b7d98ee3f7.png" width="80"/> |
| 83 | EA-11血糖尿酸测试仪 | 血糖/尿酸 | - | ea_ka_ble | - | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/058f3c03979e8e84861b5f06d30f4d42.png" width="80"/> |
| 84 | EA-12血糖尿酸测试仪 | 血糖/尿酸 | - | ea_ka_ble | 000A | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/1b231e0b9db4133ce35d0cccbbe995c9.png" width="80"/> |
| 85 | KA-11血糖血酮测试仪 | 血糖/血酮 | BDE | ea_ka_ble | 0006 | 血糖血酮测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/5630169fdbedfb446fc39963405e78c4.png" width="80"/> |
| 86 | 金准+血糖仪 | 血糖 | - | gold_aq_ble | 0009 | 血糖仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/eeeb6b4c14aaa0ddf4ded9564e5145eb.png" width="80"/> |
| 87 | EA-18血糖尿酸测试仪 | 血糖/尿酸 | - | ea_ka_ble | 000A | 血糖尿酸测试仪 | <img src="https://sino-iot-prd.oss-cn-hangzhou.aliyuncs.com/upload/20220614/81e50cbde31c03a075b3319d4c118d96.png" width="80"/> |

### 6.2 设备能力矩阵

| 功能特性 | 碳系列 | 血压计 | 血氧仪 | 体脂秤 | 温度计 | 分析仪 |
|--------|--------|--------|--------|--------|--------|--------|
| **基础功能** |
| 实时测量 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 历史数据 | ✅ | ✅ | ❌ | ✅ | ❌ | ✅ |
| 时间同步 | ✅ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **高级功能** |
| 测量控制 | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| 声音开关 | 部分 | ❌ | ❌ | ❌ | ❌ | ❌ |
| 过程数据 | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ |
| TLV参数 | 部分 | ❌ | ❌ | ❌ | ❌ | ❌ |


**图例说明：**
- ✅ 完全支持
- ❌ 不支持
- 部分 仅部分设备支持

---

## 7. 常见问题

### 7.1 连接问题

**Q: 设备显示已连接但没有回调？**
A:
1. 检查是否鉴权成功：`AuthUtils.isAuthValid()`
2. 检查是否添加了回调：`addSnCallBack()`
3. 查看日志确认鉴权状态

**Q: 连接超时怎么办？**
A:
1. 确保设备在附近且电量充足
2. 检查蓝牙和定位权限是否授予
3. 尝试增加连接超时时间
4. 使用扫描模式连接（更稳定）

**Q: Android 12+ 连接失败？**
A:
确保已授予以下权限：
- BLUETOOTH_SCAN
- BLUETOOTH_CONNECT

### 7.2 数据问题

**Q: 收不到测量数据？**
A:
1. 确认设备已连接成功
2. 检查设备是否处于测量状态
3. 查看onDataComing回调是否正确实现
4. 某些设备需要发送开始测量命令

**Q: 历史数据获取失败？**
A:
1. 确保设备不在测量状态
2. 等待设备连接稳定后再获取
3. 注意不同设备协议可能不同

**Q: 数据单位不一致？**
A:
SDK返回的数据中包含unit字段，请根据unit显示数值

### 7.3 权限问题

**Q: Android 12扫描不到设备？**
A:
```xml
<!-- 在Manifest中添加 neverForLocation -->
<uses-permission 
    android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
```

**Q: 定位权限必须要吗？**
A:
- Android 12以下：必须（蓝牙扫描需要）
- Android 12及以上：如果添加neverForLocation，可不需要

### 7.4 鉴权问题

**Q: 鉴权失败1080031007？**
A: AccessKey不正确，检查：
1. meta-data配置是否正确
2. AccessKey是否过期
3. 联系技术支持重新申请

**Q: 鉴权失败1080031008？**
A: 包名不匹配，确保：
1. 应用包名与申请时一致
2. 没有使用多渠道打包改包名

**Q: 鉴权失败1080031009？**
A: 签名不匹配，确保：
1. Debug和Release使用相同签名
2. SHA1指纹与申请时一致

**Q: 多久鉴权一次？**
A:
- Token会自动保存，无需每次鉴权
- 只在Token失效时重新鉴权
- 不要频繁调用鉴权接口

### 7.5 性能优化

**Q: 如何提高连接速度？**
A:
1. 使用直连模式（关闭扫描）
2. 减少连接超时时间
3. 确保设备在近距离范围内

**Q: 如何降低功耗？**
A:
1. 不使用时及时断开设备
2. onPause时暂停连接
3. 避免频繁扫描

**Q: 多设备同时连接卡顿？**
A:
1. 建议同时连接不超过5个设备
2. 分批连接设备
3. 关闭不必要的过程数据

### 7.6 其他问题

**Q: 如何获取设备MAC地址？**
A:
通过扫描获取设备后，可从SNDevice对象中获取MAC地址

**Q: 支持模拟器吗？**
A:
不支持，必须使用真实设备测试（需要蓝牙硬件）

**Q: 如何判断设备是否在线？**
A:
监听onDeviceStateChange回调，判断连接状态

---

## 8. 分类汇总附录（指标字段对照表 Result字段）

### 血液指标 (13个)
| 字段名 | 说明 | 字段名 | 说明 | 字段名 | 说明 |
|--------|------|--------|------|--------|------|
| GLU | 血糖 | KET | 血酮 | UA | 血尿酸 |
| HbA1c | 糖化血红蛋白 | TG | 甘油三脂 | CHOL | 总胆固醇 |
| HDLC | 高密度脂蛋白 | LDLC | 低密度脂蛋白 | TCHDLC | 总胆/高密 |
| LDLCHDLC | 低密/高密比值 | NONHDLC | 非高密度脂蛋白 | HGB | 血红蛋白 |
| LAC | 血乳酸 |  |  |  |  |

### 血压相关 (3个)
| 字段名 | 说明 | 字段名 | 说明 | 字段名 | 说明 |
|--------|------|--------|------|--------|------|
| BloodMeasureHigh | 收缩压 | BloodMeasureLow | 舒张压 | P | 脉搏 |

### 血氧相关 (6个)
| 字段名 | 说明 | 字段名 | 说明 | 字段名 | 说明 |
|--------|------|--------|------|--------|------|
| SPO2 | 血氧饱和度 | PI | 血流灌注指数 | respiratoryRate | 呼吸频率 |
| plethysmogram | 脉率曲线值 | BT | 脉率曲线波谷 | status | 佩戴状态 |

### 肌酐相关 (4个)
| 字段名 | 说明 | 字段名 | 说明 | 字段名 | 说明 |
|--------|------|--------|------|--------|------|
| UCr | 尿肌酐 | Crea | 血肌酐 | ACR | 白蛋白/肌酐 |
| CR | 尿肌酐 |  |  |  |  |

### 尿液指标 (15个)
| 字段名 | 说明 | 字段名 | 说明 | 字段名 | 说明 |
|--------|------|--------|------|--------|------|
| MALB | 微量白蛋白 | WBC | 白细胞 | UKET | 尿酮体 |
| NIT | 亚硝酸盐 | URO | 尿胆原 | BIL | 胆红素 |
| PRO | 蛋白质 | UGLU | 尿糖 | SG | 比重 |
| BLD | 隐血 | pH | 酸碱度 | VC | 维生素C |
| CR | 肌酐 | Ca | 钙 | MA | 微白蛋白 |

### 肺功能指标 (8个)
| 字段名 | 说明 | 字段名 | 说明 | 字段名 | 说明 |
|--------|------|--------|------|--------|------|
| PEF | 呼气流量峰值 | FEV1 | 第一秒呼气量 | FVC | 用力肺活量 |
| MEF75 | MEF75值 | MEF50 | MEF50值 | MEF25 | MEF25值 |
| MMEF | MMEF值 | DATA500 | 500组曲线 |  |  |

### 其他指标 (11个)
| 字段名 | 说明 | 字段名 | 说明 | 字段名 | 说明 |
|--------|------|--------|------|--------|------|
| T | 体温 | barometricPressure | 气压值 | FER | 铁蛋白 |
| power | 电量 | measiureLength | 尺子 | ECG | 心电 |
| height | 身高 | weight | 体重 | weightG | 体重(克) |
| resistance | 阻抗 | reserve | 预留值 |  |  |


### 8.2 完整示例代码

**完整的Activity示例：**
```java
public class DeviceActivity extends AppCompatActivity {
    
    private SNDevice currentDevice;
    private SnCallBack snCallBack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        
        // 请求权限
        requestPermissions();
    }
    
    private void requestPermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rxPermissions.request(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ).subscribe(granted -> {
                if (granted) {
                    initCallback();
                   connectDevice();
                }
            });
        } else {
            rxPermissions.request(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).subscribe(granted -> {
                if (granted) {
                    initCallback();
                   connectDevice();
                }
            });
        }
    }
    
    private void initCallback() {
        snCallBack = new SnCallBack() {
            @Override
            public void onDataComing(SNDevice device, BaseDetectionData data) {
                runOnUiThread(() -> handleData(device, data));
            }
            
            @Override
            public void onDetectionStateChange(SNDevice device, 
                    DeviceDetectionState state) {
                Log.d("Device", "检测状态: " + state);
            }
            
            @Override
            public void onDeviceStateChange(SNDevice device, 
                    BoothDeviceConnectState state) {
                if (state == BoothDeviceConnectState.CONNECTED) {
                    runOnUiThread(() -> onDeviceConnected(device));
                } else if (state == BoothDeviceConnectState.DISCONNECTED) {
                    runOnUiThread(() -> onDeviceDisconnected(device));
                }
            }
        };
        
        MulticriteriaSDKManager.addSnCallBack(snCallBack);
    }
    
    
    private void connectDevice(SNDevice device) {
        currentDevice = device;
        List<SNDevice> devices = new ArrayList<>();
        devices.add(device);
        MulticriteriaSDKManager.startConnect(devices);
    }
    
    private void handleData(SNDevice device, BaseDetectionData data) {
        String code = data.getCode();
        
        if ("04".equals(code)) {
            // 实时数据
            showMeasurementResult(data);
        } else if ("05".equals(code)) {
            // 历史数据
            showHistoryData(data);
        } else if ("02".equals(code)) {
            // 错误
            showError(data.getMsg());
        }
    }
    
    private void showMeasurementResult(BaseDetectionData data) {
        // 解析并显示测量结果
        Toast.makeText(this, "测量完成", Toast.LENGTH_SHORT).show();
    }
    
    private void showHistoryData(BaseDetectionData data) {
        // 显示历史数据
    }
    
    private void showError(String error) {
        Toast.makeText(this, "错误: " + error, Toast.LENGTH_SHORT).show();
    }
    
    private void onDeviceConnected(SNDevice device) {
        Toast.makeText(this, "设备已连接", Toast.LENGTH_SHORT).show();
      
    }
    
    private void onDeviceDisconnected(SNDevice device) {
        Toast.makeText(this, "设备已断开", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        MulticriteriaSDKManager.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        MulticriteriaSDKManager.onResume();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentDevice != null) {
            List<SNDevice> devices = new ArrayList<>();
            devices.add(currentDevice);
            MulticriteriaSDKManager.disConectDevice(devices);
        }
        MulticriteriaSDKManager.finishAll();
    }
}
```

### 8.3 技术支持

**联系方式：**
- 开源地址（Gitee）: https://gitee.com/sinocare-iot/Sinocare_Detection_SDK_Android
- 开源地址（GitHub）: https://github.com/snintelligent/Sinocare_Detection_SDK_Android
- 技术支持：请通过开源平台提交Issue


### 8.4 最佳实践

1. **初始化时机**: 在Application的onCreate中初始化
2. **鉴权策略**: 只在应用启动时鉴权一次，Token失效时重新鉴权
3. **连接方式**: 优先使用带扫描的连接方式（更稳定）
4. **资源释放**: 及时断开不使用的设备，onDestroy时调用finishAll()
5. **错误处理**: 完善错误处理逻辑，提供友好的用户提示
6. **日志记录**: 生产环境可关闭详细日志，保留关键日志
7. **测试建议**: 使用真机测试，覆盖不同Android版本

### 8.5 注意事项

⚠️ **重要提示：**
1. SDK依赖AndroidX，不兼容Support库
2. 必须在真机上测试，模拟器不支持蓝牙
3. Debug和Release版本必须使用相同的签名文件
4. 不要频繁鉴权，会增加失败概率
5. 部分设备在测量状态下无法响应历史数据请求
6. Android 12+必须处理新的蓝牙权限
7. 建议同时连接设备数不超过5个

---

## 结语

本文档提供了三诺多指标设备接入SDK的完整使用指南。如有任何问题，请参考：

1. 本文档的常见问题章节
2. 开源仓库的Issue列表
3. 示例代码和Demo项目

祝您开发顺利！

--- 

**文档维护**: 三诺生物传感股份有限公司


