package com.axiang.cropimagedemo.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * 可以禁用手动滑动的 ViewPager
 */
public class ScrollableViewPager extends ViewPager {

    private boolean canScroll = false;

    public ScrollableViewPager(@NonNull Context context) {
        super(context);
    }

    public ScrollableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        canScroll = true;
        super.setCurrentItem(item, smoothScroll);
        canScroll = false;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (canScroll) {
            super.scrollTo(x, y);
        }
    }
}
