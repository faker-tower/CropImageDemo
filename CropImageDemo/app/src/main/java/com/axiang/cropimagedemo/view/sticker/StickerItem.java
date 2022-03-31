package com.axiang.cropimagedemo.view.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.axiang.cropimagedemo.Constants;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.util.PaintUtil;
import com.axiang.cropimagedemo.util.RectUtil;

public class StickerItem {

    public static final String TAG = "StickerItem";

    private static final float MIN_SCALE = 0.15f;
    private static final int HELP_BOX_PAD = 25;

    private static final int BUTTON_WIDTH = Constants.STICKER_BTN_HALF_SIZE;

    public Bitmap mStickerBitmap;
    public Rect mSrcRect;    // 原始图片坐标
    public RectF mDstRect;   // 绘制目标坐标
    private Rect mHelpToolsRect;
    public RectF mDeleteRect;    // 删除按钮位置
    public RectF mRotateRect;    // 旋转按钮位置

    public RectF mHelpBox;
    public Matrix mStickerMatrix;   // 变化矩阵
    public float mRotateAngle = 0;
    boolean mIsDrawHelpTool = false;
    private final Paint mHelpBoxPaint;

    private float mInitWidth;    // 加入屏幕时原始宽度

    private static Bitmap mDeleteBitmap;
    private static Bitmap mRotateBitmap;

    // 记录经过旋转、平移、缩放等操作之后的旋转按钮的 Rect，主要是用来判断是否包含 Action.DOWN 手势落点
    public RectF mDetectRotateRect;
    // 记录经过旋转、平移、缩放等操作之后的删除按钮的 Rect，主要是用来判断是否包含 Action.DOWN 手势落点
    public RectF mDetectDeleteRect;

    public StickerItem(Context context) {
        mHelpBoxPaint = PaintUtil.newHelpLinePaint();

        // 导入工具按钮位图
        if (mDeleteBitmap == null) {
            mDeleteBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sticker_delete);
        }
        if (mRotateBitmap == null) {
            mRotateBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sticker_rotate);
        }
    }

    public void init(Bitmap addBit, View parentView) {
        mStickerBitmap = addBit;
        mSrcRect = new Rect(0, 0, addBit.getWidth(), addBit.getHeight());
        int bitWidth = Math.min(addBit.getWidth(), parentView.getWidth() >> 1);
        int bitHeight = bitWidth * addBit.getHeight() / addBit.getWidth();
        int left = (parentView.getWidth() >> 1) - (bitWidth >> 1);
        int top = (parentView.getHeight() >> 1) - (bitHeight >> 1);
        mDstRect = new RectF(left, top, left + bitWidth, top + bitHeight);
        mStickerMatrix = new Matrix();
        mStickerMatrix.postTranslate(mDstRect.left, mDstRect.top);
        mStickerMatrix.postScale((float) bitWidth / addBit.getWidth(),
                (float) bitHeight / addBit.getHeight(), mDstRect.left,
                mDstRect.top);
        mInitWidth = mDstRect.width();    // 记录原始宽度
        mIsDrawHelpTool = true;
        mHelpBox = new RectF(mDstRect);
        updateHelpBoxRect();

        mHelpToolsRect = new Rect(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());

        mDeleteRect = new RectF(mHelpBox.left - BUTTON_WIDTH, mHelpBox.top
                - BUTTON_WIDTH, mHelpBox.left + BUTTON_WIDTH, mHelpBox.top
                + BUTTON_WIDTH);
        mRotateRect = new RectF(mHelpBox.right - BUTTON_WIDTH, mHelpBox.bottom
                - BUTTON_WIDTH, mHelpBox.right + BUTTON_WIDTH, mHelpBox.bottom
                + BUTTON_WIDTH);

        mDetectRotateRect = new RectF(mRotateRect);
        mDetectDeleteRect = new RectF(mDeleteRect);
    }

    private void updateHelpBoxRect() {
        mHelpBox.left -= HELP_BOX_PAD;
        mHelpBox.right += HELP_BOX_PAD;
        mHelpBox.top -= HELP_BOX_PAD;
        mHelpBox.bottom += HELP_BOX_PAD;
    }

    /**
     * 位置更新
     */
    public void updatePosition(final float dx, final float dy) {
        mStickerMatrix.postTranslate(dx, dy);  // 记录到矩阵中
        mDstRect.offset(dx, dy);

        // 工具按钮随之移动
        mHelpBox.offset(dx, dy);
        mDeleteRect.offset(dx, dy);
        mRotateRect.offset(dx, dy);

        mDetectRotateRect.offset(dx, dy);
        mDetectDeleteRect.offset(dx, dy);
    }

    /**
     * 更新旋转、缩放
     */
    public void updateRotateAndScale(final float dx, final float dy) {
        float c_x = mDstRect.centerX();
        float c_y = mDstRect.centerY();

        float x = mDetectRotateRect.centerX();
        float y = mDetectRotateRect.centerY();

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;  // 计算缩放比

        float newWidth = mDstRect.width() * scale;
        if (newWidth / mInitWidth < MIN_SCALE) {    // 最小缩放值检测
            return;
        }

        mStickerMatrix.postScale(scale, scale, mDstRect.centerX(), mDstRect.centerY());    // 存入scale矩阵
        RectUtil.scaleRect(mDstRect, scale, scale); // 缩放目标矩形

        // 重新计算工具箱坐标
        mHelpBox.set(mDstRect);
        updateHelpBoxRect();    // 重新计算
        mRotateRect.offsetTo(mHelpBox.right - BUTTON_WIDTH, mHelpBox.bottom
                - BUTTON_WIDTH);
        mDeleteRect.offsetTo(mHelpBox.left - BUTTON_WIDTH, mHelpBox.top
                - BUTTON_WIDTH);

        mDetectRotateRect.offsetTo(mHelpBox.right - BUTTON_WIDTH, mHelpBox.bottom
                - BUTTON_WIDTH);
        mDetectDeleteRect.offsetTo(mHelpBox.left - BUTTON_WIDTH, mHelpBox.top
                - BUTTON_WIDTH);

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1) {
            return;
        }
        float angle = (float) Math.toDegrees(Math.acos(cos));

        // 定理
        float calMatrix = xa * yb - xb * ya;    // 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        Log.d(TAG, "angle: " + angle);
        mRotateAngle += angle;
        mStickerMatrix.postRotate(angle, mDstRect.centerX(), mDstRect.centerY());

        RectUtil.rotateRect(mDetectRotateRect, mDstRect.centerX(), mDstRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mDetectDeleteRect, mDstRect.centerX(), mDstRect.centerY(), mRotateAngle);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(mStickerBitmap, mStickerMatrix, null);// 贴图元素绘制

        if (mIsDrawHelpTool) {// 绘制辅助工具线
            canvas.save();
            canvas.rotate(mRotateAngle, mHelpBox.centerX(), mHelpBox.centerY());
            canvas.drawRoundRect(mHelpBox, 10, 10, mHelpBoxPaint);
            // 绘制工具按钮
            canvas.drawBitmap(mDeleteBitmap, mHelpToolsRect, mDeleteRect, null);
            canvas.drawBitmap(mRotateBitmap, mHelpToolsRect, mRotateRect, null);
            canvas.restore();
        }
    }
}
