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
    public static final int PADDING = 32;   // 文字和辅助框的间距

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Status.IDLE, Status.MOVE, Status.DELETE, Status.ROTATE})
    public @interface Status {
        int IDLE = 0;   // 静止状态，默认
        int MOVE = 1;   // 移动状态
        int DELETE = 2; // 删除状态
        int ROTATE = 3; // 字体旋转状态
    }

    private TextPaint mTextPaint;
    private final Rect mTextRect = new Rect();

    private Paint mHelpLinePaint;
    private final RectF mHelpLineRect = new RectF();
    private Rect mHelpToolsRect;

    private Bitmap mDeleteBitmap;
    private Bitmap mRotateBitmap;
    private RectF mDeleteDstRect = new RectF();
    private RectF mRotateDstRect = new RectF();

    private int mCurrentMode = Status.IDLE;

    private WeakReference<EditText> mEditTextReference; // 底下的输入控件

    public int mLayoutX;
    public int mLayoutY;
    private float mLastX = 0;
    private float mLastY = 0;

    public float mRotateAngle;
    public float mScale = 1;
    private boolean isInitLayout = true;

    private boolean isShowHelpBox = true;

    private final List<String> mTextContents = new ArrayList<>(2);  // 存放所写的文字内容
    private String mText;

    private final Point mPoint = new Point(0, 0);

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

        mDeleteDstRect = new RectF(0, 0, Constants.STICKER_BTN_HALF_SIZE << 1, Constants.STICKER_BTN_HALF_SIZE << 1);
        mRotateDstRect = new RectF(0, 0, Constants.STICKER_BTN_HALF_SIZE << 1, Constants.STICKER_BTN_HALF_SIZE << 1);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(TEXT_SIZE_DEFAULT);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mHelpLinePaint = PaintUtil.newHelpLinePaint();
    }

    public void setText(String text) {
        mText = text;
        invalidate();
    }

    public void setTextColor(int newColor) {
        mTextPaint.setColor(newColor);
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
        mDeleteDstRect.offsetTo(mHelpLineRect.left - offsetValue, mHelpLineRect.top - offsetValue);
        mRotateDstRect.offsetTo(mHelpLineRect.right - offsetValue, mHelpLineRect.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteDstRect, mHelpLineRect.centerX(),
                mHelpLineRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mRotateDstRect, mHelpLineRect.centerX(),
                mHelpLineRect.centerY(), mRotateAngle);

        if (!isShowHelpBox) {
            return;
        }

        canvas.save();
        canvas.rotate(mRotateAngle, mHelpLineRect.centerX(), mHelpLineRect.centerY());
        canvas.drawRoundRect(mHelpLineRect, 10, 10, mHelpLinePaint);
        canvas.restore();

        canvas.drawBitmap(mDeleteBitmap, mHelpToolsRect, mDeleteDstRect, null);
        canvas.drawBitmap(mRotateBitmap, mHelpToolsRect, mRotateDstRect, null);
    }

    private void drawText(Canvas canvas) {
        drawText(canvas, mLayoutX, mLayoutY, mScale, mRotateAngle);
    }

    public void drawText(Canvas canvas, int _x, int _y, float scale, float rotate) {
        if (mTextContents.isEmpty()) {
            return;
        }

        int text_height;
        mTextRect.set(0, 0, 0, 0);  // 先清空
        Rect tempRect = new Rect();
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        int charMinHeight = Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom);   // 字体高度
        text_height = charMinHeight;

        for (int i = 0; i < mTextContents.size(); i++) {
            String text = mTextContents.get(i);
            mTextPaint.getTextBounds(text, 0, text.length(), tempRect);

            if (tempRect.height() <= 0) {   // 处理此行文字为空的情况
                tempRect.set(0, 0, 0, text_height);
            }

            RectUtil.rectAddV(mTextRect, tempRect, 0, charMinHeight);
        }
        mTextRect.offset(_x, _y);

        mHelpLineRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                , mTextRect.right + PADDING, mTextRect.bottom + PADDING);
        RectUtil.scaleRect(mHelpLineRect, scale, scale);

        canvas.save();
        canvas.scale(scale, scale, mHelpLineRect.centerX(), mHelpLineRect.centerY());
        canvas.rotate(rotate, mHelpLineRect.centerX(), mHelpLineRect.centerY());

        int draw_text_y = _y + (text_height >> 1) + PADDING;
        for (int i = 0; i < mTextContents.size(); i++) {
            canvas.drawText(mTextContents.get(i), _x, draw_text_y, mTextPaint);
            draw_text_y += text_height;
        }
        canvas.restore();
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
                    clearTextContent(); // 清空底下输入框
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                result = true;
                if (mCurrentMode == Status.MOVE) {  // 移动贴图
                    float dx = x - mLastX;
                    float dy = y - mLastY;

                    mLayoutX += dx;
                    mLayoutY += dy;

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

    /**
     * 考虑旋转情况下 点击点是否在内容矩形内
     */
    private boolean detectInHelpBox(float x, float y) {
        mPoint.set((int) x, (int) y);
        // 旋转点击点
        RectUtil.rotatePoint(mPoint, mHelpLineRect.centerX(), mHelpLineRect.centerY(), -mRotateAngle);
        return mHelpLineRect.contains(mPoint.x, mPoint.y);
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
        float c_x = mHelpLineRect.centerX();
        float c_y = mHelpLineRect.centerY();

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
        float newWidth = mHelpLineRect.width() * mScale;

        if (newWidth < 70) {
            mScale /= scale;
            return;
        }

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xa * yb - xb * ya;    // 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
    }

    public void resetView() {
        mLayoutX = getMeasuredWidth() / 2;
        mLayoutY = getMeasuredHeight() / 2;
        mRotateAngle = 0;
        mScale = 1;
        mTextContents.clear();
    }

    public float getScale() {
        return mScale;
    }
}
