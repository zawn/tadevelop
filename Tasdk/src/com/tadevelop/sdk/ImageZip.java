package com.tadevelop.sdk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.util.Log;

/**
 * 图片压缩处理
 * 
 * @author ZhangZhenli
 * 
 */
public class ImageZip {
    private static final boolean DEBUG = true;
    private static final String TAG = "ImageZip";

    // 允许的最大图片大小(单位KB)
    private static final int MAX_SIZE = 400;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 800;
    private static final int quality = 80;

    /***
     * 判断使否进行图片像素压缩处理
     * 
     * @param imagePath
     * @return
     */
    public static boolean isNeedZipPixel(String imagePath) {
        boolean flag = false;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        Log.i(TAG, "BitMapHelper.getBitmapByQuality()-------处理之前---width:" + options.outWidth
                + ",height:" + options.outHeight);
        // 期望的图片大小
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        // 计算图片缩放比例
        final int minSideLength = Math.min(width, height);
        final int minSideOutLength = Math.min(options.outWidth, options.outHeight);
        if (minSideOutLength > minSideLength) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    /***
     * 判断使否进行图片质量处理
     * 
     * @param imagePath
     * @return
     * @throws FileNotFoundException
     */
    public static boolean isNeedZipQuality(String imagePath) throws FileNotFoundException {
        File file = new File(imagePath);
        double sizeKB = FileSize.setFile(file).getSizeKB();
        Log.v(TAG, imagePath + ", size=" + FileSize.setFile(file).getSize());
        if (sizeKB > MAX_SIZE) {
            return true;
        }
        return false;
    }

    /***
     * 判断使否进行图片质量处理
     * 
     * @param imagePath
     * @return
     * @throws FileNotFoundException
     */
    public static boolean isNeedZip(String imagePath) throws FileNotFoundException {
        if (isNeedZipQuality(imagePath) || isNeedZipPixel(imagePath)) {
            return true;
        }
        return false;
    }

    /**
     * 使用既定的规则压缩图片
     * 
     * @param imagePath
     * @return
     * @throws FileNotFoundException
     */
    public static String zipImage(String imagePath, File cacheDir) throws FileNotFoundException {
        if (isNeedZip(imagePath)) {
            Bitmap bitmap = getBitmap(imagePath);
            File file = new File(imagePath);
            FileOutputStream fileOutputStream = null;
            try {
                File distImagePath = new File(cacheDir, file.getName());
                fileOutputStream = new FileOutputStream(distImagePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
                fileOutputStream.flush();
                return distImagePath.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                    }
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
        return imagePath;
    }

    public static Bitmap getBitmap(String imagePath) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        Log.i(TAG, "BitMapHelper.getBitmapByQuality()-------处理之前---width:" + options.outWidth
                + ",height:" + options.outHeight);
        // 期望的图片大小
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        // 计算图片缩放比例
        final int minSideLength = Math.min(width, height);
        final int minSideOutLength = Math.min(options.outWidth, options.outHeight);
        Log.i(TAG, "BitMapHelper.getBitmap()-------minSideOutLength:" + minSideOutLength);
        if (minSideOutLength > minSideLength) {
            options.inSampleSize = computeSampleSize(options, minSideLength, width * height);
            options.inJustDecodeBounds = false;
            options.inInputShareable = true;
            options.inPurgeable = true;
            try {
                bitmap = BitmapFactory.decodeFile(imagePath, options);
                Log.i(TAG, "Bitmap size:" + FileSize.setSize(bitmap.getByteCount()).getSize()
                        + ", width:" + bitmap.getWidth() + ",height:" + bitmap.getHeight());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bitmap = BitmapFactory.decodeFile(imagePath);
        }
        return bitmap;
    }

    private static int computeSampleSize(BitmapFactory.Options options, int minSideLength,
            int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        if (DEBUG) Log.v(TAG, "initialSize :" + initialSize);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        if (DEBUG) Log.v(TAG, "roundedSize :" + roundedSize);
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength,
            int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h
                / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

}
