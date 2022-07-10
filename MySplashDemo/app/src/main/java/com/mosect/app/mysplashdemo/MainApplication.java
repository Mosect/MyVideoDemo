package com.mosect.app.mysplashdemo;

import android.app.Application;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化代码
        InitUtils.preInit(this);
    }
}
