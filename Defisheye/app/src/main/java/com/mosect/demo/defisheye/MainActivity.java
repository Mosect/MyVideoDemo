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
    private ImageView ivImage;
    private Bitmap currentImage = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivImage = findViewById(R.id.iv_image);
        findViewById(R.id.btn_load).setOnClickListener(v -> {
            // 载入图片
            Log.d(TAG, "loadBitmap: ");
            Bitmap bitmap = loadBitmap();
            Defisheye.Params params = new Defisheye.Params();
            params.format = Defisheye.Format.fullframe;
            params.dtype = Defisheye.Dtype.linear;
            try (Defisheye defisheye = new Defisheye(bitmap, params)) {
                Log.d(TAG, "convert: ");
                Bitmap result = defisheye.convert();
                setCurrentImage(result);
            } finally {
                bitmap.recycle();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ivImage.setImageBitmap(null);
        if (null != currentImage) {
            currentImage.recycle();
            currentImage = null;
        }
    }

    private void setCurrentImage(Bitmap currentImage) {
        Log.d(TAG, "setCurrentImage: " + currentImage);
        if (null != this.currentImage) {
            this.currentImage.recycle();
        }
        this.currentImage = currentImage;
        ivImage.setImageBitmap(currentImage);
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
