package com.mosect.app.mysplashdemo;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initSystemUI();

        handler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSystemUI();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
            int frameCount = 0;
            ViewTreeObserver.OnDrawListener _this = this;

            @Override
            public void onDraw() {
                Log.d(TAG, "onDraw: ");
                ++frameCount;
                // 页面被绘制
                if (frameCount == 2) {
                    // 初始化
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            decorView.getViewTreeObserver().removeOnDrawListener(_this);
                            InitUtils.init(SplashActivity.this);
                            Toast.makeText(SplashActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 强制页面进行绘制
                    decorView.postInvalidate();
                }
            }
        });
    }

    private void initSystemUI() {
        View decorView = getWindow().getDecorView();
        int sui = 0;
        // 隐藏导航栏
        sui |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        // 全屏
        sui |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        // 固定布局，系统栏不会影响布局
        sui |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        // 让布局延申到状态栏
        sui |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        // 让布局延申到导航栏
        sui |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        // 沉浸式交互
//        sui |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        sui |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(sui);
    }
}
