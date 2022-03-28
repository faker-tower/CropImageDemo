package com.axiang.cropimagedemo.editimg;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.axiang.cropimagedemo.util.BitmapUtil;
import com.axiang.cropimagedemo.util.DialogUtil;
import com.axiang.cropimagedemo.util.ToastUtil;

import java.lang.ref.WeakReference;

/**
 * 保存图像任务 抽象类
 */
public class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {

    protected Dialog mLoadingDialog;

    protected final WeakReference<EditImageActivity> mEditImageActReference;
    protected final String mSaveFilePath; // 保存的图片路径
    private final TaskExecuteListener mListener;

    public SaveImageTask(@NonNull EditImageActivity activity, String saveFilePath, TaskExecuteListener listener) {
        super();
        mEditImageActReference = new WeakReference<>(activity);
        mSaveFilePath = saveFilePath;
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
    protected Boolean doInBackground(Bitmap... params) {
        if (TextUtils.isEmpty(mSaveFilePath)) {
            return false;
        }

        return BitmapUtil.saveBitmap(params[0], mSaveFilePath);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mLoadingDialog.dismiss();
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        mLoadingDialog.dismiss();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        mLoadingDialog.dismiss();

        if (!result) {
            ToastUtil.showShort("图片保存失败");
            return;
        }

        EditImageActivity activity = mEditImageActReference.get();
        if (activity != null && !activity.isFinishing() && mListener != null) {
            mListener.onPostExecute();
        }
    }

    public interface TaskExecuteListener {

        void onPostExecute();
    }
}
