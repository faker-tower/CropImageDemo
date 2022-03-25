package com.axiang.cropimagedemo.util;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.axiang.cropimagedemo.MainApplication;

import java.io.File;
import java.io.FileNotFoundException;

public class FileUtil {

    public static final String FOLDER_NAME = "xiangtietu";

    /**
     * 获取存贮文件的文件夹路径
     */
    public static File createFolders() {
        File baseDir = MainApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (baseDir == null) {
            return Environment.getExternalStorageDirectory();
        }
        File aviaryFolder = new File(baseDir, FOLDER_NAME);
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
        File folder = createFolders();
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
                    file.getAbsolutePath(), getFileName(picPath), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
}
