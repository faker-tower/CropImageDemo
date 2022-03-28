package com.axiang.cropimagedemo.editimg.crop;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.axiang.cropimagedemo.editimg.EditImageActivity;
import com.axiang.cropimagedemo.util.DialogUtil;
import com.axiang.cropimagedemo.util.Matrix3;
import com.axiang.cropimagedemo.util.ToastUtil;

import java.lang.ref.WeakReference;

/**
 * 图片剪裁生成 异步任务
 */
public class CropImageTask extends AsyncTask<Bitmap, Void, Bitmap> {

    protected Dialog mLoadingDialog;

    protected final WeakReference<EditImageActivity> mEditImageActReference;
    private final RectF mCropRect;  // 剪切区域矩形
    private final Matrix mMainImageViewMatrix;  // MainImageView 的 Matrix
    private final TaskExecuteListener mListener;

    public CropImageTask(@NonNull EditImageActivity activity,
                         RectF cropRect,
                         Matrix mainImageViewMatrix,
                         TaskExecuteListener listener) {
        super();
        mEditImageActReference = new WeakReference<>(activity);
        mCropRect = cropRect;
        mMainImageViewMatrix = mainImageViewMatrix;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        EditImageActivity activity = mEditImageActReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        mLoadingDialog = DialogUtil.newSaveFileDialog(activity);
        mLoadingDialog.show();
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        float[] data = new float[9];
        mMainImageViewMatrix.getValues(data);    // 底部图片变化记录矩阵原始数据
        Matrix3 cal = new Matrix3(data);    // 辅助矩阵计算类
        Matrix3 inverseMatrix = cal.inverseMatrix();    // 计算逆矩阵
        Matrix matrix = new Matrix();
        matrix.setValues(inverseMatrix.getValues());
        matrix.mapRect(mCropRect);   // 变化剪切矩形

        return Bitmap.createBitmap(bitmaps[0],
                (int) mCropRect.left, (int) mCropRect.top,
                (int) mCropRect.width(), (int) mCropRect.height());
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mLoadingDialog.dismiss();
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        super.onCancelled(bitmap);
        mLoadingDialog.dismiss();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        mLoadingDialog.dismiss();

        if (bitmap == null) {
            ToastUtil.showShort("图片保存失败");
            return;
        }

        EditImageActivity activity = mEditImageActReference.get();
        if (activity != null && !activity.isFinishing() && mListener != null) {
            mListener.onPostExecute(bitmap);
        }
    }

    public interface TaskExecuteListener {

        void onPostExecute(Bitmap result);
    }
}
