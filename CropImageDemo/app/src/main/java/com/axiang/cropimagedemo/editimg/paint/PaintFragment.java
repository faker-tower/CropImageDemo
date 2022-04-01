package com.axiang.cropimagedemo.editimg.paint;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.ModuleConfig;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.editimg.BaseEditImageFragment;
import com.axiang.cropimagedemo.editimg.EditImageActivity;
import com.axiang.cropimagedemo.util.DialogUtil;

/**
 * 涂鸦 Fragment
 * Created by 邱翔威 on 2022/4/1
 */
public class PaintFragment extends BaseEditImageFragment
        implements ColorPaintAdapter.OnItemClickListener, ImagePaintAdapter.OnItemClickListener {

    public static final int INDEX = ModuleConfig.INDEX_PAINT;

    private final int[] mPaintColors = {Color.BLACK,
            Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.WHITE,
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, R.drawable.ic_more};

    private ImageView mIvBackToMain;
    private ViewFlipper mViewFlipperPaint;
    private RecyclerView mRvColorPaintList;
    private Button mBtnSwitchImage;
    private RecyclerView mRvImagePaintList;
    private Button mBtnSwitchColor;
    private ImageView mPaintEraser;

    private PopupWindow mPaintSizePopup;
    private SeekBar mPaintSizeSeekBar;

    private ColorPaintAdapter mColorPaintAdapter;
    private ImagePaintAdapter mImagePaintAdapter;

    private boolean mIsEraser = false;    // 是否是擦除模式

    public static PaintFragment newInstance() {
        return new PaintFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_paint, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mIvBackToMain = rootView.findViewById(R.id.iv_back_to_main);
        mViewFlipperPaint = rootView.findViewById(R.id.view_flipper_paint);
        mRvColorPaintList = rootView.findViewById(R.id.rv_color_paint_list);
        mBtnSwitchImage = rootView.findViewById(R.id.btn_switch_image);
        mRvImagePaintList = rootView.findViewById(R.id.rv_image_paint_list);
        mBtnSwitchColor = rootView.findViewById(R.id.btn_switch_color);
        mPaintEraser = rootView.findViewById(R.id.paint_eraser);

        mViewFlipperPaint.setInAnimation(mActivity, R.anim.in_bottom_to_top);
        mViewFlipperPaint.setOutAnimation(mActivity, R.anim.out_bottom_to_top);

        initRv();

        mIvBackToMain.setOnClickListener(view -> backToMain());
        mBtnSwitchImage.setOnClickListener(view -> mViewFlipperPaint.showNext());
        mBtnSwitchColor.setOnClickListener(view -> mViewFlipperPaint.showPrevious());
        mPaintEraser.setOnClickListener(view -> onEraserClick());
    }

    private void initRv() {
        mRvColorPaintList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvColorPaintList.setLayoutManager(mLayoutManager);
        mColorPaintAdapter = new ColorPaintAdapter(mActivity, mPaintColors);
        mColorPaintAdapter.setOnItemClickListener(this);
        mRvColorPaintList.setAdapter(mColorPaintAdapter);

        mRvColorPaintList.setHasFixedSize(true);
        LinearLayoutManager stickerListLayoutManager = new LinearLayoutManager(mActivity);
        stickerListLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvImagePaintList.setLayoutManager(stickerListLayoutManager);
        mImagePaintAdapter = new ImagePaintAdapter(mActivity);
        mImagePaintAdapter.setOnItemClickListener(this);
        mRvImagePaintList.setAdapter(mImagePaintAdapter);
    }

    private void onEraserClick() {

    }

    @Override
    public void onColorClick(int position) {

    }

    @Override
    public void onMoreClick() {
        DialogUtil.showColorPickerDialog(mActivity.getSupportFragmentManager(), 45, 215, 215,
                (red, green, blue) -> {

                });
    }

    @Override
    public void onImageClick() {

    }

    public void applyPaints() {

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
}
