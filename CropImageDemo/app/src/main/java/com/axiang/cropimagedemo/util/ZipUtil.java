package com.axiang.cropimagedemo.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.axiang.cropimagedemo.MainApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * zip 操作工具类
 * Created by 邱翔威 on 2022/4/11
 */
public class ZipUtil {

    public static final String TAG = "ZipUtil";
    public static final String MAGIC_FOLD = "Magics";
    public static final String MAGIC_NAME = "assetsmagics";

    /**
     * zip 解压路径
     */
    public static String getUnzipFile() {
        File result = FileUtil.createFolders(MAGIC_FOLD, MAGIC_NAME);
        if (!result.exists()) {
            result.mkdir();
        }
        return result.getAbsolutePath();
    }

    /**
     * 解压 zip
     */
    public static void unzip(@NonNull String zipFileName, OnUnzipFinishListener listener) {
        String unzipPath = getUnzipFile();  // zip 解压保存路径
        Log.d(TAG, "unzipPath = " + unzipPath);
        unzipFileForAssets(zipFileName, unzipPath, listener);
    }

    /**
     * 解压缩功能
     * 将 zipFile 文件解压到 folderPath 目录下
     */
    public static void unzipFileForAssets(@NonNull String zipFileName,
                                          @NonNull String folderPath,
                                          OnUnzipFinishListener listener) {
        boolean result = true;
        String successPath = null;
        InputStream is = null;
        ZipInputStream zis = null;
        FileOutputStream fos = null;

        try {
            is = MainApplication.getContext().getAssets().open(zipFileName);    // 打开压缩文件
            zis = new ZipInputStream(is);

            // 读取一个进入点
            ZipEntry zipEntry = zis.getNextEntry();
            // 图片较大，使用 1M Buffer
            byte[] buffer = new byte[1024 * 1024];
            // 解压时字节计数
            int count;
            // 解压文件
            File file;
            // 如果进入点为空说明已经遍历完压缩包中的所有文件和目录
            while (zipEntry != null) {
                if (zipEntry.isDirectory()) {   // 如果是一个目录
                    if (successPath == null) {  // zip文件的格式：最外层的目录就对应文件名
                        successPath = FileUtil.addSeparator(folderPath) + zipEntry.getName();
                    }
                    file = new File(FileUtil.addSeparator(folderPath) + zipEntry.getName());
                    // 文件不存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                } else {    // 如果是文件
                    file = new File(FileUtil.addSeparator(folderPath) + zipEntry.getName());
                    // 文件不存在
                    if (!file.exists()) {
                        file.createNewFile();
                        fos = new FileOutputStream(file);
                        while ((count = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, count);
                        }
                        fos.close();
                    }
                }
                zipEntry = zis.getNextEntry();
            }

        } catch (IOException e) {
            Log.e(TAG, "unzipFileForAssets 方法抛出了异常：" + e);
            result = false;
        } finally {
            result &= FileUtil.closeSafety(is);
            result &= FileUtil.closeSafety(zis);
            result &= FileUtil.closeSafety(fos);
        }

        if (listener != null) {
            if (result) {   // 解压成功
                Log.d(TAG, "unzip success, outputPath = " + successPath);
                listener.onSuccess(successPath);
            } else {
                listener.onFailed();
            }
        }
    }


    public interface OnUnzipFinishListener {

        void onSuccess(String successPath);

        void onFailed();
    }
}
