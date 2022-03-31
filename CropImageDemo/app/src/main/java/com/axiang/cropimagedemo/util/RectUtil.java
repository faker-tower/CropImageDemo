package com.axiang.cropimagedemo.util;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

public class RectUtil {

    /**
     * 缩放指定矩形
     */
    public static void scaleRect(RectF rect, float scaleX, float scaleY) {
        float w = rect.width();
        float h = rect.height();

        float newW = scaleX * w;
        float newH = scaleY * h;

        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;

        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    /**
     * 矩形绕指定点旋转
     */
    public static void rotateRect(RectF rect, float center_x, float center_y, float rotateAngle) {
        float x = rect.centerX();
        float y = rect.centerY();
        float sinA = (float) Math.sin(Math.toRadians(rotateAngle));
        float cosA = (float) Math.cos(Math.toRadians(rotateAngle));
        float newX = center_x + (x - center_x) * cosA - (y - center_y) * sinA;
        float newY = center_y + (y - center_y) * cosA + (x - center_x) * sinA;

        float dx = newX - x;
        float dy = newY - y;

        rect.offset(dx, dy);
    }

    /**
     * 旋转Point点
     * <p>
     * 计算某点绕点旋转后的坐标公式
     * x1，y1：要旋转的点；x2，y2：要绕的点；A:要旋转的弧度
     * x = (x1 - x2) * cos(A) - (y1 - y2) * sin(A) + x2 ;
     * y = (x1 - x2) * sin(A) + (y1 - y2) * cos(A) + y2 ;
     */
    public static void rotatePoint(Point p, float center_x, float center_y, float rotateAngle) {
        float sinA = (float) Math.sin(Math.toRadians(rotateAngle));
        float cosA = (float) Math.cos(Math.toRadians(rotateAngle));
        // 计算新的坐标点
        float newX = center_x + (p.x - center_x) * cosA - (p.y - center_y) * sinA;
        float newY = center_y + (p.y - center_y) * cosA + (p.x - center_x) * sinA;
        p.set((int) newX, (int) newY);
    }


    /**
     * 矩形在Y轴方向上的加法操作
     */
    public static void rectAddY(final RectF srcRect, final RectF addRect, int padding) {
        if (srcRect == null || addRect == null) {
            return;
        }

        float left = srcRect.left;
        float top = srcRect.top;
        float right = srcRect.right;
        float bottom = srcRect.bottom;

        if (srcRect.width() <= addRect.width()) {
            right = left + addRect.width();
        }

        bottom += padding + addRect.height();

        srcRect.set(left, top, right, bottom);
    }

    /**
     * 矩形在Y轴方向上的加法操作
     */
    public static void rectAddY(final Rect srcRect, final Rect addRect, int padding, int charMinHeight) {
        if (srcRect == null || addRect == null) {
            return;
        }

        int left = srcRect.left;
        int top = srcRect.top;
        int right = srcRect.right;
        int bottom = srcRect.bottom;

        if (srcRect.width() <= addRect.width()) {
            right = left + addRect.width();
        }

        bottom += padding + Math.max(addRect.height(), charMinHeight);

        srcRect.set(left, top, right, bottom);
    }
}
