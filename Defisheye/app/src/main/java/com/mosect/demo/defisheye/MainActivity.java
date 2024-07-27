package com.mosect.demo.defisheye;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Act/Main";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivImage = findViewById(R.id.iv_image);
        findViewById(R.id.btn_load).setOnClickListener(v -> {
            // 载入图片
            new Thread(() -> {
                Log.d(TAG, "loadBitmap: ");
                Bitmap bitmap = loadBitmap();
                Defisheye.Params params = new Defisheye.Params();
                Defisheye defisheye = new Defisheye(bitmap, params);
                Log.d(TAG, "convert: ");
                Bitmap finalBitmap = defisheye.convert();
                bitmap.recycle();
                Log.d(TAG, "finalBitmap: " + finalBitmap);
                runOnUiThread(() -> ivImage.setImageBitmap(finalBitmap));
            }).start();
        });
    }

    private Bitmap loadBitmap() {
        Context context = getApplicationContext();
        try (InputStream ins = context.getAssets().open("example3.jpg")) {
            return BitmapFactory.decodeStream(ins);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
