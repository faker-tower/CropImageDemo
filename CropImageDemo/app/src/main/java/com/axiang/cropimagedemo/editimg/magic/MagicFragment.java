package com.axiang.cropimagedemo.editimg.magic;

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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 指尖魔法 Fragment
 * Created by 邱翔威 on 2022/4/7
 */
public class MagicFragment extends BaseEditImageFragment implements MagicAdapter.OnItemClickListener,
        SaveStickerTask.TaskListener {

    public static final int INDEX = ModuleConfig.INDEX_MAGIC;

    private static final Map<String, String[]> mMaterialMap = new LinkedHashMap<>();

    static {
        mMaterialMap.put("magic/face/1.png",
                new String[]{"magic/face/1.png", "magic/face/2.png", "magic/face/3.png",
                        "magic/face/4.png", "magic/face/5.png", "magic/face/6.png",
                        "magic/face/7.png", "magic/face/8.png", "magic/face/9.png"});
        mMaterialMap.put("magic/love/1.png",
                new String[]{"magic/love/1.png", "magic/love/2.png", "magic/love/3.png",
                        "magic/love/4.png", "magic/love/5.png", "magic/love/6.png",
                        "magic/love/7.png", "magic/love/8.png", "magic/love/9.png"});
        mMaterialMap.put("magic/space/1.png",
                new String[]{"magic/space/1.png", "magic/space/2.png", "magic/space/3.png",
                        "magic/space/4.png", "magic/space/5.png", "magic/space/6.png",
                        "magic/space/7.png", "magic/space/8.png", "magic/space/9.png"});
    }

    private ImageView mIvBackToMain;
    private RecyclerView mRvMagic;
    private ImageView mIvClear;

    private MagicAdapter mMagicAdapter;

    private SaveStickerTask mSaveMagicTask;

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

        mRvMagic.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvMagic.setLayoutManager(mLayoutManager);
        mMagicAdapter = new MagicAdapter(mActivity, mMaterialMap.keySet().toArray(new String[0]));
        mMagicAdapter.setOnItemClickListener(this);
        mRvMagic.setAdapter(mMagicAdapter);

        mIvBackToMain.setOnClickListener(view -> backToMain());
        mIvClear.setOnClickListener(view -> clearMagic());
    }

    /**
     * 清除画布上的所有指尖魔法图
     */
    private void clearMagic() {
        DialogUtil.showAlertDialog(mActivity, "是否清空？",
                (dialogInterface, i) -> mActivity.mMagicView.clearCanvas());
    }

    @Override
    public void onImageClick(String key) {
        mActivity.mMagicView.setMaterials(mMaterialMap.get(key));
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
            canvas.drawBitmap(mActivity.mMagicView.getBufferBitmap(), 0, 0, null);
        }
        canvas.restore();
    }

    @Override
    public void onPostExecute(Bitmap result) {
        mActivity.changeMainBitmap(result);
        backToMain();
    }
}
