package com.axiang.cropimagedemo.util;

import java.util.Collection;
import java.util.Random;

/**
 * Created by 邱翔威 on 2022/4/12
 */
public class CollectionUtil {

    public static <E> boolean isEmpty(E[] data) {
        return data == null || data.length <= 0;
    }

    public static <E> boolean isNoEmpty(E[] data) {
        return !isEmpty(data);
    }

    public static <E> boolean isEmpty(Collection<E> data) {
        return data == null || data.isEmpty();
    }

    public static <E> boolean isNoEmpty(Collection<E> data) {
        return !isEmpty(data);
    }

    public static <E> void addAll(Collection<E> srcData, Collection<E> targetData) {
        if (srcData == null || isEmpty(targetData)) {
            return;
        }
        srcData.addAll(targetData);
    }

    /**
     * 随机打乱数组
     */
    public static <E> void random(E[] array) {
        if (isEmpty(array) || array.length == 1) {
            return;
        }

        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            E temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}
