package com.axiang.cropimagedemo.view.sticker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;

import com.axiang.cropimagedemo.util.RectUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashMap;

/**
 * 贴图操作控件
 */
public class StickerView extends View {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.IDLE, Status.MOVE, Status.DELETE, Status.ROTATE})
    public @interface Status {
        int IDLE = 0;   // 静止状态，默认
        int MOVE = 1;   // 移动状态
        int DELETE = 2; // 删除状态
        int ROTATE = 3; // 图片旋转状态
    }

    private int mStickerCount;  // 已加入的贴图数量
    private int mCurrentStatus = Status.IDLE;   // 当前状态
    private StickerItem mCurrentItem;   // 当前操作的贴图数据
    private final Point mDownPoint = new Point(0, 0);   // Action_DOWN 的点
    private float mLastX, mLastY;

    private final LinkedHashMap<Integer, StickerItem> mStickerBank = new LinkedHashMap<>(); // 存贮每层贴图数据

    public StickerView(Context context) {
        super(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addBitImage(final Bitmap addBit) {
        StickerItem item = new StickerItem(this.getContext());
        item.init(addBit, this);
        if (mCurrentItem != null) {
            mCurrentItem.mIsDrawHelpTool = false;
        }
        mStickerBank.put(++mStickerCount, item);
        invalidate();
    }

    /**
     * 绘制客户页面
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Integer id : mStickerBank.keySet()) {
            StickerItem item = mStickerBank.get(id);
            if (item != null) {
                item.draw(canvas);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);    // 是否向下传递事件标志 true为消耗
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                int deleteId = -1;
                for (Integer id : mStickerBank.keySet()) {
                    StickerItem item = mStickerBank.get(id);
                    if (item == null) {
                        continue;
                    }

                    if (item.mDetectDeleteRect.contains(x, y)) { // 点击了删除按钮
                        deleteId = id;
                        mCurrentStatus = Status.DELETE;
                    } else if (item.mDetectRotateRect.contains(x, y)) {  // 点击了旋转按钮
                        result = true;
                        if (mCurrentItem != null) {
                            mCurrentItem.mIsDrawHelpTool = false;
                        }
                        mCurrentItem = item;
                        mCurrentItem.mIsDrawHelpTool = true;
                        mCurrentStatus = Status.ROTATE;
                        mLastX = x;
                        mLastY = y;
                    } else if (detectInItemContent(item, x, y)) {   // 移动模式
                        // 被选中一张贴图
                        result = true;
                        if (mCurrentItem != null) {
                            mCurrentItem.mIsDrawHelpTool = false;
                        }
                        mCurrentItem = item;
                        mCurrentItem.mIsDrawHelpTool = true;
                        mCurrentStatus = Status.MOVE;
                        mLastX = x;
                        mLastY = y;
                    }
                }

                if (!result && mCurrentItem != null && mCurrentStatus == Status.IDLE) {    // 没有贴图被选择
                    mCurrentItem.mIsDrawHelpTool = false;
                    mCurrentItem = null;
                    invalidate();
                }

                if (deleteId > 0 && mCurrentStatus == Status.DELETE) {  // 删除选定贴图
                    mStickerBank.remove(deleteId);
                    mCurrentStatus = Status.IDLE;   // 返回空闲状态
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                result = true;
                if (mCurrentStatus == Status.MOVE) {    // 移动贴图
                    float dx = x - mLastX;
                    float dy = y - mLastY;
                    if (mCurrentItem != null) {
                        mCurrentItem.updatePosition(dx, dy);
                        invalidate();
                    }
                    mLastX = x;
                    mLastY = y;
                } else if (mCurrentStatus == Status.ROTATE) {   // 旋转 缩放图片操作
                    float dx = x - mLastX;
                    float dy = y - mLastY;
                    if (mCurrentItem != null) {
                        mCurrentItem.updateRotateAndScale(dx, dy);  // 旋转
                        invalidate();
                    }
                    mLastX = x;
                    mLastY = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                result = false;
                mCurrentStatus = Status.IDLE;
                break;
        }
        return result;
    }

    /**
     * 判定点击点是否在内容范围之内，需考虑旋转
     */
    private boolean detectInItemContent(StickerItem item, float x, float y) {
        // reset
        mDownPoint.set((int) x, (int) y);
        // 旋转点击点
        RectUtil.rotatePoint(mDownPoint, item.mHelpBox.centerX(), item.mHelpBox.centerY(), -item.mRotateAngle);
        return item.mHelpBox.contains(mDownPoint.x, mDownPoint.y);
    }

    public LinkedHashMap<Integer, StickerItem> getStickerBank() {
        return mStickerBank;
    }

    public void clear() {
        mStickerBank.clear();
        this.invalidate();
    }
}
