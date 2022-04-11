package com.axiang.cropimagedemo.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Gson 工具封装类
 * Created by 邱翔威 on 2022/4/11
 */
public class GsonUtil {

    public static final String TAG = "GsonUtil";

    /**
     * 将字符串转换为 对象
     */
    public static <T> T JsonToObject(String json, Class<T> type) {
        Gson gson = new Gson();
        T result = null;
        try {
            result = gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JsonToObject 方法抛出了异常：" + e);
        }
        return result;
    }
}
