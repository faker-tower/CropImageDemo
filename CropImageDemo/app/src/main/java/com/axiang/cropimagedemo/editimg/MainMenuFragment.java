package com.axiang.cropimagedemo.editimg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axiang.cropimagedemo.ModuleConfig;
import com.axiang.cropimagedemo.R;

/**
 * 底部工具栏主菜单
 */
public class MainMenuFragment extends BaseEditImageFragment {

    public static final int INDEX = ModuleConfig.INDEX_MAIN;

    private TextView mTvSticker;

    public static MainMenuFragment newInstance() {
        return new MainMenuFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mTvSticker = rootView.findViewById(R.id.tv_sticker);

        mTvSticker.setOnClickListener(view -> onStickerClick());
    }

    // 贴图
    private void onStickerClick() {
        mActivity.mBottomOperateBar.setCurrentItem(ModuleConfig.INDEX_STICKER);
        mActivity.mStickerFragment.onShow();
    }

    @Override
    public void onShow() {
        // 不用处理
    }

    @Override
    public void backToMain() {
        // 不用处理
    }
}
