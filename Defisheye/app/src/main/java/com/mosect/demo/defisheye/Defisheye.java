package com.mosect.demo.defisheye;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Defisheye implements AutoCloseable {

    private float fov;
    private float pfov;
    private int xcenter;
    private int ycenter;
    private int radius;
    private int pad;
    private float angle;
    private Dtype dtype;
    private Format format;

    private Bitmap image;
    private int width;
    private int height;

    private boolean closed = false;

    public Defisheye(Bitmap bitmap, Params params) {
        this.fov = params.fov;
        this.pfov = params.pfov;
        this.xcenter = params.xcenter;
        this.ycenter = params.ycenter;
        this.radius = params.radius;
        this.pad = params.pad;
        this.angle = params.angle;
        this.dtype = params.dtype;
        this.format = params.format;

        image = bitmap;

        if (pad > 0) {
            Bitmap bitmapWithPad = Bitmap.createBitmap(
                    bitmap.getWidth() + pad * 2,
                    bitmap.getHeight() + pad * 2,
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(bitmapWithPad);
            canvas.drawBitmap(bitmap, pad, pad, null);
            image = bitmapWithPad;
        }

        int width = image.getWidth();
        int height = image.getHeight();
//        int xcenter = width / 2;
//        int ycenter = height / 2;

        int dim = Math.min(width, height);
        int x = (width - dim) / 2;
        int y = (height - dim) / 2;
        Bitmap old = pad > 0 ? image : null;
        image = Bitmap.createBitmap(image, x, y, dim, dim);
        if (null != old) old.recycle();

        this.width = image.getWidth();
        this.height = image.getHeight();
        if (this.xcenter < 0) {
            this.xcenter = (this.width - 1) / 2;
        }
        if (this.ycenter < 0) {
            this.ycenter = (this.height - 1) / 2;
        }
    }

    public Bitmap convert() {
        float dim;
        if (format == Format.circular) {
            dim = Math.min(width, height);
        } else if (format == Format.fullframe) {
            dim = (float) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
        } else {
            throw new IllegalStateException("Unknown format: " + format);
        }
        if (radius > 0) {
            dim = radius * 2;
        }
        float ofoc = (float) (dim / (2 * Math.tan(pfov * Math.PI / 360)));
        float ofocinv = (float) (1.0 / ofoc);

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];
        int pixelsOffset = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 需要计算出矫正后的坐标点
                int xd = x - xcenter;
                int yd = y - ycenter;
                float rd = (float) Math.hypot(xd, yd);
                float phiang = (float) Math.atan(ofocinv * rd);
                float ifoc, rr;
                switch (dtype) {
                    case linear:
                        ifoc = (float) (dim * 180 / (fov * Math.PI));
                        rr = ifoc * phiang;
                        break;
                    case equalarea:
                        ifoc = (float) (dim / (2.0 * Math.sin(fov * Math.PI / 720)));
                        rr = (float) (ifoc * Math.sin(phiang / 2));
                        break;
                    case orthographic:
                        ifoc = (float) (dim / (2.0 * Math.sin(fov * Math.PI / 360)));
                        rr = (float) (ifoc * Math.sin(phiang));
                        break;
                    case stereographic:
                        ifoc = (float) (dim / (2.0 * Math.tan(fov * Math.PI / 720)));
                        rr = (float) (ifoc * Math.tan(phiang / 2));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + dtype);
                }
                float xs, ys;
                if (rd != 0) {
                    xs = (rr / rd) * xd + xcenter;
                    ys = (rr / rd) * yd + ycenter;
                } else {
                    xs = 0;
                    ys = 0;
                }

                int nx = (int) Math.max(0, Math.min(width - 1, xs));
                int ny = (int) Math.max(0, Math.min(height - 1, ys));

                // 矫正后的图片(x,y)的颜色是原本图片(nx,ny)的颜色
                // 当前点的颜色
                int color = image.getPixel(nx, ny);
                // 把新的颜色设置到最终图像里
//                result.setPixel(x, y, color);
                pixels[pixelsOffset++] = color;
            }
        }
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    @Override
    public void close() {
        if (!closed) {
            image.recycle();
            image = null;
            closed = true;
        }
    }

    public static class Params {
        public float fov = 180f;
        public float pfov = 120f;
        public int xcenter = -1;
        public int ycenter = -1;
        public int radius = 0;
        public int pad = 0;
        public float angle = 0f;
        public Dtype dtype = Dtype.equalarea;
        public Format format = Format.circular;
    }

    public enum Dtype {
        linear, equalarea, orthographic, stereographic
    }

    public enum Format {
        circular, fullframe
    }
}
