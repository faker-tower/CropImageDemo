package com.axiang.cropimagedemo.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.axiang.cropimagedemo.widget.ColorPickerDialog;

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

    public static void showColorPickerDialog(@NonNull FragmentManager fragmentManager,
                                             int red,
                                             int green,
                                             int blue,
                                             ColorPickerDialog.OnSelectListener onSelectListener) {
        ColorPickerDialog.show(fragmentManager, red, green, blue, onSelectListener);
    }

    public static void showAlertDialog(@NonNull Context context, String content,
                                       DialogInterface.OnClickListener positiveListener) {
        new AlertDialog.Builder(context)
                .setMessage(content)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (positiveListener != null) {
                        positiveListener.onClick(dialog, which);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
