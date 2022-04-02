package com.axiang.cropimagedemo.view.paint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 涂鸦画板
 * Created by 邱翔威 on 2022/4/1
 */
public class PaintView extends View {

    public static final String TAG = "PaintView";
    private static final int ERASER_STOKE_WIDTH = 40;

    private Paint mColorPaint;  // 纯色 Paint
    private Paint mEraserPaint; // 橡皮擦 Paint
    private Paint mImagePaint;  // 图片 Paint

    private Canvas mDrawCanvas;
    private Bitmap mDrawBitmap;

    private float mLastX, mLastY;

    private boolean mIsEraser = false;  // 是否是擦除模式

    public PaintView(Context context) {
        super(context);
        init();
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaintView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorPaint.setDither(true);
        mColorPaint.setStrokeCap(Paint.Cap.ROUND);
        mColorPaint.setStrokeJoin(Paint.Join.ROUND);

        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEraserPaint.setAlpha(0);
        mColorPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setStrokeWidth(ERASER_STOKE_WIDTH);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mDrawBitmap == null) {
            generateBitmap();
        }
    }

    private void generateBitmap() {
        mDrawBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(mDrawBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawBitmap != null && !mDrawBitmap.isRecycled()) {
            canvas.drawBitmap(mDrawBitmap, 0, 0, null);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                result = true;
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                result = true;
                mDrawCanvas.drawLine(mLastX, mLastY, x, y, mIsEraser ? mEraserPaint : mColorPaint);
                mLastX = x;
                mLastY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                result = false;
                break;
        }
        return result;
    }

    @Override
    protected void onDetachedFromWindow() {
        recycleBitmap();
        super.onDetachedFromWindow();
    }

    private void recycleBitmap() {
        if (mDrawBitmap != null && !mDrawBitmap.isRecycled()) {
            mDrawBitmap.recycle();
        }
    }

    /**
     * 重置画布
     */
    public void resetBitmap() {
        recycleBitmap();
        generateBitmap();
    }

    public Bitmap getDrawBitmap() {
        return mDrawBitmap;
    }

    public void setStokeWidth(float stokeWidth) {
        mColorPaint.setStrokeWidth(stokeWidth);
    }

    public void setStokeColor(int stokeColor) {
        mColorPaint.setColor(stokeColor);
    }

    public void setEraser(boolean eraser) {
        mIsEraser = eraser;
    }
}
