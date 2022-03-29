package com.axiang.cropimagedemo.editimg.crop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axiang.cropimagedemo.ModuleConfig;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.editimg.BaseEditImageFragment;
import com.axiang.cropimagedemo.editimg.EditImageActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 裁剪 Fragment
 */
public class CropFragment extends BaseEditImageFragment {

    public static final int INDEX = ModuleConfig.INDEX_CROP;
    private static final int SELECTED_COLOR = Color.YELLOW;
    private static final int UNSELECTED_COLOR = Color.WHITE;
    private static final List<RatioItem> mRatioList = new ArrayList<>();

    static {
        mRatioList.add(new RatioItem("none", -1f));
        mRatioList.add(new RatioItem("1:1", 1f));
        mRatioList.add(new RatioItem("1:2", 1 / 2f));
        mRatioList.add(new RatioItem("1:3", 1 / 3f));
        mRatioList.add(new RatioItem("2:3", 2 / 3f));
        mRatioList.add(new RatioItem("3:4", 3 / 4f));
        mRatioList.add(new RatioItem("2:1", 2f));
        mRatioList.add(new RatioItem("3:1", 3f));
        mRatioList.add(new RatioItem("3:2", 3 / 2f));
        mRatioList.add(new RatioItem("4:3", 4 / 3f));
    }

    private ImageView mIvBackToMain;
    private LinearLayout mLlRatioList;

    private final CropRatioBtnClick mCropRatioBtnClick = new CropRatioBtnClick();
    public TextView mSelectedTextView;


    public static CropFragment newInstance() {
        return new CropFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_crop, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mIvBackToMain = rootView.findViewById(R.id.iv_back_to_main);
        mLlRatioList = rootView.findViewById(R.id.ll_ratio_list);

        fillRatioView();

        mIvBackToMain.setOnClickListener(view -> backToMain());
    }

    private void fillRatioView() {
        mLlRatioList.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.leftMargin = 20;
        params.rightMargin = 20;

        for (int i = 0, len = mRatioList.size(); i < len; i++) {
            TextView text = new TextView(mActivity);
            text.setTextColor(UNSELECTED_COLOR);
            text.setTextSize(20);
            text.setText(mRatioList.get(i).getText());
            mLlRatioList.addView(text, params);
            text.setTag(i);
            if (i == 0) {
                mSelectedTextView = text;
            }
            mRatioList.get(i).setIndex(i);
            text.setTag(mRatioList.get(i));
            text.setOnClickListener(mCropRatioBtnClick);
        }
        mSelectedTextView.setTextColor(SELECTED_COLOR);
    }

    /**
     * 保存裁剪图片
     */
    public void applyCropImage() {
        CropImageTask task = new CropImageTask(mActivity,
                mActivity.mCropImageView.getCropRectF(),
                mActivity.mMainImageView.getImageViewMatrix(),
                this::onPostExecute);
        task.execute(mActivity.mMainBitmap);
    }

    private void onPostExecute(Bitmap result) {
        if (!isAdded()) {
            return;
        }

        mActivity.changeMainBitmap(result);
        mActivity.mCropImageView.setCropRectF(mActivity.mMainImageView.getBitmapRect());
        backToMain();
    }

    @Override
    public void onShow() {
        mActivity.mMode = EditImageActivity.Mode.CROP;
        mActivity.mCropImageView.setVisibility(View.VISIBLE);
        mActivity.mViewFlipperSave.showNext();

        // 设置与屏幕匹配的尺寸，确保变换矩阵设置生效后才设置裁剪区域
        mActivity.mMainImageView.post(() -> {
            if (mActivity == null || mActivity.isFinishing() || !isAdded()) {
                return;
            }

            RectF rectF = mActivity.mMainImageView.getBitmapRect();
            mActivity.mCropImageView.setCropRectF(rectF);
        });
    }

    @Override
    public void backToMain() {
        mActivity.mMode = EditImageActivity.Mode.NONE;
        mActivity.mCropImageView.setVisibility(View.GONE);
        mActivity.mBottomOperateBar.setCurrentItem(0);
        if (mSelectedTextView != null) {
            mSelectedTextView.setTextColor(UNSELECTED_COLOR);
        }
        mActivity.mCropImageView.setRatioCropRectF(mActivity.mMainImageView.getBitmapRect(), -1);
        mActivity.mViewFlipperSave.showPrevious();
    }

    /**
     * 选择剪裁比率
     */
    private class CropRatioBtnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            TextView curTextView = (TextView) v;
            mSelectedTextView.setTextColor(UNSELECTED_COLOR);
            RatioItem dataItem = (RatioItem) v.getTag();
            mSelectedTextView = curTextView;
            mSelectedTextView.setTextColor(SELECTED_COLOR);

            mActivity.mCropImageView.setRatioCropRectF(mActivity.mMainImageView.getBitmapRect(),
                    dataItem.getRatio());
        }
    }
}
