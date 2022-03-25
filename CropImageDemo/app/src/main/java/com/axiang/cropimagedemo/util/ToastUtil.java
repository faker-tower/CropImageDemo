package com.axiang.cropimagedemo.util;

import android.widget.Toast;

import com.axiang.cropimagedemo.MainApplication;

public class ToastUtil {

    public static void showShort(String text) {
        Toast.makeText(MainApplication.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(String text) {
        Toast.makeText(MainApplication.getContext(), text, Toast.LENGTH_LONG).show();
    }
}
