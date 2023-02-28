package com.multicriteriasdkdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.sinocare.multicriteriasdk.entity.SNDevice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**************************************************
 * @file_name
 * @description:
 * @author baojiji
 * created : 2022/4/21
 ********************************************/
public class DeviceInfoListActivity  extends AppCompatActivity {
    private DeviceInfoListAdapter adapter;
    private ArrayList<SNDevice> snDevices = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);
        setTitle("选择设备类型");
        initData();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        //设置LayoutManager，以LinearLayoutManager为例子进行线性布局
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        //创建适配器
        adapter = new DeviceInfoListAdapter(snDevices,this);
        //设置适配器
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new DeviceInfoListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SNDevice snDevice) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("device", snDevice);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }
    private void initData() {
        String ExpandableModeStr = readAssetsFile("deviceInfo.json", this);
        snDevices = JsonUtils.fromJsonList(ExpandableModeStr, SNDevice.class);
    }
    public String readAssetsFile(String fileName, Context context) {
        // 输入流对象
        InputStream inputStream;
        // 缓存流对象
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            // getAssets方法返回通过输入流对象
            inputStream = context.getAssets().open(fileName);
            // InputStreamReader 实现字节流到字符流的转换
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
            }
        }
        return stringBuilder.toString();
    }
}
