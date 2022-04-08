package com.axiang.cropimagedemo.view.magic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;

import com.axiang.cropimagedemo.util.BitmapUtil;
import com.axiang.cropimagedemo.util.RectUtil;

import java.util.Arrays;

/**
 * Created by 邱翔威 on 2022/4/7
 */
public class MagicView extends View {

    private static final int MATERIALS_VIEW_SIZE = 60;  // 每滑动 60 才显示一个图标
    private static final int ERASER_STOKE_WIDTH = 40;

    private Canvas mBufferCanvas;   // 绘图缓存保管 Canvas
    private Bitmap mBufferBitmap;   // 绘图缓存保管 Bitmap

    private String[] mMaterials;
    private Bitmap[] mMaterialBitmaps;
    private Rect mSrcRect;
    private RectF mDstRect;

    private Paint mEraserPaint; // 橡皮擦 Paint
    private Paint mMaterialPaint; // 素材 Paint

    private float mInitX, mInitY, mLastX, mLastY;

    public MagicView(Context context) {
        super(context);
        init();
    }

    public MagicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MagicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.FILL);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mMaterialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaterialPaint.setDither(true);
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                result = true;
                handleDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                result = true;
                handleMove(x, y);
                break;
            case MotionEvent.ACTION_CANCEL:
                result = false;
                break;
            case MotionEvent.ACTION_UP:
                result = false;
                handleUp(x, y);
                break;
        }
        return result;
    }

    private void handleDown(float x, float y) {
        if (mMaterialBitmaps == null || mMaterialBitmaps.length <= 0) {
            return;
        }

        mDstRect = new RectF(mSrcRect);
        mDstRect.offsetTo(x - ((float) mSrcRect.width()) / 2,
                y - ((float) mSrcRect.height()) / 2);
        mLastX = mInitX = x;
        mLastY = mInitY = y;
    }

    private void handleMove(float x, float y) {
        if (mMaterialBitmaps == null || mMaterialBitmaps.length <= 0) {
            return;
        }

        float dx = x - mLastX;
        float dy = y - mLastY;
        // 每滑动 60 才显示一个图标
        if (Math.abs(dx) < MATERIALS_VIEW_SIZE && Math.abs(dy) < MATERIALS_VIEW_SIZE) {
            return;
        }

        mDstRect.offset(dx, dy);
        Bitmap bitmap = getRandomBitmap();

        // 对图标进行随机略微的缩放操作
        RectF operateRect = new RectF(mDstRect);
        float scale = (float) ((((Math.random() + 1) * 2) + 8f) / 10); // 缩放区间：[0.8-1.2)
        RectUtil.scaleRect(operateRect, scale, scale);

        // 对图标进行随机略微的旋转操作
        float rotate = (float) (Math.random() * 360);   // 旋转区间：[0-360)
        mBufferCanvas.save();
        mBufferCanvas.rotate(rotate, operateRect.centerX(), operateRect.centerY());
        mBufferCanvas.drawBitmap(bitmap, mSrcRect, operateRect, mMaterialPaint);
        mBufferCanvas.restore();

        mLastX = x;
        mLastY = y;
        invalidate();
    }

    /**
     * 是否发生了滑动
     */
    private boolean isHappenMove(float x, float y) {
        int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        return Math.abs(x - mInitX) >= touchSlop || Math.abs(y - mInitY) >= touchSlop;
    }

    /**
     * 获取一个随机图标
     */
    private Bitmap getRandomBitmap() {
        int position = (int) (Math.random() * mMaterialBitmaps.length);
        return mMaterialBitmaps[position];
    }

    private void handleUp(float x, float y) {
        if (isHappenMove(x, y)) {   // 发生了滑动
            return;
        }

        Bitmap bitmap = getRandomBitmap();
        RectUtil.scaleRect(mDstRect, 3f, 3f);
        mBufferCanvas.drawBitmap(bitmap, mSrcRect, mDstRect, mMaterialPaint);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBufferBitmap != null && !mBufferBitmap.isRecycled()) {
            canvas.drawBitmap(mBufferBitmap, 0f, 0f, null);
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
        mBufferCanvas = null;
        recycleBitmap(mBufferBitmap);
        generateBufferBitmap();
    }

    /**
     * 擦除画布
     */
    public void clearCanvas() {
        mBufferCanvas.drawRect(0, 0,
                mBufferBitmap.getWidth(), mBufferBitmap.getHeight(),
                mEraserPaint);
        invalidate();
    }

    public void setMaterials(String[] materials) {
        // 所选魔法图标没变化
        if ((Arrays.equals(mMaterials, materials))) {
            resetRect();
            return;
        }

        mMaterials = materials;

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
        resetRect();
    }

    private void resetRect() {
        if (mMaterialBitmaps == null || mMaterialBitmaps[0] == null || mMaterialBitmaps[0].isRecycled()) {
            return;
        }

        mSrcRect = new Rect(0, 0, mMaterialBitmaps[0].getWidth(), mMaterialBitmaps[0].getHeight());
    }
}
