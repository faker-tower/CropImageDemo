package com.axiang.cropimagedemo.util;

import com.google.gson.Gson;

/**
 * Gson 工具封装类
 * Created by 邱翔威 on 2022/4/11
 */
public class GsonUtil {

    /**
     * 将字符串转换为 对象
     */
    public static <T> T JsonToObject(String json, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }
}
