package com.axiang.cropimagedemo.editimg.magic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.axiang.cropimagedemo.util.FileUtil;
import com.axiang.cropimagedemo.util.GsonUtil;
import com.axiang.cropimagedemo.util.PaintUtil;
import com.axiang.cropimagedemo.util.ThreadPoolUtil;
import com.axiang.cropimagedemo.util.ZipUtil;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 指尖魔法 Fragment
 * Created by 邱翔威 on 2022/4/7
 */
public class MagicFragment extends BaseEditImageFragment implements MagicAdapter.OnItemClickListener,
        SaveStickerTask.TaskListener {

    public static final String TAG = "MagicFragment";
    public static final int INDEX = ModuleConfig.INDEX_MAGIC;
    private static final String ZIP_FILE_NAME = "103186.zip";

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
    private Runnable mUnzipRunnable;

    public static MagicFragment newInstance() {
        return new MagicFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_magic, container, false);
        initView(rootView);
        loadAssetsMagicZipData();
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
     * 加载 Assets 目录下的 指尖魔法的 zip 资源
     */
    private void loadAssetsMagicZipData() {
        // 解压 zip 资源
        mUnzipRunnable = () -> ZipUtil.unzip(ZIP_FILE_NAME, new ZipUtil.OnUnzipFinishListener() {
            @Override
            public void onSuccess(String successPath) {
                // 取 data.json 文件中的内容
                String dataJson = FileUtil.getJsonFileContent(successPath + File.separator + "data.json");
                if (TextUtils.isEmpty(dataJson)) {
                    return;
                }

                // Gson 转实体类
                MagicData magicData = GsonUtil.JsonToObject(dataJson, MagicData.class);
                String[] magicFiles = getMagicFiles(magicData);
                if (magicFiles == null || magicFiles.length <= 0) {
                    return;
                }

                // 循环取出所有 a1.json、a2.json、a3.json...axx.json 的数据
                String key = null;
                for (String magicFile : magicFiles) {
                    MagicFrameData magicFrameData = GsonUtil.JsonToObject(magicFile, MagicFrameData.class);
                    if (key == null) {
                        key = magicFrameData.getMeta().getImage();
                    }

                }
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "解压 Assets 目录下的 指尖魔法的 zip 资源失败");
            }
        });
        ThreadPoolUtil.execute(mUnzipRunnable);
    }

    /**
     * 取 data.json 文件中的 files 字段
     */
    private String[] getMagicFiles(MagicData magicData) {
        List<MagicData.Frame> frame = magicData.getFrame();
        if (frame == null || frame.isEmpty()) {
            return null;
        }

        List<MagicData.Res> res = frame.get(0).getRes();
        if (res == null || res.isEmpty()) {
            return null;
        }

        String files = res.get(0).getFiles();
        Log.d(TAG, "getMagicFiles(): " + files);
        return files.split(",");
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
