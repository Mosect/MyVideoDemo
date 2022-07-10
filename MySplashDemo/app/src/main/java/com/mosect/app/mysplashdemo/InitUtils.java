package com.mosect.app.mysplashdemo;

import android.content.Context;

public class InitUtils {

    /**
     * 前置初始化，必须在Application中初始化
     *
     * @param context 上下文
     */
    public static void preInit(Context context) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        try {
            Thread.sleep(1700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
