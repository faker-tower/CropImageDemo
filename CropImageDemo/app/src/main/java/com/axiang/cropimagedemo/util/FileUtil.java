package com.axiang.cropimagedemo.util;

import android.os.Environment;

import java.io.File;

public class FileUtil {

    public static final String FOLDER_NAME = "xiangtietu";

    /**
     * 获取存贮文件的文件夹路径
     */
    public static File createFolders() {
        File baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
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
                File file = new File(folder, name);
                return file;
            }
        }
        return null;
    }
}
