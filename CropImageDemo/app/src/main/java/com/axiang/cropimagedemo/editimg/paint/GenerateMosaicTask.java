package com.axiang.cropimagedemo.editimg.paint;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;

import com.axiang.cropimagedemo.util.BitmapUtil;

/**
 * Created by 邱翔威 on 2022/4/7
 */
public class GenerateMosaicTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private final RectF mMainImageRectF;
    private final long mOperateRecordTime;
    private TaskExecuteListener mListener;

    public GenerateMosaicTask(RectF mainImageRectF, long operateRecordTime, TaskExecuteListener listener) {
        super();
        mMainImageRectF = new RectF(mainImageRectF);
        mOperateRecordTime = operateRecordTime;
        mListener = listener;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        if (bitmaps == null || bitmaps[0] == null || bitmaps[0].isRecycled()) {
            return null;
        }
        return BitmapUtil.getMosaicBitmap(bitmaps[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (mListener != null) {
            mListener.onPostExecute(bitmap, mOperateRecordTime, mMainImageRectF);
        }
    }

    public interface TaskExecuteListener {

        void onPostExecute(Bitmap result, long operateRecordTime, RectF mainImageRectF);
    }
}
