package com.axiang.cropimagedemo.editimg.sticker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.ModuleConfig;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.editimg.BaseEditImageFragment;
import com.axiang.cropimagedemo.editimg.EditImageActivity;
import com.axiang.cropimagedemo.util.BitmapUtil;

/**
 * 贴图 Fragment
 */
public class StickerFragment extends BaseEditImageFragment {

    public static final int INDEX = ModuleConfig.INDEX_STICKER;

    private ViewFlipper mViewFlipperSticker;
    private ImageView mIvBackToMain;
    private RecyclerView mRvStickerTypeList;
    private ImageView mIvBackToType;
    private RecyclerView mRvStickerList;

    private StickerAdapter mStickerAdapter;

    public static StickerFragment newInstance() {
        return new StickerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sticker, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mViewFlipperSticker = rootView.findViewById(R.id.view_flipper_sticker);
        mIvBackToMain = rootView.findViewById(R.id.iv_back_to_main);
        mRvStickerTypeList = rootView.findViewById(R.id.rv_sticker_type_list);
        mIvBackToType = rootView.findViewById(R.id.iv_back_to_type);
        mRvStickerList = rootView.findViewById(R.id.rv_sticker_list);

        mViewFlipperSticker.setInAnimation(mActivity, R.anim.in_bottom_to_top);
        mViewFlipperSticker.setOutAnimation(mActivity, R.anim.out_bottom_to_top);

        // 固定尺寸，避衡重新衡量
        mRvStickerTypeList.setHasFixedSize(true);
        mRvStickerList.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvStickerTypeList.setLayoutManager(mLayoutManager);
        StickerTypeAdapter stickerTypeAdapter = new StickerTypeAdapter(mActivity);
        stickerTypeAdapter.setOnItemClickListener(this::swipeToStickerDetail);
        mRvStickerTypeList.setAdapter(stickerTypeAdapter);

        LinearLayoutManager stickerListLayoutManager = new LinearLayoutManager(mActivity);
        stickerListLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvStickerList.setLayoutManager(stickerListLayoutManager);
        mStickerAdapter = new StickerAdapter(mActivity);
        mStickerAdapter.setOnItemClickListener(stickerPath ->
                mActivity.mStickerView.addBitImage(BitmapUtil.getBitmapFromAssetsFile(mActivity, stickerPath)));
        mRvStickerList.setAdapter(mStickerAdapter);

        mIvBackToMain.setOnClickListener(view -> backToMain()); // 返回主菜单
        mIvBackToType.setOnClickListener(v -> mViewFlipperSticker.showPrevious());  // 返回上一级列表
    }

    @Override
    public void onShow() {
        mActivity.mMode = EditImageActivity.Mode.STICKERS;
        mActivity.mStickerView.setVisibility(View.VISIBLE);
        mActivity.mViewFlipperSave.showNext();
    }

    @Override
    public void backToMain() {
        mActivity.mMode = EditImageActivity.Mode.NONE;
        mActivity.mBottomOperateBar.setCurrentItem(0);
        mActivity.mStickerView.setVisibility(View.GONE);
        mActivity.mViewFlipperSave.showPrevious();
    }

    /**
     * 跳转至贴图详情列表
     *
     * @param path
     */
    public void swipeToStickerDetail(String path) {
        mStickerAdapter.addStickerImages(path);
        mViewFlipperSticker.showNext();
    }
}
