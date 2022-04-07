package com.axiang.cropimagedemo.view.paint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 涂鸦画板
 * Created by 邱翔威 on 2022/4/1
 */
public class PaintView extends View {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Mode.COLOR, Mode.ERASER, Mode.IMAGE})
    public @interface Mode {
        int COLOR = 0;  // 纯色涂鸦
        int ERASER = 1; // 橡皮擦
        int IMAGE = 2;  // 图片涂鸦
    }

    public static final String TAG = "PaintView";
    private static final int ERASER_STOKE_WIDTH = 40;

    private BitmapPool mBitmapPool;
    private Canvas mBufferCanvas;   // 绘图缓存保管 Canvas
    private Bitmap mBufferBitmap;   // 绘图缓存保管 Bitmap
    private Bitmap mImageBitmap;    // 要绘制的图片，大小和底图一致
    private RectF mImageRectF;  // 图像位置矩阵，保持和底图一致
    private Bitmap mImageTempBitmap;    // 临时 Bitmap，为了绘图需要
    private Canvas mImageTempCanvas;    // 临时 Canvas，为了绘图需要

    private Paint mColorPaint;  // 纯色 Paint
    private Paint mEraserPaint; // 橡皮擦 Paint
    private Paint mImagePaint;  // 图片 Paint
    private Path mCurrentPath;  // 当前的涂鸦轨迹

    private float mLastX, mLastY;

    private int mMode;   // 当前的涂鸦模式

    public PaintView(Context context) {
        super(context);
        init(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        mBitmapPool = Glide.get(context).getBitmapPool();

        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorPaint.setDither(true);
        mColorPaint.setStyle(Paint.Style.STROKE);
        mColorPaint.setStrokeCap(Paint.Cap.ROUND);
        mColorPaint.setStrokeJoin(Paint.Join.ROUND);

        mEraserPaint = new Paint(mColorPaint);
        mEraserPaint.setStrokeWidth(ERASER_STOKE_WIDTH);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mImagePaint = new Paint(mColorPaint);
        mImagePaint.setFilterBitmap(true);

        mCurrentPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mBufferBitmap == null) {
            generateBufferBitmap();
        }
    }

    private void generateBufferBitmap() {
        mBufferBitmap = mBitmapPool.get(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBufferBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBufferBitmap != null && !mBufferBitmap.isRecycled()) {
            canvas.drawBitmap(mBufferBitmap, 0f, 0f, null);
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
                mCurrentPath.moveTo(x, y);
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                result = true;
                mCurrentPath.quadTo(mLastX, mLastY, x, y);
                handleMove();
                mLastX = x;
                mLastY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                result = false;
                mCurrentPath.reset();
                break;
        }
        return result;
    }

    /**
     * MOVE 事件中的图层处理
     */
    private void handleMove() {
        Paint paint = getPaint();
        if (mMode != Mode.IMAGE) {
            mBufferCanvas.drawPath(mCurrentPath, paint);
            if (mMode == Mode.ERASER) {
                mImageTempCanvas.drawPath(mCurrentPath, paint);
            }
            return;
        }

        paint.setXfermode(null);
        mImageTempCanvas.drawPath(mCurrentPath, paint);
        // 设置图层混合的模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect src = new Rect(0, 0, mImageBitmap.getWidth(), mImageBitmap.getHeight());
        mImageTempCanvas.drawBitmap(mImageBitmap, src, mImageRectF, paint);

        // 这里不需要用到画笔了，只是简单的图层合并
        mBufferCanvas.drawBitmap(mImageTempBitmap, 0, 0, null);
    }

    private Paint getPaint() {
        switch (mMode) {
            case Mode.ERASER:
                return mEraserPaint;
            case Mode.IMAGE:
                return mImagePaint;
            case Mode.COLOR:
            default:
                return mColorPaint;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        recyclerAllBitMaps();
        super.onDetachedFromWindow();
    }

    private void recyclerAllBitMaps() {
        mBufferCanvas = null;
        mImageTempCanvas = null;
        recycleBitmap(mBufferBitmap);
        recycleBitmap(mImageBitmap);
        recycleBitmap(mImageTempBitmap);
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            mBitmapPool.put(bitmap);
        }
    }

    /**
     * 重置画布
     */
    public void reset() {
        mMode = Mode.COLOR;
        recyclerAllBitMaps();
        generateBufferBitmap();
    }

    public Bitmap getBufferBitmap() {
        return mBufferBitmap;
    }

    public void setStokeWidth(float stokeWidth) {
        mColorPaint.setStrokeWidth(stokeWidth);
        mImagePaint.setStrokeWidth(stokeWidth);
    }

    public void setStokeColor(int stokeColor) {
        mColorPaint.setColor(stokeColor);
    }

    public void setMode(@Mode int mode) {
        mMode = mode;
    }

    public void setImageBitmap(Bitmap imageBitmap, RectF imageRectF) {
        mImageBitmap = imageBitmap;
        mImageRectF = new RectF(imageRectF);

        mImageTempBitmap = mBitmapPool.get(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mImageTempCanvas = new Canvas(mImageTempBitmap);
    }
}
