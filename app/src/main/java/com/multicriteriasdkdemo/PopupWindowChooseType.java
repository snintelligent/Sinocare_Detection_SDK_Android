package com.multicriteriasdkdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
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
        for (int i = 1; i < SNDevice.SIZE + 1; i++) {
            @SuppressLint("WrongConstant") SNDevice snDevice = new SNDevice(i);
            if (!TextUtils.isEmpty(snDevice.getDeviceName())) {
                snDevices.add(snDevice);
            }
        }
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
