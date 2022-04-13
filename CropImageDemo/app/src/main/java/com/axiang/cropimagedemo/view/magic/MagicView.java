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

import com.axiang.cropimagedemo.editimg.magic.MagicData;
import com.axiang.cropimagedemo.util.BitmapUtil;
import com.axiang.cropimagedemo.util.PaintUtil;
import com.axiang.cropimagedemo.util.RectUtil;
import com.axiang.cropimagedemo.util.ThreadPoolUtil;
import com.axiang.cropimagedemo.widget.MagicBitmapPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 邱翔威 on 2022/4/7
 */
public class MagicView extends View {

    private static final int MATERIALS_VIEW_SIZE = 60;  // 每滑动 60 才显示一个图标
    private static final int ERASER_STOKE_WIDTH = 40;

    private Canvas mBufferCanvas;   // 绘图缓存保管 Canvas
    private Bitmap mBufferBitmap;   // 绘图缓存保管 Bitmap

    private MagicBitmapPool mBitmapPool;
    private MagicData mData;
    private List<Bitmap> mMaterialList;
    private Rect mSrcRect;
    private RectF mDstRect;

    private Paint mEraserPaint; // 橡皮擦 Paint
    private Paint mDrawBitmapPaint;

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
        mEraserPaint = PaintUtil.newDefaultPaint();
        mEraserPaint.setStyle(Paint.Style.FILL);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mDrawBitmapPaint = PaintUtil.newDefaultPaint();

        mMaterialList = new ArrayList<>();
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
                mLastX = mInitX = x;
                mLastY = mInitY = y;
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
        if (isEmptyBitmaps()) {
            return;
        }
        initRect(x, y);
    }

    /**
     * 单独提取出来初始化主要是因为 move 过程中图片突然加载出来了而此时 mDstRect 为空会导致崩溃
     */
    private void initRect(float x, float y) {
        if (mSrcRect == null) {
            if (mData.isFromZip()) {
                Bitmap bitmap = mBitmapPool.getFirstBitmap();
                mSrcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            } else {
                mSrcRect = new Rect(0, 0, mMaterialList.get(0).getWidth(), mMaterialList.get(0).getHeight());
            }
        }

        mDstRect = new RectF(mSrcRect);
        mDstRect.offsetTo(x - ((float) mSrcRect.width()) / 2,
                y - ((float) mSrcRect.height()) / 2);
    }

    private boolean isEmptyBitmaps() {
        if (mData == null) {
            return true;
        }
        return (mData.isFromZip() && mBitmapPool.isNoFillFull()) || (!mData.isFromZip() && mMaterialList.isEmpty());
    }

    private void handleMove(float x, float y) {
        if (isEmptyBitmaps()) {
            return;
        }

        float dx = x - mLastX;
        float dy = y - mLastY;
        // 每滑动 60 才显示一个图标
        if (Math.abs(dx) < MATERIALS_VIEW_SIZE && Math.abs(dy) < MATERIALS_VIEW_SIZE) {
            return;
        }

        if (mDstRect == null) {
            initRect(x, y);
        } else {
            mDstRect.offset(dx, dy);
        }

        // 对图标进行随机略微的缩放和旋转操作
        float scale = (float) ((((Math.random() + 1) * 2) + 8f) / 10); // 缩放区间：[0.8-1.2)
        float rotate = (float) (Math.random() * 360);   // 旋转区间：[0-360)
        operateMaterial(scale, rotate);

        mLastX = x;
        mLastY = y;
    }

    /**
     * 取一个随机素材图标，进行指定的缩放和旋转
     */
    private void operateMaterial(float scale, float rotate) {
        Bitmap bitmap = getRandomBitmap();

        RectF operateRect = new RectF(mDstRect);
        RectUtil.scaleRect(operateRect, scale, scale);

        mBufferCanvas.save();
        mBufferCanvas.rotate(rotate, operateRect.centerX(), operateRect.centerY());
        mBufferCanvas.drawBitmap(bitmap, mSrcRect, operateRect, mDrawBitmapPaint);
        mBufferCanvas.restore();
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
        if (mData.isFromZip()) {
            return mBitmapPool.getBitmap();
        }

        int position = (int) (Math.random() * mMaterialList.size());
        return mMaterialList.get(position);
    }

    private void handleUp(float x, float y) {
        if (isEmptyBitmaps()) {
            return;
        }

        if (isHappenMove(x, y)) {   // 发生了滑动
            return;
        }

        if (mDstRect == null) {
            initRect(x, y);
        }

        float rotate = (float) (Math.random() * 360);   // 旋转区间：[0-360)
        operateMaterial(2f, rotate);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBufferBitmap != null && !mBufferBitmap.isRecycled()) {
            canvas.drawBitmap(mBufferBitmap, 0f, 0f, mDrawBitmapPaint);
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
        recyclerMaterials();
        mData = null;
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
        recyclerAllBitMaps();
        generateBufferBitmap();
    }

    public Bitmap getBufferBitmap() {
        return mBufferBitmap;
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

    public void setMaterials(MagicData data, int mainBitmapWidth, int mainBitmapHeight) {
        // 所选图标合集没变化
        if (mData == data) {
            return;
        }
        mData = data;

        recyclerMaterials();
        generateMaterials(mainBitmapWidth, mainBitmapHeight);
    }

    /**
     * 清空素材 Bitmap 合集
     */
    private void recyclerMaterials() {
        if (mBitmapPool != null) {
            mBitmapPool.reset();
        }

        if (!mMaterialList.isEmpty()) {
            for (Bitmap materialBitmap : mMaterialList) {
                recycleBitmap(materialBitmap);
            }
        }
        mMaterialList.clear();

        mSrcRect = null;
        mDstRect = null;
    }

    /**
     * 生成素材 Bitmap 合集
     */
    private void generateMaterials(int mainBitmapWidth, int mainBitmapHeight) {
        if (mData.isFromZip()) { // 来自 zip 压缩包
            List<MagicData.FrameMeta> frameMetaList = mData.getFrameMetaList();
            if (mBitmapPool == null) {
                mBitmapPool = new MagicBitmapPool(frameMetaList, mainBitmapWidth, mainBitmapHeight);
            } else {
                mBitmapPool.reset(frameMetaList, mainBitmapWidth, mainBitmapHeight);
            }
        } else {
            ThreadPoolUtil.execute(() -> {
                List<String> assetsMagicList = mData.getAssetsMagicList();
                for (String s : assetsMagicList) {
                    mMaterialList.add(BitmapUtil.getBitmapFromAssetsFile(getContext(), s));
                }
            });
        }
    }
}
