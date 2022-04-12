package com.axiang.cropimagedemo.editimg.magic;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axiang.cropimagedemo.ModuleConfig;
import com.axiang.cropimagedemo.R;
import com.axiang.cropimagedemo.editimg.BaseEditImageFragment;
import com.axiang.cropimagedemo.editimg.EditImageActivity;
import com.axiang.cropimagedemo.editimg.sticker.SaveStickerTask;
import com.axiang.cropimagedemo.util.DialogUtil;
import com.axiang.cropimagedemo.util.PaintUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 指尖魔法 Fragment
 * Created by 邱翔威 on 2022/4/7
 */
public class MagicFragment extends BaseEditImageFragment implements MagicAdapter.OnItemClickListener,
        SaveStickerTask.TaskListener {

    public static final String TAG = "MagicFragment";
    public static final int INDEX = ModuleConfig.INDEX_MAGIC;
    private static final String[] sZipNameList;

    static {
        sZipNameList = new String[]{"103186.zip"};
    }

    private List<MagicData> mMagicDataList;

    private ImageView mIvBackToMain;
    private RecyclerView mRvMagic;
    private ImageView mIvClear;

    private MagicAdapter mMagicAdapter;

    private SaveStickerTask mSaveMagicTask;
    private Runnable mUnzipRunnable;

    public static MagicFragment newInstance() {
        return new MagicFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_magic, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mIvBackToMain = rootView.findViewById(R.id.iv_back_to_main);
        mRvMagic = rootView.findViewById(R.id.rv_magic);
        mIvClear = rootView.findViewById(R.id.iv_clear);

        initRv();

        mIvBackToMain.setOnClickListener(view -> backToMain());
        mIvClear.setOnClickListener(view -> clearMagic());
    }

    private void initRv() {
        mRvMagic.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvMagic.setLayoutManager(mLayoutManager);

        mMagicAdapter = new MagicAdapter(mActivity);
        mMagicAdapter.setOnItemClickListener(this);
        mRvMagic.setAdapter(mMagicAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadData() {
        mMagicDataList = new ArrayList<>();
        MagicHelper.fillAssetsData(mMagicDataList);
        MagicHelper.loadAssetsAllZipData(sZipNameList, magicDataList -> {
            if (!isAdded()) {
                return;
            }

            if (mMagicDataList == null || magicDataList == null || magicDataList.isEmpty()) {
                return;
            }

            mMagicDataList.addAll(magicDataList);
            mMagicAdapter.setMagicDataList(mMagicDataList);
            mActivity.runOnUiThread(() -> mMagicAdapter.notifyDataSetChanged());
        });
    }

    /**
     * 清除画布上的所有指尖魔法图
     */
    private void clearMagic() {
        DialogUtil.showAlertDialog(mActivity, "是否清空？",
                (dialogInterface, i) -> mActivity.mMagicView.clearCanvas());
    }

    @Override
    public void onImageClick(MagicData data) {
        mActivity.mMagicView.setMaterials(data);
    }

    @Override
    public void onShow() {
        mActivity.mMode = EditImageActivity.Mode.MAGIC;
        mActivity.mViewFlipperSave.showNext();
        mActivity.mMagicView.setVisibility(View.VISIBLE);
    }

    @Override
    public void backToMain() {
        mActivity.mMagicView.reset();
        mActivity.mMode = EditImageActivity.Mode.NONE;
        mActivity.mMagicView.setVisibility(View.GONE);
        mActivity.mBottomOperateBar.setCurrentItem(0);
        mActivity.mViewFlipperSave.showPrevious();
    }

    public void applyPaints() {
        if (mSaveMagicTask != null && !mSaveMagicTask.isCancelled()) {
            mSaveMagicTask.cancel(true);
        }

        mSaveMagicTask = new SaveStickerTask(mActivity, mActivity.mMainImageView.getImageViewMatrix(), this);
        mSaveMagicTask.execute(mActivity.mMainBitmap);
    }

    @Override
    public void handleImage(Canvas canvas, Matrix matrix) {
        float[] f = new float[9];
        matrix.getValues(f);
        int dx = (int) f[Matrix.MTRANS_X];
        int dy = (int) f[Matrix.MTRANS_Y];
        float scale_x = f[Matrix.MSCALE_X];
        float scale_y = f[Matrix.MSCALE_Y];
        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale_x, scale_y);

        if (mActivity.mMagicView.getBufferBitmap() != null && !mActivity.mMagicView.getBufferBitmap().isRecycled()) {
            canvas.drawBitmap(mActivity.mMagicView.getBufferBitmap(), 0, 0, PaintUtil.newDefaultPaint());
        }
        canvas.restore();
    }

    @Override
    public void onPostExecute(Bitmap result) {
        mActivity.changeMainBitmap(result);
        backToMain();
    }
}
