package com.axiang.cropimagedemo.view.paint;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Created by 邱翔威 on 2022/4/1
 */
public class PaintView extends View {

    public static final String TAG = "PaintView";

    public PaintView(Context context) {
        super(context);
        intiView();
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        intiView();
    }

    public PaintView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intiView();
    }

    private void intiView() {

    }
}
