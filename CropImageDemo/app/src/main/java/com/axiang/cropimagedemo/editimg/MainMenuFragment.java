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
    private TextView mTvCrop;
    private TextView mTvTextSticker;
    private TextView mTvPaint;
    private TextView mTvMagic;

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
        mTvCrop = rootView.findViewById(R.id.tv_crop);
        mTvTextSticker = rootView.findViewById(R.id.tv_text_sticker);
        mTvPaint = rootView.findViewById(R.id.tv_paint);
        mTvMagic = rootView.findViewById(R.id.tv_magic);

        mTvSticker.setOnClickListener(view -> onStickerClick());
        mTvCrop.setOnClickListener(view -> onCropClick());
        mTvTextSticker.setOnClickListener(view -> onTextClick());
        mTvPaint.setOnClickListener(view -> onPaintClick());
        mTvMagic.setOnClickListener(view -> onMagicClick());
    }

    /**
     * 贴图 点击
     */
    private void onStickerClick() {
        mActivity.mBottomOperateBar.setCurrentItem(ModuleConfig.INDEX_STICKER);
        mActivity.mStickerFragment.onShow();
    }

    /**
     * 裁剪 点击
     */
    private void onCropClick() {
        mActivity.mBottomOperateBar.setCurrentItem(ModuleConfig.INDEX_CROP);
        mActivity.mCropFragment.onShow();
    }

    /**
     * 文字 点击
     */
    private void onTextClick() {
        mActivity.mBottomOperateBar.setCurrentItem(ModuleConfig.INDEX_TEXT);
        mActivity.mTextStickerFragment.onShow();
    }

    /**
     * 涂鸦 点击
     */
    private void onPaintClick() {
        mActivity.mBottomOperateBar.setCurrentItem(ModuleConfig.INDEX_PAINT);
        mActivity.mPaintFragment.onShow();
    }

    /**
     * 指尖魔法 点击
     */
    private void onMagicClick() {
        mActivity.mBottomOperateBar.setCurrentItem(ModuleConfig.INDEX_MAGIC);
        mActivity.mMagicFragment.onShow();
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
