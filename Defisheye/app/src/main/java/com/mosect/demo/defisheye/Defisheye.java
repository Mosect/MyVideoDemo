package com.mosect.demo.defisheye;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Defisheye {

    public float fov;
    public float pfov;
    public int xcenter;
    public int ycenter;
    public int radius;
    public int pad;
    public float angle;
    public Dtype dtype;
    public Format format;
    private Bitmap image;
    private int width;
    private int height;

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

        Bitmap bitmapWithPad;
        if (pad > 0) {
            int width = bitmap.getWidth() + pad * 2;
            int height = bitmap.getHeight() + pad * 2;
            bitmapWithPad = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapWithPad);
            canvas.drawBitmap(bitmap, pad, pad, null);
        } else {
            bitmapWithPad = bitmap;
        }

        int xcenter = bitmapWithPad.getWidth() / 2;
        int ycenter = bitmapWithPad.getHeight() / 2;
        int dim = Math.min(bitmapWithPad.getWidth(), bitmapWithPad.getHeight());
        int x0 = xcenter - dim / 2; // left
        int xf = xcenter + dim / 2; // right
        int y0 = ycenter - dim / 2; // top
        int yf = ycenter + dim / 2; // bottom
        this.image = Bitmap.createBitmap(bitmapWithPad,
                Math.max(0, x0), // x
                Math.max(0, y0), // y
                Math.min(xf, bitmapWithPad.getWidth()) - x0, // width
                Math.min(yf, bitmapWithPad.getHeight()) - y0 // height
        );
        if (pad > 0) {
            bitmapWithPad.recycle();
        }

        this.width = this.image.getWidth();
        this.height = this.image.getHeight();

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
        float ofocinv = 1.0f / ofoc;

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];
        // 需要一种算法从image对象中采样，填充到pixels对象里
//        for (int i = 0; i < pixels.length; i++) {
//            int color = ;
//            pixels[i] = color;
//        }
        int index = 0;
        float[] nxAndNy = new float[2];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 使用x，y去算出新的坐标点，然后从image中采样
                // 怎样使用算法输出nx和ny？
                pixelMap(x, y, ofocinv, dim, nxAndNy);
                int nx = (int) Math.max(0, Math.min(nxAndNy[0], width - 1));
                int ny = (int) Math.max(0, Math.min(nxAndNy[1], height - 1));
                int color = image.getPixel(nx, ny);
                pixels[index++] = color;
            }
        }

        // 再把pixels对象填充到result对象位图中
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 矫正算法，使用x,y计算出nx和ny，放到out参数里
     *
     * @param x       x
     * @param y       y
     * @param ofocinv ofocinv
     * @param dim     dim
     * @param out     out
     */
    private void pixelMap(int x, int y, float ofocinv, float dim, float[] out) {
        float xd = x - xcenter;
        float yd = y - ycenter;
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
            xs = (rr / rd) * xd + xcenter; // (rr[rdmask] / rd[rdmask]) * xd[rdmask] + self._xcenter
            ys = (rr / rd) * yd + ycenter; // (rr[rdmask] / rd[rdmask]) * yd[rdmask] + self._ycenter
        } else {
            xs = 0;
            ys = 0;
        }
        out[0] = xs;
        out[1] = ys;
    }

    public static class Params {
        public float fov = 180;
        public float pfov = 120;
        public int xcenter = -1;
        public int ycenter = -1;
        public int radius = 0;
        public int pad = 0;
        public float angle = 0;
        public Dtype dtype = Dtype.equalarea;
        public Format format = Format.circular;
    }

    public enum Dtype {
        linear,
        equalarea,
        orthographic,
        stereographic,

        ;
    }

    public enum Format {
        circular,
        fullframe,

        ;
    }
}
