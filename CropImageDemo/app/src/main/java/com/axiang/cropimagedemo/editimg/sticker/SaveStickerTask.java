package com.axiang.cropimagedemo.editimg.sticker;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;

import com.axiang.cropimagedemo.editimg.EditImageActivity;
import com.axiang.cropimagedemo.util.DialogUtil;
import com.axiang.cropimagedemo.util.Matrix3;

import java.lang.ref.WeakReference;

/**
 * 贴图合成保存任务 抽象类
 */
public class SaveStickerTask extends AsyncTask<Bitmap, Void, Bitmap> {

    protected Dialog mLoadingDialog;

    protected final WeakReference<EditImageActivity> mEditImageActReference;
    private final Matrix mMainImageViewMatrix;  // MainImageView 的 Matrix
    private final TaskListener mListener;

    public SaveStickerTask(@NonNull EditImageActivity activity, Matrix mainImageViewMatrix, TaskListener listener) {
        super();
        mEditImageActReference = new WeakReference<>(activity);
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
    protected Bitmap doInBackground(Bitmap... params) {
        Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBit);

        float[] data = new float[9];
        mMainImageViewMatrix.getValues(data);    // 底部图片变化记录矩阵原始数据
        Matrix3 cal = new Matrix3(data);    // 辅助矩阵计算类
        Matrix3 inverseMatrix = cal.inverseMatrix();    // 计算逆矩阵
        Matrix matrix = new Matrix();
        matrix.setValues(inverseMatrix.getValues());

        EditImageActivity activity = mEditImageActReference.get();
        if (activity != null && !activity.isFinishing() && mListener != null) {
            mListener.handleImage(canvas, matrix);
        }
        return resultBit;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mLoadingDialog.dismiss();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCancelled(Bitmap result) {
        super.onCancelled(result);
        mLoadingDialog.dismiss();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        mLoadingDialog.dismiss();

        EditImageActivity activity = mEditImageActReference.get();
        if (activity != null && !activity.isFinishing() && mListener != null) {
            mListener.onPostExecute(result);
        }
    }

    public interface TaskListener {

        void handleImage(Canvas canvas, Matrix matrix);

        void onPostExecute(Bitmap result);
    }
}
