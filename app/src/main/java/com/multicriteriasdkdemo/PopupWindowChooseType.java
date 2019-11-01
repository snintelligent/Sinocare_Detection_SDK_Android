package com.multicriteriasdkdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.sinocare.multicriteriasdk.entity.SNDevice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author zhongzhigang
 * @Description:
 * @date 2018/1/30
 */
public class PopupWindowChooseType extends PopupWindow {
    private int screenWidth;
    private int screenHeight;
    // 用于保存PopupWindow的宽度
    private int width;
    // 用于保存PopupWindow的高度
    private int height;

    View popupWindow_view;
    private OpClick opClick;
    private ListView listView;
    private DeviceAdapter snDeviceArrayAdapter;
    private ArrayList<SNDevice> snDevices = new ArrayList<>();
    Context context;

    public PopupWindowChooseType(Context context, OpClick opClick) {
        super(context);
        this.opClick = opClick;
        this.context = context;
        popupWindow_view = LayoutInflater.from(context).inflate(R.layout.pop_devices, null, false);
        listView = popupWindow_view.findViewById(R.id.list);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        initView();
        initData();
        snDeviceArrayAdapter = new DeviceAdapter(snDevices, context);
        listView.setAdapter(snDeviceArrayAdapter);
    }

    private void initData() {
        snDevices.add(new SNDevice(SNDevice.DEVICE_EA_12));
        snDevices.add(new SNDevice(SNDevice.DEVICE_KA_11));
        snDevices.add(new SNDevice(SNDevice.DEVICE_CARDIO_CBEK));
        snDevices.add(new SNDevice(SNDevice.DEVICE_PCH_100));
        snDevices.add(new SNDevice(SNDevice.DEVICE_BLOOD_PRESSURE_MBB_NO_BLE));
        snDevices.add(new SNDevice(SNDevice.DEVICE_BLOOD_PRESSURE_MBB_BLE));
        snDevices.add(new SNDevice(SNDevice.DEVICE_BLOOD_PRESSURE_YK));
        snDevices.add(new SNDevice(SNDevice.DEVICE_WL_ONE));
        snDevices.add(new SNDevice(SNDevice.DEVICE_GOLD_AQ));
        snDevices.add(new SNDevice(SNDevice.DEVICE_ID_CARD));
        snDevices.add(new SNDevice(SNDevice.DEVICE_GPRINT));
        snDevices.add(new SNDevice(SNDevice.DEVICE_TEMP));
        snDevices.add(new SNDevice(SNDevice.DEVICE_SLX));
        snDevices.add(new SNDevice(SNDevice.DEVICE_PABA));
        snDevices.add(new SNDevice(SNDevice.DEVICE_URIT));
        snDevices.add(new SNDevice(SNDevice.DEVICE_BMI));
        snDevices.add(new SNDevice(SNDevice.DEVICE_SPO));
        snDevices.add(new SNDevice(SNDevice.DEVICE_HXJ));
        snDevices.add(new SNDevice(SNDevice.DEVICE_A4_PRINTER));
        snDevices.add(new SNDevice(SNDevice.DEVICE_EMP_UI));
        snDevices.add(new SNDevice(SNDevice.DEVICE_EMP_UI_10C));
        snDevices.add(new SNDevice(SNDevice.DEVICE_ANWEN_AIR));
        snDevices.add(new SNDevice(SNDevice.DEVICE_UG_11));
        snDevices.add(new SNDevice(SNDevice.DEVICE_AILIKANG_FOND_THERMOMETER));
        snDevices.add(new SNDevice(SNDevice.DEVICE_ICARE));
        snDevices.add(new SNDevice(SNDevice.DEVICE_ZHEN_RUI));
        snDevices.add(new SNDevice(SNDevice.DEVICE_EA_18));
        snDevices.add(new SNDevice(SNDevice.DEVICE_BLOOD_PRESSURE_MBB_NO_BLE_RBP_9804));
    }

    /**
     * 强制绘制popupWindowView，并且初始化popupWindowView的尺寸
     */
    private void mandatoryDraw() {
        this.popupWindow_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        /**
         * 强制刷新后拿到PopupWindow的宽高
         */
        this.width = this.popupWindow_view.getMeasuredWidth();
        this.height = this.popupWindow_view.getMeasuredHeight();
    }

    private void initView() {
        this.setContentView(popupWindow_view);
        // 设置弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置弹出窗体可点击
        this.setTouchable(true);
        this.setFocusable(true);
        // 设置点击是否消失
        this.setOutsideTouchable(true);
        //设置弹出窗体动画效果
        this.setAnimationStyle(android.R.style.Animation_Dialog);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable background = new ColorDrawable(0x4f000000);
        //设置弹出窗体的背景
        this.setBackgroundDrawable(background);
        // 绘制
        this.mandatoryDraw();
        View layMenu = popupWindow_view.findViewById(R.id.cmd_dialog);
        layMenu.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    disMissPopup();
                }
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                opClick.goTestActivity(snDeviceArrayAdapter.getItem(position).getType());
                dismiss();
            }
        });
    }

    // date类型转换为String类型
    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) throws RuntimeException {
        return new SimpleDateFormat(formatType).format(data);
    }

    private void disMissPopup() {
        dismiss();
    }

    public interface OpClick {
        void goTestActivity(@SNDevice.SNDeviceEnum int snDeviceType);
    }

}
