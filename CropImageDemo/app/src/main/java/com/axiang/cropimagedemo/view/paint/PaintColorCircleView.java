package com.axiang.cropimagedemo.view.paint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 画笔颜色预览圆圈 View
 * Created by 邱翔威 on 2022/4/1
 */
public class PaintColorCircleView extends View {

    private Paint mPaint;
    private int mStokeColor;
    private float mStokeWidth;
    private float mRadius;

    public PaintColorCircleView(Context context) {
        super(context);
        init();
    }

    public PaintColorCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaintColorCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public int getStokeColor() {
        return mStokeColor;
    }

    public void setStokeColor(int stokeColor) {
        mStokeColor = stokeColor;
        mPaint.setColor(mStokeColor);
        invalidate();
    }

    public float getStokeWidth() {
        return mStokeWidth;
    }

    public void setStokeWidth(float stokeWidth) {
        mStokeWidth = stokeWidth;
        mRadius = mStokeWidth / 2;
        mPaint.setStrokeWidth(mStokeWidth);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, mRadius, mPaint);
    }
}
