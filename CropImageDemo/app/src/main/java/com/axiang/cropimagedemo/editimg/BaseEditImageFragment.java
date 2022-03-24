package com.axiang.cropimagedemo.editimg;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public abstract class BaseEditImageFragment extends Fragment implements DefaultLifecycleObserver {

    protected EditImageActivity mActivity;

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        mActivity = (EditImageActivity) owner;
        Log.d("BaseEditImageFragment", "onCreate: owner: " + owner);
        Log.d("BaseEditImageFragment", "owner instanceof EditImageActivity: " + (owner instanceof EditImageActivity ? "T" : "F"));
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        mActivity = null;
        Log.d("BaseEditImageFragment", "onDestroy: owner: " + owner);
    }

    // Fragment 显示
    public abstract void onShow();

    // 返回 MainMenuFragment
    public abstract void backToMain();
}
