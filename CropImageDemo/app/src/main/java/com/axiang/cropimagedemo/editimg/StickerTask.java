package com.axiang.cropimagedemo.editimg;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;

import com.axiang.cropimagedemo.util.DialogUtil;
import com.axiang.cropimagedemo.util.Matrix3;

import java.lang.ref.WeakReference;

/**
 * 贴图合成任务 抽象类
 */
public abstract class StickerTask extends AsyncTask<Bitmap, Void, Bitmap> {

    protected Dialog mLoadingDialog;

    protected WeakReference<EditImageActivity> mWeakReference;

    public StickerTask(EditImageActivity activity) {
        mWeakReference = new WeakReference<>(activity);
    }

    protected EditImageActivity getActivity() {
        if (mWeakReference == null || mWeakReference.get() == null) {
            return null;
        }
        return mWeakReference.get();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        EditImageActivity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        mLoadingDialog = DialogUtil.newLoadingDialog(activity, "图片保存中...", false);
        mLoadingDialog.show();
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        EditImageActivity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return null;
        }

        Matrix touchMatrix = activity.mMainImageView.getImageViewMatrix();

        Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBit);

        float[] data = new float[9];
        touchMatrix.getValues(data);    // 底部图片变化记录矩阵原始数据
        Matrix3 cal = new Matrix3(data);    // 辅助矩阵计算类
        Matrix3 inverseMatrix = cal.inverseMatrix();    // 计算逆矩阵
        Matrix matrix = new Matrix();
        matrix.setValues(inverseMatrix.getValues());

        handleImage(canvas, matrix);
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
        onPostResult(result);
        mLoadingDialog.dismiss();
    }

    public abstract void handleImage(Canvas canvas, Matrix m);

    public abstract void onPostResult(Bitmap result);
}
