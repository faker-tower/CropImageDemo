package com.axiang.cropimagedemo.view.magic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.axiang.cropimagedemo.util.BitmapUtil;

/**
 * Created by 邱翔威 on 2022/4/7
 */
public class MagicView extends View {

    private Canvas mBufferCanvas;   // 绘图缓存保管 Canvas
    private Bitmap mBufferBitmap;   // 绘图缓存保管 Bitmap
    private Bitmap[] mMaterialBitmaps;
    private boolean mIsEraser;   // 是否是橡皮擦模式

    private Path mCurrentPath = new Path();

    private float mLastX, mLastY;

    public MagicView(Context context) {
        super(context);
    }

    public MagicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MagicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mBufferBitmap == null) {
            generateBufferBitmap();
        }
    }

    private void generateBufferBitmap() {
        mBufferBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBufferBitmap);
    }

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
                invalidate();
                break;
        }
        return result;
    }

    private void handleMove() {
        if (mIsEraser) {
            return;
        }

        RectF pathRectF = new RectF();
        mCurrentPath.computeBounds(pathRectF, true);
        float width = pathRectF.width();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsEraser) {    // 擦除整个画布
            canvas.drawColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        recyclerAllBitMaps();
        super.onDetachedFromWindow();
    }

    private void recyclerAllBitMaps() {
        mBufferCanvas = null;
        recycleBitmap(mBufferBitmap);
        if (mMaterialBitmaps != null && mMaterialBitmaps.length > 0) {
            for (Bitmap materialBitmap : mMaterialBitmaps) {
                recycleBitmap(materialBitmap);
            }
        }
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 重置画布
     */
    public void reset() {
        mIsEraser = false;
        mBufferCanvas = null;
        recycleBitmap(mBufferBitmap);
        generateBufferBitmap();
    }

    public void setEraser(boolean eraser) {
        mIsEraser = eraser;
        if (eraser) {   // 直接擦除整个画布
            invalidate();
        }
    }

    public void setMaterials(String[] materials) {
        if (mMaterialBitmaps != null && mMaterialBitmaps.length > 0) {
            for (Bitmap materialBitmap : mMaterialBitmaps) {
                if (materialBitmap != null && !materialBitmap.isRecycled()) {
                    materialBitmap.recycle();
                }
            }
        }
        mMaterialBitmaps = new Bitmap[materials.length];
        for (int i = 0; i < mMaterialBitmaps.length; i++) {
            mMaterialBitmaps[i] = BitmapUtil.getBitmapFromAssetsFile(getContext(), materials[i]);
        }
    }
}
