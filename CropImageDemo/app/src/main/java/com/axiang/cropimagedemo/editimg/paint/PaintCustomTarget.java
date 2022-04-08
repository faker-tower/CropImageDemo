package com.axiang.cropimagedemo.editimg.paint;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.lang.ref.WeakReference;

/**
 * Created by 邱翔威 on 2022/4/7
 */
public abstract class PaintCustomTarget extends CustomTarget<Bitmap> {

    private final long mOperateRecordTime;
    private WeakReference<PaintFragment> mFragmentReference;

    public PaintCustomTarget(PaintFragment paintFragment, long operateRecordTime) {
        super();
        mFragmentReference = new WeakReference<>(paintFragment);
        mOperateRecordTime = operateRecordTime;
    }

    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        PaintFragment paintFragment = mFragmentReference.get();
        if (paintFragment == null || !paintFragment.isAdded()) {
            return;
        }
        onResourceReady(resource, mOperateRecordTime);
    }

    public abstract void onResourceReady(@NonNull Bitmap resource, long operateRecordTime);

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
    }
}
