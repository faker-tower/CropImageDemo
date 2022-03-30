package com.axiang.cropimagedemo.view.crop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.util.PaintUtil;
import com.axiang.cropimagedemo.util.RectUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CropImageView extends View {

    private static final int CIRCLE_WIDTH = 46;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.IDLE, Status.MOVE, Status.SCALE})
    public @interface Status {
        int IDLE = 0;   // 静止状态，默认
        int MOVE = 1;   // 移动状态
        int SCALE = 2;   // 缩放状态
    }

    private final RectF mMainImageRect = new RectF();    // 底图位置信息

    private final RectF mCropRectF = new RectF();    // 裁剪框位置矩阵
    private Paint mHelpLinePaint;   // 辅助线 Paint
    private int mCurrentStatus = Status.IDLE;
    private final RectF mTempCropRect = new RectF(); // 临时存储裁剪框矩阵数据

    private Bitmap mCircleBitmap;   // 圆点
    private Paint mCirclePaint; // 圆点 Paint
    private final Rect mCircleRect = new Rect();    // 圆点尺寸矩阵
    private Circle mSelectedCircle = Circle.NONE;  // 当前选中的圆点

    private float mRatio = -1;  // 剪裁缩放比率, 默认任意比例
    private float mLastX, mLastY;

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        mHelpLinePaint = PaintUtil.newHelpLinePaint();
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCircleBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sticker_rotate);
        mCircleRect.set(0, 0, mCircleBitmap.getWidth(), mCircleBitmap.getHeight());
    }

    /**
     * 触摸事件处理
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);    // 是否向下传递事件标志，true为消耗
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Circle selectCircle = isSelectedCircle(x, y);
                if (selectCircle != Circle.NONE) {  // 选择圆点
                    result = true;
                    mSelectedCircle = selectCircle; // 记录选中圆点编号
                    mCurrentStatus = Status.SCALE;
                } else if (mCropRectF.contains(x, y)) {  // 选择裁剪框内部
                    result = true;
                    mCurrentStatus = Status.MOVE;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrentStatus == Status.SCALE) {
                    scaleCrop(x, y);
                } else if (mCurrentStatus == Status.MOVE) {
                    translateCrop(x - mLastX, y - mLastY);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mCurrentStatus = Status.IDLE;   // 回归空闲状态
                break;
        }

        mLastX = x;
        mLastY = y;
        return result;
    }

    /**
     * 是否选中控制点
     */
    private Circle isSelectedCircle(float x, float y) {
        for (Circle value : Circle.values()) {
            if (Circle.NONE != value && value.contains(x, y)) {
                return value;
            }
        }
        return Circle.NONE;
    }

    /**
     * 缩放裁剪框
     */
    private void scaleCrop(float x, float y) {
        mTempCropRect.set(mCropRectF);   //  临时存储裁剪框矩阵数据，以便还原
        mSelectedCircle.updateCropRectF(mCropRectF, x, y);   // 更新裁剪框位置

        if (mRatio < 0) {    // 任意缩放比
            validateCropRectF();    // // 边界条件检测
        } else {
            updateCropRectFWithRadio();   // 按照裁剪框的比例去裁剪框矩阵长宽
            validateCropRectFWithRadio();   // // 边界条件检测 按比例
        }
        invalidate();
    }

    /**
     * 边界条件检测
     */
    private void validateCropRectF() {
        if (mCropRectF.width() < CIRCLE_WIDTH * 2) { // 限制最小裁剪框宽度
            mCropRectF.left = mTempCropRect.left;
            mCropRectF.right = mTempCropRect.right;
        }
        if (mCropRectF.height() < CIRCLE_WIDTH * 2) { // 限制最小裁剪框高度
            mCropRectF.top = mTempCropRect.top;
            mCropRectF.bottom = mTempCropRect.bottom;
        }
        if (mCropRectF.left < mMainImageRect.left) {    // 不能超出左边界
            mCropRectF.left = mMainImageRect.left;
        }
        if (mCropRectF.right > mMainImageRect.right) {  // 不能超出右边界
            mCropRectF.right = mMainImageRect.right;
        }
        if (mCropRectF.top < mMainImageRect.top) {  // 不能超出上边界
            mCropRectF.top = mMainImageRect.top;
        }
        if (mCropRectF.bottom > mMainImageRect.bottom) {    // 不能超出下边界
            mCropRectF.bottom = mMainImageRect.bottom;
        }
    }

    /**
     * 按照裁剪框的比例去裁剪框矩阵长宽
     */
    private void updateCropRectFWithRadio() {
        switch (mSelectedCircle) {
            case LEFT_TOP:  // 左上点
            case RIGHT_TOP: // 右上点
                mCropRectF.top = mCropRectF.bottom - (mCropRectF.right - mCropRectF.left) / mRatio;
                break;
            case LEFT_BOTTOM:   // 左下点
            case RIGHT_BOTTOM:  // 右下点
                mCropRectF.bottom = mCropRectF.top + (mCropRectF.right - mCropRectF.left) / mRatio;
                break;
            case LEFT_CENTER_TOP:   // 左上中点
            case BOTTOM_CENTER_BOTTOM:  // // 底下中点
                float widthHalfDiff = ((mCropRectF.bottom - mCropRectF.top) * mRatio - mCropRectF.width()) / 2;
                mCropRectF.left -= widthHalfDiff;
                mCropRectF.right += widthHalfDiff;
                break;
            case LEFT_CENTER_BOTTOM:    // 左下中点
            case RIGHT_CENTER_BOTTOM:   // 右下中点
                float heightHalfDiff = ((mCropRectF.right - mCropRectF.left) / mRatio - mCropRectF.height()) / 2;
                mCropRectF.top -= heightHalfDiff;
                mCropRectF.bottom += heightHalfDiff;
                break;
        }

    }

    /**
     * 边界条件检测 按比例
     */
    private void validateCropRectFWithRadio() {
        if (mCropRectF.width() < CIRCLE_WIDTH * 2) { // 限制最小裁剪框宽度
            mCropRectF.set(mTempCropRect);
        }
        if (mCropRectF.height() < CIRCLE_WIDTH * 2) { // 限制最小裁剪框高度
            mCropRectF.set(mTempCropRect);
        }

        if (mCropRectF.left < mMainImageRect.left) {    // 不能超出左边界
            mCropRectF.right += mMainImageRect.left - mCropRectF.left;
            mCropRectF.left = mMainImageRect.left;
        }
        if (mCropRectF.right > mMainImageRect.right) {  // 不能超出右边界
            mCropRectF.left -= mCropRectF.right - mMainImageRect.right;
            mCropRectF.right = mMainImageRect.right;
        }
        if (mCropRectF.left < mMainImageRect.left || mCropRectF.right > mMainImageRect.right) { // 调整后不能超出左右边界
            mCropRectF.set(mTempCropRect);
        }

        if (mCropRectF.top < mMainImageRect.top) {  // 不能超出上边界
            mCropRectF.bottom += mMainImageRect.top - mCropRectF.top;
            mCropRectF.top = mMainImageRect.top;
        }
        if (mCropRectF.bottom > mMainImageRect.bottom) {    // 不能超出下边界
            mCropRectF.top -= mCropRectF.bottom - mMainImageRect.bottom;
            mCropRectF.bottom = mMainImageRect.bottom;
        }
        if (mCropRectF.top < mMainImageRect.top || mCropRectF.bottom > mMainImageRect.bottom) { // 调整后不能超出上下边界
            mCropRectF.set(mTempCropRect);
        }
    }

    /**
     * 移动裁剪框
     */
    private void translateCrop(float dx, float dy) {
        translateRectF(mCropRectF, dx, dy);

        // 边界判定算法优化
        float mdLeft = mMainImageRect.left - mCropRectF.left;
        if (mdLeft > 0) {   // 超出左边界
            translateRectF(mCropRectF, mdLeft, 0);
        }
        float mdRight = mMainImageRect.right - mCropRectF.right;
        if (mdRight < 0) {  // 超出右边界
            translateRectF(mCropRectF, mdRight, 0);
        }
        float mdTop = mMainImageRect.top - mCropRectF.top;
        if (mdTop > 0) {    // 超出上边界
            translateRectF(mCropRectF, 0, mdTop);
        }
        float mdBottom = mMainImageRect.bottom - mCropRectF.bottom;
        if (mdBottom < 0) { // 超出下边界
            translateRectF(mCropRectF, 0, mdBottom);
        }
        invalidate();
    }

    /**
     * 移动矩阵
     */
    private static void translateRectF(RectF rectF, float dx, float dy) {
        rectF.left += dx;
        rectF.right += dx;
        rectF.top += dy;
        rectF.bottom += dy;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        // 绘制辅助线
        canvas.drawRect(mCropRectF.left,
                mCropRectF.top,
                mCropRectF.right,
                mCropRectF.bottom,
                mHelpLinePaint);

        // 绘制控制圆点
        int halfCircleWidth = CIRCLE_WIDTH >> 1;
        for (Circle value : Circle.values()) {
            if (Circle.NONE != value) {
                value.updateCircleRectF(mCropRectF, halfCircleWidth);
                canvas.drawBitmap(mCircleBitmap, mCircleRect, value.getCircleRectF(), mCirclePaint);
            }
        }
    }

    public void setRatioCropRectF(RectF rectF, float radio) {
        mRatio = radio;
        if (mRatio < 0) {    // 任意比例
            setCropRectF(rectF);
            return;
        }

        mMainImageRect.set(rectF);
        mCropRectF.set(rectF);

        float width, height;
        if (mCropRectF.width() >= mCropRectF.height()) {
            height = mCropRectF.height() / 2;
            width = mRatio * height;
        } else {
            width = rectF.width() / 2;
            height = width / mRatio;
        }
        float scaleX = width / mCropRectF.width();
        float scaleY = height / mCropRectF.height();
        RectUtil.scaleRect(mCropRectF, scaleX, scaleY);
        invalidate();
    }

    /**
     * 重置剪裁框
     */
    public void setCropRectF(RectF rectF) {
        if (rectF == null) {
            return;
        }

        mMainImageRect.set(rectF);
        mCropRectF.set(rectF);
        RectUtil.scaleRect(mCropRectF, 0.5f, 0.5f);
        invalidate();
    }

    /**
     * 返回裁剪框矩阵
     */
    public RectF getCropRectF() {
        return new RectF(mCropRectF);
    }

    /**
     * 圆点 enum
     */
    enum Circle {
        NONE {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
            }
        },   // 未选中如何控制点，默认

        LEFT_TOP {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
                cropRect.left = x;
                cropRect.top = y;
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
                setCircleRectF(cropRect.left - halfCircleWidth, cropRect.top - halfCircleWidth,
                        cropRect.left + halfCircleWidth, cropRect.top + halfCircleWidth);
            }
        },   // 左上点

        RIGHT_TOP {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
                cropRect.right = x;
                cropRect.top = y;
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
                setCircleRectF(cropRect.right - halfCircleWidth, cropRect.top - halfCircleWidth,
                        cropRect.right + halfCircleWidth, cropRect.top + halfCircleWidth);
            }
        },   // 右上点

        LEFT_BOTTOM {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
                cropRect.left = x;
                cropRect.bottom = y;
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
                setCircleRectF(cropRect.left - halfCircleWidth, cropRect.bottom - halfCircleWidth,
                        cropRect.left + halfCircleWidth, cropRect.bottom + halfCircleWidth);
            }
        },   // 左下点

        RIGHT_BOTTOM {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
                cropRect.right = x;
                cropRect.bottom = y;
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
                setCircleRectF(cropRect.right - halfCircleWidth, cropRect.bottom - halfCircleWidth,
                        cropRect.right + halfCircleWidth, cropRect.bottom + halfCircleWidth);
            }
        },   // 右下点

        LEFT_CENTER_TOP {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
                cropRect.top = y;
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
                setCircleRectF(cropRect.centerX() - halfCircleWidth, cropRect.top - halfCircleWidth,
                        cropRect.centerX() + halfCircleWidth, cropRect.top + halfCircleWidth);
            }
        },   // 左上中点

        LEFT_CENTER_BOTTOM {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
                cropRect.left = x;
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
                setCircleRectF(cropRect.left - halfCircleWidth, cropRect.centerY() - halfCircleWidth,
                        cropRect.left + halfCircleWidth, cropRect.centerY() + halfCircleWidth);
            }
        },   // 左下中点

        RIGHT_CENTER_BOTTOM {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
                cropRect.right = x;
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
                setCircleRectF(cropRect.right - halfCircleWidth, cropRect.centerY() - halfCircleWidth,
                        cropRect.right + halfCircleWidth, cropRect.centerY() + halfCircleWidth);
            }
        },   // 右下中点

        BOTTOM_CENTER_BOTTOM {
            @Override
            public void updateCropRectF(RectF cropRect, float x, float y) {
                cropRect.bottom = y;
            }

            @Override
            public void updateCircleRectF(RectF cropRect, float halfCircleWidth) {
                setCircleRectF(cropRect.centerX() - halfCircleWidth, cropRect.bottom - halfCircleWidth,
                        cropRect.centerX() + halfCircleWidth, cropRect.bottom + halfCircleWidth);
            }
        };   // 底下中点

        private final RectF mCircleRectF = new RectF(0, 0, CIRCLE_WIDTH, CIRCLE_WIDTH);

        public RectF getCircleRectF() {
            return mCircleRectF;
        }

        public void setCircleRectF(float left, float top, float right, float bottom) {
            mCircleRectF.set(left, top, right, bottom);
        }

        public boolean contains(float x, float y) {
            return mCircleRectF.contains(x, y);
        }

        /**
         * 更新 CropRectF 位置
         */
        public abstract void updateCropRectF(RectF cropRect, float x, float y);

        /**
         * 通过 CropRectF 来更新 CircleRectF 位置
         */
        public abstract void updateCircleRectF(RectF cropRect, float halfCircleWidth);
    }
}
