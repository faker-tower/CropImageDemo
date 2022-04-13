package com.axiang.cropimagedemo.widget;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.axiang.cropimagedemo.editimg.magic.MagicData;
import com.axiang.cropimagedemo.editimg.magic.MagicFrameData;
import com.axiang.cropimagedemo.editimg.magic.MagicHelper;
import com.axiang.cropimagedemo.util.CollectionUtil;
import com.axiang.cropimagedemo.util.ThreadPoolUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 转为指尖魔法写的 Bitmap 复用池
 * Created by 邱翔威 on 2022/4/13
 */
public class MagicBitmapPool {

    private static final String TAG = "MagicBitmapPool";
    // 默认单张最多分 10 个，多了只选取一张图，避免占用过多内存
    public static final int DEFAULT_BITMAP_POOL_MAX_SIZE = 10;

    private Bitmap[] mBitmaps;
    private List<MagicData.FrameMeta> mFrameMetaList;

    private final ReentrantLock mLock = new ReentrantLock();

    private int mRecyclerCount; // 需要重新加载的计数
    private int mCount; // 已取出的 Bitmap 技术
    private final AtomicBoolean mIsLoadingNewBitmaps; // 是否正在加载新的 Bitmap 合集

    /**
     * @param frameMetaList zip 压缩包中素材信息合集
     */
    public MagicBitmapPool(@NonNull List<MagicData.FrameMeta> frameMetaList) {
        mIsLoadingNewBitmaps = new AtomicBoolean();
        mIsLoadingNewBitmaps.set(true);
        reset(frameMetaList);
    }

    /**
     * 取得第一个 Bitmap，不触发 mCount 计数，主要是为了获取 Bitmap 信息用
     */
    public Bitmap getFirstBitmap() {
        if (isEmpty()) {
            return null;
        }
        return mBitmaps[0];
    }

    public Bitmap getBitmap() {
        if (isEmpty()) {
            return null;
        }

        if (mCount + 1 == mRecyclerCount && !mIsLoadingNewBitmaps.get()) {
            mIsLoadingNewBitmaps.set(true);
            loadNewBitmaps(mRecyclerCount);
        }

        if (mCount >= mBitmaps.length) {
            mCount = mIsLoadingNewBitmaps.get() ? mRecyclerCount : 0;
        }
        return mBitmaps[mCount++];
    }

    private void loadNewBitmaps(final int count) {
        ThreadPoolUtil.execute(() -> {
            Bitmap[] preBitmaps = new Bitmap[count];
            int index = 0;
            for (MagicData.FrameMeta meta : mFrameMetaList) {
                List<Bitmap> splitBitmaps = MagicHelper.splitZipBitmap(meta);
                if (splitBitmaps == null) {
                    continue;
                }

                for (Bitmap splitBitmap : splitBitmaps) {
                    if (index >= preBitmaps.length) {
                        break;
                    }
                    preBitmaps[index++] = splitBitmap;
                }

                if (index >= preBitmaps.length) {
                    break;
                }
            }

            // 打乱整个数组再放进去
            CollectionUtil.random(preBitmaps);
            putAll(preBitmaps);
        });
    }

    public void put(@NonNull Bitmap bitmap) {
        mLock.lock();
        try {
            mBitmaps[0] = bitmap;
        } catch (Exception e) {
            Log.e(TAG, "putAll 方法抛出了异常：" + e);
        } finally {
            mLock.unlock();
            mIsLoadingNewBitmaps.set(false);
        }
    }

    public void putAll(@NonNull Bitmap[] bitmaps) {
        mLock.lock();
        try {
            System.arraycopy(bitmaps, 0, mBitmaps, 0, bitmaps.length);
        } catch (Exception e) {
            Log.e(TAG, "putAll 方法抛出了异常：" + e);
        } finally {
            mLock.unlock();
            mIsLoadingNewBitmaps.set(false);
        }
    }

    public boolean isEmpty() {
        return CollectionUtil.isEmpty(mBitmaps);
    }

    public boolean isNoEmpty() {
        return !isEmpty();
    }

    /**
     * 内容是否填充满了
     */
    public boolean isFillFull() {
        boolean result = isNoEmpty();
        if (result) {
            for (Bitmap bitmap : mBitmaps) {
                if (bitmap == null || bitmap.isRecycled()) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isNoFillFull() {
        return !isFillFull();
    }

    /**
     * 需要重新实例化 mBitmapList
     */
    public void reset(@NonNull List<MagicData.FrameMeta> frameMetaList) {
        reset();    // 清空资源

        mFrameMetaList = frameMetaList;
        for (MagicData.FrameMeta meta : frameMetaList) {
            List<MagicFrameData.Frames> frames = meta.getFrameData().getFrames();
            if (frames.size() > DEFAULT_BITMAP_POOL_MAX_SIZE) {
                mBitmaps = new Bitmap[frames.size()];
            } else {
                mBitmaps = new Bitmap[frames.size() * 2];
            }
        }

        if (mBitmaps != null) {
            mRecyclerCount = mBitmaps.length / 2;
            loadNewBitmaps(mBitmaps.length);
        }
    }

    /**
     * 只释放资源，不需要重新实例化 mBitmapList
     */
    public void reset() {
        if (isEmpty()) {
            return;
        }

        for (int i = 0; i < mBitmaps.length; i++) {
            Bitmap bitmap = mBitmaps[0];
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            mBitmaps[0] = null;
        }
    }
}
