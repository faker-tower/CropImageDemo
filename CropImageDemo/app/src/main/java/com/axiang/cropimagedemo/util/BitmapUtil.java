package com.axiang.cropimagedemo.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {

    public static final String TAG = "BitmapUtil";

    public static Bitmap getSampledBitmap(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        final Bitmap retBit = BitmapFactory.decodeFile(filePath, options);
        final int degree = readPictureDegree(filePath);
        return degree > 0 ? rotateBitmap(degree, retBit) : retBit;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            Log.e(TAG, "readPictureDegree 方法抛出了异常：" + e);
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param degree 被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(final int degree, Bitmap bitmap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

    /**
     * 从Assert文件夹中读取位图数据
     */
    public static Bitmap getBitmapFromAssetsFile(Context context, String fileName) {
        Bitmap result = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            result = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "getBitmapFromAssetsFile 方法抛出了异常：" + e);
        }
        return result;
    }

    /**
     * 保存 Bitmap 图片到指定文件
     */
    public static boolean saveBitmap(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "saveBitmap 方法抛出了异常：" + e);
            return false;
        }
    }

    /**
     * 图片打马赛克
     */
    public static Bitmap getMosaicBitmap(Bitmap srcBitmap) {
        if (srcBitmap == null || srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0 || srcBitmap.isRecycled()) {
            return null;
        }

        int srcBitmapWidth = srcBitmap.getWidth();
        int srcBitmapHeight = srcBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(srcBitmapWidth, srcBitmapHeight, Bitmap.Config.ARGB_8888);
        int blockSize = getMosaicBlockSize(srcBitmapWidth, srcBitmapHeight);    // 获取合适的切割块尺寸
        int row = srcBitmapWidth / blockSize; // 获得列的切线
        int col = srcBitmapHeight / blockSize;    // 获得行的切线
        int[] blocks = new int[blockSize * blockSize];

        for (int i = 0; i <= row; i++) {
            for (int j = 0; j <= col; j++) {
                int length = blocks.length;
                int flag = 0;   // 是否到边界标志

                if (i == row && j == col) { // 右下顶点
                    // length 范围：[0, (blockSize - 1) * (blockSize - 1)]
                    length = (srcBitmapWidth - i * blockSize) * (srcBitmapHeight - j * blockSize);
                    if (length == 0) {
                        break;  // 边界外已经没有像素
                    }

                    srcBitmap.getPixels(blocks, 0, blockSize, i * blockSize, j * blockSize,
                            srcBitmapWidth - i * blockSize, srcBitmapHeight - j * blockSize);
                    flag = 1;
                } else if (i == row) {  // 右边界
                    // length 范围：[0, (blockSize - 1) * blockSize]
                    length = (srcBitmapWidth - i * blockSize) * blockSize;
                    if (length == 0) {
                        break;  // 边界外已经没有像素
                    }

                    srcBitmap.getPixels(blocks, 0, blockSize, i * blockSize, j * blockSize,
                            srcBitmapWidth - i * blockSize, blockSize);
                    flag = 2;
                } else if (j == col) {  // 下边界
                    // length 范围：[0, blockSize * (blockSize - 1)]
                    length = (srcBitmapHeight - j * blockSize) * blockSize;
                    if (length == 0) {
                        break;  // 边界外已经没有像素
                    }

                    srcBitmap.getPixels(blocks, 0, blockSize, i * blockSize, j * blockSize,
                            blockSize, srcBitmapHeight - j * blockSize);
                    flag = 3;
                } else {
                    srcBitmap.getPixels(blocks, 0, blockSize, i * blockSize, j * blockSize,
                            blockSize, blockSize);  // 取出像素数组
                }

                // 求块内所有颜色的平均值
                int r = 0, g = 0, b = 0, a = 0;
                for (int k = 0; k < length; k++) {
                    r += Color.red(blocks[k]);
                    g += Color.green(blocks[k]);
                    b += Color.blue(blocks[k]);
                    a += Color.alpha(blocks[k]);
                }
                int color = Color.argb(a / length, r / length, g / length, b / length);
                for (int k = 0; k < length; k++) {
                    blocks[k] = color;
                }

                // 将马赛克颜色填充新图
                if (flag == 1) {    // 右下顶点
                    resultBitmap.setPixels(blocks, 0, blockSize, i * blockSize, j * blockSize,
                            srcBitmapWidth - i * blockSize, srcBitmapHeight - j * blockSize);
                } else if (flag == 2) { // 右边界
                    resultBitmap.setPixels(blocks, 0, blockSize, i * blockSize, j * blockSize,
                            srcBitmapWidth - i * blockSize, blockSize);
                } else if (flag == 3) { // 下边界
                    resultBitmap.setPixels(blocks, 0, blockSize, i * blockSize, j * blockSize,
                            blockSize, srcBitmapHeight - j * blockSize);
                } else {
                    resultBitmap.setPixels(blocks, 0, blockSize, i * blockSize, j * blockSize,
                            blockSize, blockSize);
                }
            }
        }
        return resultBitmap;
    }

    /**
     * 获取适合的马赛克切割块尺寸
     */
    private static int getMosaicBlockSize(int bitmapWidth, int bitmapHeight) {
        int size = Math.max(bitmapWidth, bitmapHeight);
        int result = size / 60;
        return Math.min(Math.max(result, 2), 50);
    }
}
