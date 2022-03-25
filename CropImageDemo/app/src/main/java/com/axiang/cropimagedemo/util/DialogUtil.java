package com.axiang.cropimagedemo.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.NonNull;

public class DialogUtil {

    public static Dialog newLoadingDialog(@NonNull Context context, String title, boolean canCancel) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(canCancel);
        dialog.setMessage(title);
        return dialog;
    }

    public static Dialog newSaveFileDialog(@NonNull Context context) {
        return newLoadingDialog(context, "图片保存中...", false);
    }
}
