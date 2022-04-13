package com.axiang.cropimagedemo.util;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.axiang.cropimagedemo.MainApplication;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtil {

    public static final String TAG = "FileUtil";
    public static final String TIETU_FOLDER_NAME = "xiangtietu";

    /**
     * 获取存贮文件的文件夹路径
     *
     * @param directoryName 系统目录名
     * @param foldName      文件名
     */
    public static File createFolders(String directoryName, String foldName) {
        File baseDir = MainApplication.getContext().getExternalFilesDir(directoryName);
        if (baseDir == null) {
            return Environment.getExternalStorageDirectory();
        }
        File aviaryFolder = new File(baseDir, foldName);
        if (aviaryFolder.exists()) {
            return aviaryFolder;
        }
        if (aviaryFolder.isFile()) {
            aviaryFolder.delete();
        }
        if (aviaryFolder.mkdirs()) {
            return aviaryFolder;
        }
        return Environment.getExternalStorageDirectory();
    }

    public static File genEditFile() {
        return getEmptyFile("tietu" + System.currentTimeMillis() + ".png");
    }

    public static File getEmptyFile(String name) {
        File folder = createFolders(Environment.DIRECTORY_PICTURES, TIETU_FOLDER_NAME);
        if (folder != null) {
            if (folder.exists()) {
                return new File(folder, name);
            }
        }
        return null;
    }

    /**
     * 将图片文件加入到相册
     */
    public static void addPicToAlbum(final String picPath) {
        if (TextUtils.isEmpty(picPath)) {
            return;
        }

        File file = new File(picPath);
        if (!file.exists() || file.length() == 0) { // 文件若不存在，则不操作
            return;
        }

        try {
            MediaStore.Images.Media.insertImage(MainApplication.getContext().getContentResolver(),
                    file.getAbsolutePath(),
                    getFileName(picPath),
                    null);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "addPicToAlbum 方法抛出了异常：" + e);
        }

        // 通知图库更新
        MediaScannerConnection.scanFile(MainApplication.getContext(), new String[]{file.getAbsolutePath()}, null,
                (path, uri) -> {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(uri);
                    MainApplication.getContext().sendBroadcast(mediaScanIntent);
                });
    }

    /**
     * 从路径中获取名字
     */
    public static String getFileName(String path) {
        int start = path.lastIndexOf("/");
        int end = path.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return path.substring(start + 1, end);
        } else {
            return null;
        }
    }

    public static boolean closeSafety(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, "closeSafety 方法抛出了异常：" + e);
                return false;
            }
        }
        return true;
    }

    /**
     * 获取 json 文件里的内容
     */
    public static String getJsonFileContent(String jsonPath) {
        StringBuilder stringBuilder = new StringBuilder();
        File jsonFile = new File(jsonPath);
        if (!jsonFile.exists()) {
            return stringBuilder.toString();
        }

        BufferedReader bufferedReader = null;
        String line;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile)));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "loadAssetsMagicZipData 方法抛出了异常：" + e);
        } finally {
            FileUtil.closeSafety(bufferedReader);
        }

        return stringBuilder.toString();
    }


    /**
     * 判断 path 是不是 File.separator 结尾，不是就加上
     */
    public static String addSeparator(String path) {
        String result = path;
        if (path.charAt(path.length() - 1) != File.separatorChar) {
            result += File.separator;
        }
        return result;
    }
}
