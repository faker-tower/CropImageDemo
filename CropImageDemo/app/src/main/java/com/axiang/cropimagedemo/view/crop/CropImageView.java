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

    private float mOldX, mOldY;
    private int mCurrentStatus = Status.IDLE;
    private int mSelectedControllerCircle;

    private final RectF mCropRect = new RectF();// 剪切矩形

    private Bitmap mCircleBit;
    private final Rect mCircleRect = new Rect();
    private RectF mLeftTopCircleRect;
    private RectF mRightTopCircleRect;
    private RectF mLeftBottomRect;
    private RectF mRightBottomRect;

    private Paint mHelpLinePaint;   // 辅助线 Paint

    private final RectF mImageRect = new RectF();    // 存贮图片位置信息
    private final RectF mTempRect = new RectF(); // 临时存贮矩形数据

    private float mRatio = -1;// 剪裁缩放比率

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
        mCircleBit = BitmapFactory.decodeResource(context.getResources(), R.drawable.sticker_rotate);
        mCircleRect.set(0, 0, mCircleBit.getWidth(), mCircleBit.getHeight());
        mLeftTopCircleRect = new RectF(0, 0, CIRCLE_WIDTH, CIRCLE_WIDTH);
        mRightTopCircleRect = new RectF(mLeftTopCircleRect);
        mLeftBottomRect = new RectF(mLeftTopCircleRect);
        mRightBottomRect = new RectF(mLeftTopCircleRect);

        mHelpLinePaint = PaintUtil.newHelpLinePaint();
    }

    /**
     * 重置剪裁面
     */
    public void setCropRect(RectF rect) {
        if (rect == null) {
            return;
        }

        mImageRect.set(rect);
        mCropRect.set(rect);
        scaleRect(mCropRect, 0.5f, 0.5f);
        invalidate();
    }

    public void setRatioCropRect(RectF rect, float r) {
        this.mRatio = r;
        if (r < 0) {
            setCropRect(rect);
            return;
        }

        mImageRect.set(rect);
        mCropRect.set(rect);

        float h, w;
        if (mCropRect.width() >= mCropRect.height()) {
            h = mCropRect.height() / 2;
            w = this.mRatio * h;
        } else {
            w = rect.width() / 2;
            h = w / this.mRatio;
        }
        float scaleX = w / mCropRect.width();
        float scaleY = h / mCropRect.height();
        scaleRect(mCropRect, scaleX, scaleY);
        invalidate();
    }

    /**
     * 触摸事件处理
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);    // 是否向下传递事件标志 true为消耗
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int selectCircle = isSelectedControllerCircle(x, y);
                if (selectCircle > 0) { // 选择控制点
                    ret = true;
                    mSelectedControllerCircle = selectCircle; // 记录选中控制点编号
                    mCurrentStatus = Status.SCALE;  // 进入缩放状态
                } else if (mCropRect.contains(x, y)) {   // 选择缩放框内部
                    ret = true;
                    mCurrentStatus = Status.MOVE;   // 进入移动状态
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrentStatus == Status.SCALE) {   // 缩放控制
                    scaleCropController(x, y);
                } else if (mCurrentStatus == Status.MOVE) { // 移动控制
                    translateCrop(x - mOldX, y - mOldY);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mCurrentStatus = Status.IDLE;   // 回归空闲状态
                break;
        }

        // 记录上一次动作点
        mOldX = x;
        mOldY = y;

        return ret;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        // 绘制辅助线
        canvas.drawRect(mCropRect.left,
                mCropRect.top,
                mCropRect.right,
                mCropRect.bottom,
                mHelpLinePaint);

        // 绘制四个控制点
        int radius = CIRCLE_WIDTH >> 1;
        mLeftTopCircleRect.set(mCropRect.left - radius, mCropRect.top - radius,
                mCropRect.left + radius, mCropRect.top + radius);
        mRightTopCircleRect.set(mCropRect.right - radius, mCropRect.top - radius,
                mCropRect.right + radius, mCropRect.top + radius);
        mLeftBottomRect.set(mCropRect.left - radius, mCropRect.bottom - radius,
                mCropRect.left + radius, mCropRect.bottom + radius);
        mRightBottomRect.set(mCropRect.right - radius, mCropRect.bottom - radius,
                mCropRect.right + radius, mCropRect.bottom + radius);

        canvas.drawBitmap(mCircleBit, mCircleRect, mLeftTopCircleRect, null);
        canvas.drawBitmap(mCircleBit, mCircleRect, mRightTopCircleRect, null);
        canvas.drawBitmap(mCircleBit, mCircleRect, mLeftBottomRect, null);
        canvas.drawBitmap(mCircleBit, mCircleRect, mRightBottomRect, null);
    }

    /**
     * 移动剪切框
     */
    private void translateCrop(float dx, float dy) {
        mTempRect.set(mCropRect); // 存贮原有数据，以便还原

        translateRect(mCropRect, dx, dy);
        // 边界判定算法优化
        float mdLeft = mImageRect.left - mCropRect.left;
        if (mdLeft > 0) {
            translateRect(mCropRect, mdLeft, 0);
        }
        float mdRight = mImageRect.right - mCropRect.right;
        if (mdRight < 0) {
            translateRect(mCropRect, mdRight, 0);
        }
        float mdTop = mImageRect.top - mCropRect.top;
        if (mdTop > 0) {
            translateRect(mCropRect, 0, mdTop);
        }
        float mdBottom = mImageRect.bottom - mCropRect.bottom;
        if (mdBottom < 0) {
            translateRect(mCropRect, 0, mdBottom);
        }

        this.invalidate();
    }

    /**
     * 移动矩形
     */
    private static void translateRect(RectF rect, float dx, float dy) {
        rect.left += dx;
        rect.right += dx;
        rect.top += dy;
        rect.bottom += dy;
    }

    /**
     * 操作控制点 控制缩放
     */
    private void scaleCropController(float x, float y) {
        mTempRect.set(mCropRect); // 存贮原有数据，以便还原
        switch (mSelectedControllerCircle) {
            case 1: // 左上角控制点
                mCropRect.left = x;
                mCropRect.top = y;
                break;
            case 2: // 右上角控制点
                mCropRect.right = x;
                mCropRect.top = y;
                break;
            case 3: // 左下角控制点
                mCropRect.left = x;
                mCropRect.bottom = y;
                break;
            case 4: // 右下角控制点
                mCropRect.right = x;
                mCropRect.bottom = y;
                break;
        }

        if (mRatio < 0) {    // 任意缩放比
            // 边界条件检测
            validateCropRect();
            invalidate();
            return;
        }

        // 更新剪切矩形长宽，确定不变点
        switch (mSelectedControllerCircle) {
            case 1: // 左上角控制点
            case 2: // 右上角控制点
                mCropRect.top = mCropRect.bottom - (mCropRect.right - mCropRect.left) / this.mRatio;
                break;
            case 3: // 左下角控制点
            case 4: // 右下角控制点
                mCropRect.bottom = (mCropRect.right - mCropRect.left) / this.mRatio + mCropRect.top;
                break;
        }

        if (mCropRect.left < mImageRect.left
                || mCropRect.right > mImageRect.right
                || mCropRect.top < mImageRect.top
                || mCropRect.bottom > mImageRect.bottom
                || mCropRect.width() < CIRCLE_WIDTH
                || mCropRect.height() < CIRCLE_WIDTH) {
            mCropRect.set(mTempRect);
        }
        invalidate();
    }

    /**
     * 边界条件检测
     */
    private void validateCropRect() {
        if (mCropRect.width() < CIRCLE_WIDTH) {
            mCropRect.left = mTempRect.left;
            mCropRect.right = mTempRect.right;
        }
        if (mCropRect.height() < CIRCLE_WIDTH) {
            mCropRect.top = mTempRect.top;
            mCropRect.bottom = mTempRect.bottom;
        }
        if (mCropRect.left < mImageRect.left) {
            mCropRect.left = mImageRect.left;
        }
        if (mCropRect.right > mImageRect.right) {
            mCropRect.right = mImageRect.right;
        }
        if (mCropRect.top < mImageRect.top) {
            mCropRect.top = mImageRect.top;
        }
        if (mCropRect.bottom > mImageRect.bottom) {
            mCropRect.bottom = mImageRect.bottom;
        }
    }

    /**
     * 是否选中控制点，-1为没有
     */
    private int isSelectedControllerCircle(float x, float y) {
        if (mLeftTopCircleRect.contains(x, y)) {   // 选中左上角
            return 1;
        }
        if (mRightTopCircleRect.contains(x, y)) {  // 选中右上角
            return 2;
        }
        if (mLeftBottomRect.contains(x, y)) {  // 选中左下角
            return 3;
        }
        if (mRightBottomRect.contains(x, y)) { // 选中右下角
            return 4;
        }
        return -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 返回剪切矩形
     */
    public RectF getCropRect() {
        return new RectF(this.mCropRect);
    }

    /**
     * 缩放指定矩形
     */
    private void scaleRect(RectF rect, float scaleX, float scaleY) {
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
}
