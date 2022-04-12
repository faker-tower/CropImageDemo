package com.axiang.cropimagedemo.editimg.magic;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by 邱翔威 on 2022/4/11
 */
public class MagicData {

    private boolean mIsFromZip;  // 是否来自 zip 压缩包

    private String mAssetsThumb;    // assets 文件夹中的缩略图

    private List<String> mAssetsMagicList;  // assets 文件夹中的指尖魔法素材合集

    private List<FrameMeta> mFrameMetaList; //  zip 压缩包中素材信息合集

    public MagicData(boolean isFromZip) {
        mIsFromZip = isFromZip;
    }

    public MagicData(String assetsThumb, List<String> assetsMagicList) {
        mIsFromZip = false;
        mAssetsThumb = assetsThumb;
        mAssetsMagicList = assetsMagicList;
    }

    public MagicData(List<FrameMeta> frameMetaList) {
        mIsFromZip = true;
        mFrameMetaList = frameMetaList;
    }

    public boolean isFromZip() {
        return mIsFromZip;
    }

    public void setFromZip(boolean fromZip) {
        mIsFromZip = fromZip;
    }

    public String getAssetsThumb() {
        return mAssetsThumb;
    }

    public void setAssetsThumb(String assetsThumb) {
        mAssetsThumb = assetsThumb;
    }

    public List<String> getAssetsMagicList() {
        return mAssetsMagicList;
    }

    public void setAssetsMagicList(List<String> assetsMagicList) {
        mAssetsMagicList = assetsMagicList;
    }

    public List<FrameMeta> getFrameMetaList() {
        return mFrameMetaList;
    }

    public void setFrameMetaList(List<FrameMeta> frameMetaList) {
        mFrameMetaList = frameMetaList;
    }

    /**
     * 校验合法性
     */
    public boolean checkValidity() {
        boolean result;
        if (!mIsFromZip) {
            result = !TextUtils.isEmpty(mAssetsThumb);
            result &= mAssetsMagicList != null && !mAssetsMagicList.isEmpty();
        } else {
            result = mFrameMetaList != null && !mFrameMetaList.isEmpty();
            if (result) {
                for (FrameMeta meta : mFrameMetaList) {
                    result &= meta.checkValidity();
                }
            }
        }
        return result;
    }

    public static class FrameMeta {

        private String mFrameImagePath; // zip 压缩包中素材大图路径

        private MagicFrameData mFrameData;  // zip 压缩包中素材信息

        public String getFrameImagePath() {
            return mFrameImagePath;
        }

        public void setFrameImagePath(String frameImagePath) {
            mFrameImagePath = frameImagePath;
        }

        public MagicFrameData getFrameData() {
            return mFrameData;
        }

        public void setFrameData(MagicFrameData frameData) {
            mFrameData = frameData;
        }

        /**
         * 校验合法性
         */
        public boolean checkValidity() {
            boolean result = !TextUtils.isEmpty(mFrameImagePath);
            result &= mFrameData != null;
            if (result) {
                List<MagicFrameData.Frames> frames = mFrameData.getFrames();
                result = frames != null && !frames.isEmpty();
                result &= mFrameData.getMeta() != null && mFrameData.getMeta().getSize() != null;
            }
            return result;
        }
    }
}
