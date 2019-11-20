package com.multicriteriasdkdemo;

import android.app.Application;
import android.content.res.Configuration;

import com.sinocare.multicriteriasdk.MulticriteriaSDKManager;

/**
 * @author zhongzhigang
 * @Description:
 * @date 2019/1/15
 */
public class MyApplication extends Application {
    public MyApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * This method is for use in emulated process environments.  It will
     * never be called on a production Android device, where processes are
     * removed by simply killing them; no user code (including this callback)
     * is executed when doing so.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
