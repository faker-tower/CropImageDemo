package com.axiang.cropimagedemo.editimg;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public abstract class BaseEditImageFragment extends Fragment implements DefaultLifecycleObserver {

    protected EditImageActivity mActivity;

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        mActivity = (EditImageActivity) owner;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        mActivity = null;
    }

    // Fragment 显示
    public abstract void onShow();

    // 返回 MainMenuFragment
    public abstract void backToMain();
}
