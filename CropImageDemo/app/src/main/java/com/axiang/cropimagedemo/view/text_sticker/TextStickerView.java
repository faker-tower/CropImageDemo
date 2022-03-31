package com.axiang.cropimagedemo.view.text_sticker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axiang.cropimagedemo.Constants;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.util.PaintUtil;
import com.axiang.cropimagedemo.util.RectUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextStickerView extends View {

    public static final float TEXT_SIZE_DEFAULT = 76f;  // 字体大小
    public static final int PADDING = 32;   // 文字和辅助线的间距

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.IDLE, Status.MOVE, Status.DELETE, Status.ROTATE})
    public @interface Status {
        int IDLE = 0;   // 静止状态，默认
        int MOVE = 1;   // 移动状态
        int DELETE = 2; // 删除状态
        int ROTATE = 3; // 旋转状态
    }

    private TextPaint mTextPaint;
    private final Rect mTextRect = new Rect();

    private Paint mHelpLinePaint;
    private final RectF mHelpLineRectF = new RectF();

    private Bitmap mDeleteBitmap;
    private Bitmap mRotateBitmap;
    private Rect mHelpToolsRect;    // 删除、旋转按钮的大小矩阵
    private RectF mDeleteDstRect = new RectF(); // 删除按钮的位置矩阵
    private RectF mRotateDstRect = new RectF(); // 旋转按钮的位置矩阵

    public int mLayoutCenterX;    // 文字中心点的X坐标
    public int mLayoutCenterY;    // 文字中心点的Y坐标
    private float mLastX = 0;
    private float mLastY = 0;

    public float mRotateAngle;
    public float mScale = 1f;
    private boolean isInitLayout = true;
    private boolean isShowHelpBox = true;

    private WeakReference<EditText> mEditTextReference; // 底下的输入框
    private final List<String> mTextContents = new ArrayList<>(2);  // 存放所写的文字内容
    private String mText;   // 输入框里的文字

    private final Point mPoint = new Point(0, 0);
    private int mCurrentMode = Status.IDLE;

    public TextStickerView(Context context) {
        super(context);
        initView(context);
    }

    public TextStickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TextStickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(@NonNull Context context) {
        mDeleteBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.sticker_delete);
        mRotateBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.sticker_rotate);

        mHelpToolsRect = new Rect(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());

        mDeleteDstRect = new RectF(0, 0,
                Constants.STICKER_BTN_HALF_SIZE << 1, Constants.STICKER_BTN_HALF_SIZE << 1);
        mRotateDstRect = new RectF(0, 0,
                Constants.STICKER_BTN_HALF_SIZE << 1, Constants.STICKER_BTN_HALF_SIZE << 1);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(TEXT_SIZE_DEFAULT);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mHelpLinePaint = PaintUtil.newHelpLinePaint();
    }

    public void setText(String text) {
        mText = text;
        invalidate();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isInitLayout) {
            isInitLayout = false;
            resetView();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);    // 是否向下传递事件标志，true为消耗
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mDeleteDstRect.contains(x, y)) {    // 删除模式
                    isShowHelpBox = true;
                    mCurrentMode = Status.DELETE;
                } else if (mRotateDstRect.contains(x, y)) { // 旋转模式
                    isShowHelpBox = true;
                    mCurrentMode = Status.ROTATE;
                    mLastX = mRotateDstRect.centerX();
                    mLastY = mRotateDstRect.centerY();
                    result = true;
                } else if (detectInHelpBox(x, y)) { // 移动模式
                    isShowHelpBox = true;
                    mCurrentMode = Status.MOVE;
                    mLastX = x;
                    mLastY = y;
                    result = true;
                } else {
                    isShowHelpBox = false;
                    invalidate();
                }

                if (mCurrentMode == Status.DELETE) {    // 删除选定贴图
                    mCurrentMode = Status.IDLE; // 返回空闲状态
                    clearTextContent(); // 清空底下输入框，输入框会触发 setText() 重绘视图达到删除的目的
                }
                break;
            case MotionEvent.ACTION_MOVE:
                result = true;
                if (mCurrentMode == Status.MOVE) {  // 移动贴图
                    float dx = x - mLastX;
                    float dy = y - mLastY;

                    mLayoutCenterX += dx;
                    mLayoutCenterY += dy;

                    invalidate();

                    mLastX = x;
                    mLastY = y;
                } else if (mCurrentMode == Status.ROTATE) { // 旋转 缩放文字操作
                    float dx = x - mLastX;
                    float dy = y - mLastY;
                    updateRotateAndScale(dx, dy);

                    invalidate();
                    mLastX = x;
                    mLastY = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                result = false;
                mCurrentMode = Status.IDLE;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(mText)) {
            return;
        }

        parseText();
        drawContent(canvas);
    }

    private void parseText() {
        if (TextUtils.isEmpty(mText)) {
            return;
        }

        mTextContents.clear();
        String[] splits = mText.split("\n");
        mTextContents.addAll(Arrays.asList(splits));
    }

    private void drawContent(Canvas canvas) {
        drawText(canvas);

        int offsetValue = ((int) mDeleteDstRect.width()) >> 1;
        mDeleteDstRect.offsetTo(mHelpLineRectF.left - offsetValue, mHelpLineRectF.top - offsetValue);
        mRotateDstRect.offsetTo(mHelpLineRectF.right - offsetValue, mHelpLineRectF.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteDstRect, mHelpLineRectF.centerX(),
                mHelpLineRectF.centerY(), mRotateAngle);
        RectUtil.rotateRect(mRotateDstRect, mHelpLineRectF.centerX(),
                mHelpLineRectF.centerY(), mRotateAngle);

        if (!isShowHelpBox) {
            return;
        }

        canvas.save();
        canvas.rotate(mRotateAngle, mHelpLineRectF.centerX(), mHelpLineRectF.centerY());
        canvas.drawRoundRect(mHelpLineRectF, 10, 10, mHelpLinePaint);
        canvas.restore();

        canvas.drawBitmap(mDeleteBitmap, mHelpToolsRect, mDeleteDstRect, null);
        canvas.drawBitmap(mRotateBitmap, mHelpToolsRect, mRotateDstRect, null);
    }

    public void drawText(Canvas canvas) {
        if (mTextContents.isEmpty()) {
            return;
        }

        int textHeight, layoutStartX, layoutStartY;

        mTextRect.set(0, 0, 0, 0);  // 先清空
        Rect tempRect = new Rect();
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        int charMinHeight = Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom);   // 字体高度
        textHeight = charMinHeight;

        // 计算文字所有行加起来的总宽高
        for (int i = 0; i < mTextContents.size(); i++) {
            String text = mTextContents.get(i);
            mTextPaint.getTextBounds(text, 0, text.length(), tempRect);

            if (tempRect.height() <= 0) {   // 处理此行文字为空的情况
                tempRect.set(0, 0, 0, textHeight);
            }

            RectUtil.rectAddY(mTextRect, tempRect, 0, charMinHeight);
        }

        layoutStartX = mLayoutCenterX - mTextRect.width() / 2;
        layoutStartY = mLayoutCenterY - mTextRect.height() / 2;
        mTextRect.offset(layoutStartX, layoutStartY);

        // 设置辅助线的位置
        mHelpLineRectF.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                , mTextRect.right + PADDING, mTextRect.bottom + PADDING);
        RectUtil.scaleRect(mHelpLineRectF, mScale, mScale);

        canvas.save();
        canvas.scale(mScale, mScale, mHelpLineRectF.centerX(), mHelpLineRectF.centerY());
        canvas.rotate(mRotateAngle, mHelpLineRectF.centerX(), mHelpLineRectF.centerY());
        int baselineY = layoutStartY + (textHeight >> 1) + PADDING; // 基准线
        for (int i = 0; i < mTextContents.size(); i++) {
            canvas.drawText(mTextContents.get(i), layoutStartX, baselineY, mTextPaint);
            baselineY += textHeight;
        }
        canvas.restore();
    }

    /**
     * 考虑旋转情况下 点击点是否在内容矩形内
     */
    private boolean detectInHelpBox(float x, float y) {
        mPoint.set((int) x, (int) y);
        // 旋转点击点
        RectUtil.rotatePoint(mPoint, mHelpLineRectF.centerX(), mHelpLineRectF.centerY(), -mRotateAngle);
        return mHelpLineRectF.contains(mPoint.x, mPoint.y);
    }

    public void setEditText(EditText textView) {
        mEditTextReference = new WeakReference<>(textView);
    }

    public void clearTextContent() {
        if (mEditTextReference != null && mEditTextReference.get() != null) {
            mEditTextReference.get().setText("");
        }
    }

    /**
     * 旋转 缩放 更新
     */
    public void updateRotateAndScale(final float dx, final float dy) {
        float c_x = mHelpLineRectF.centerX();
        float c_y = mHelpLineRectF.centerY();

        float x = mRotateDstRect.centerX();
        float y = mRotateDstRect.centerY();

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;  // 计算缩放比
        mScale *= scale;
        float newWidth = mHelpLineRectF.width() * mScale;
        if (newWidth < 70) {    // 最小缩放检测
            mScale /= scale;
            return;
        }

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1) {
            return;
        }
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xa * yb - xb * ya;    // 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
    }

    /**
     * 重置视图
     */
    public void resetView() {
        mLayoutCenterX = getMeasuredWidth() / 2;
        mLayoutCenterY = getMeasuredHeight() / 2;
        mRotateAngle = 0;
        mScale = 1f;
        mTextContents.clear();
    }

    public float getScale() {
        return mScale;
    }
}
