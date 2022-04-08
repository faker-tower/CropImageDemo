package com.axiang.cropimagedemo.util;

import android.graphics.Color;
import android.graphics.Paint;

public class PaintUtil {

    public static Paint newDefaultPaint() {
        Paint defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultPaint.setDither(true);
        return defaultPaint;
    }

    /**
     * 辅助线 Paint
     */
    public static Paint newHelpLinePaint() {
        Paint helpLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        helpLinePaint.setColor(Color.BLACK);
        helpLinePaint.setStyle(Paint.Style.STROKE);
        helpLinePaint.setStrokeWidth(3);
        return helpLinePaint;
    }
}
