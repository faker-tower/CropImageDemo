package com.axiang.cropimagedemo.editimg.sticker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import com.axiang.cropimagedemo.editimg.StickerTask;
import com.axiang.cropimagedemo.util.BitmapUtil;
import com.axiang.cropimagedemo.view.sticker.StickerItem;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

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

    private SaveStickersTask mSaveStickersTask;

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

    /**
     * 保存贴图层 合成一张图片
     */
    public void applyStickers() {
        if (mSaveStickersTask != null) {
            mSaveStickersTask.cancel(true);
        }
        mSaveStickersTask = new SaveStickersTask(mActivity, this);
        mSaveStickersTask.execute(mActivity.mMainBitmap);
    }

    /**
     * 保存贴图任务
     */
    private static class SaveStickersTask extends StickerTask {

        private final WeakReference<StickerFragment> mStickerReference;

        public SaveStickersTask(EditImageActivity activity, StickerFragment stickerFragment) {
            super(activity);
            mStickerReference = new WeakReference<>(stickerFragment);
        }

        @Override
        public void handleImage(Canvas canvas, Matrix matrix) {
            EditImageActivity editImageActivity = mEditImageActReference.get();
            if (editImageActivity == null || editImageActivity.isFinishing()) {
                return;
            }

            LinkedHashMap<Integer, StickerItem> addItems = editImageActivity.mStickerView.getStickerBank();
            for (Integer id : addItems.keySet()) {
                StickerItem item = addItems.get(id);
                if (item != null) {
                    item.mStickerMatrix.postConcat(matrix); // 乘以底部图片变化矩阵
                    canvas.drawBitmap(item.mStickerBitmap, item.mStickerMatrix, null);
                }
            }
        }

        @Override
        public void onPostResult(Bitmap result) {
            EditImageActivity editImageActivity = mEditImageActReference.get();
            if (editImageActivity == null || editImageActivity.isFinishing()) {
                return;
            }

            StickerFragment stickerFragment = mStickerReference.get();
            if (stickerFragment == null || !stickerFragment.isAdded()) {
                return;
            }

            editImageActivity.mStickerView.clear();
            editImageActivity.changeMainBitmap(result);
            stickerFragment.backToMain();
        }
    }
}
